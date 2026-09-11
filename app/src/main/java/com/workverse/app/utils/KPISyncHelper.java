package com.workverse.app.utils;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

/**
 * Runs entirely on the Android client (no backend). Whenever a feedback
 * item finishes AI analysis, this recomputes:
 *   1. That employee's average AI Feedback Score across all their
 *      AI-analyzed feedback (stored at ai_scores/{userId}).
 *   2. Their Final KPI on every performance/{id} doc that belongs to them.
 */
public class KPISyncHelper {

    /** Fire-and-forget: recompute aggregate AI score + Final KPI for one employee. */
    public static void recomputeForUser(String userId) {
        if (userId == null || userId.isEmpty()) return;

        FirebaseHelper.getDb().collection(FirebaseHelper.COL_FEEDBACK)
                .whereEqualTo("userId", userId)
                .whereEqualTo("aiStatus", "completed")
                .get()
                .addOnSuccessListener(feedbackSnap -> {
                    Double avgAiScore = null;
                    String latestInsight = null;
                    int posC = 0, neuC = 0, negC = 0;

                    if (!feedbackSnap.isEmpty()) {
                        double total = 0; int n = 0;
                        for (QueryDocumentSnapshot d : feedbackSnap) {
                            // Safe parsing for aiScore (handles Long, Double, and String formats safely)
                            Object rawScore = d.get("aiScore");
                            Double score = parseDoubleSafely(rawScore);
                            
                            if (score != null) { 
                                total += score; 
                                n++; 
                            }

                            String sent = d.getString("aiSentiment");
                            if ("Positive".equals(sent)) posC++;
                            else if ("Negative".equals(sent)) negC++;
                            else neuC++;

                            // Pick up latest summary for Performance Insight dashboard display
                            String summary = d.getString("aiSummary");
                            if (summary != null && !summary.trim().isEmpty()) {
                                latestInsight = summary;
                            }
                        }
                        if (n > 0) avgAiScore = Math.round((total / n) * 10.0) / 10.0;
                    }

                    Map<String, Object> aiScoreDoc = new HashMap<>();
                    aiScoreDoc.put("avgScore", avgAiScore);
                    aiScoreDoc.put("count", feedbackSnap.size());
                    aiScoreDoc.put("userId", userId);
                    Map<String, Object> breakdown = new HashMap<>();
                    breakdown.put("positive", posC);
                    breakdown.put("neutral", neuC);
                    breakdown.put("negative", negC);
                    aiScoreDoc.put("sentimentBreakdown", breakdown);
                    aiScoreDoc.put("updatedAt", System.currentTimeMillis());
                    
                    FirebaseHelper.getDb().collection(FirebaseHelper.COL_AI_SCORES).document(userId).set(aiScoreDoc);

                    Double finalAvgAiScore = avgAiScore;
                    syncFinalKpi(userId, finalAvgAiScore, latestInsight);
                });
    }

    private static void syncFinalKpi(String userId, Double aiScore, String latestInsight) {
        com.google.android.gms.tasks.Task<QuerySnapshot> perfTask = FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_PERFORMANCE).whereEqualTo("userId", userId).get();
        com.google.android.gms.tasks.Task<QuerySnapshot> salesTask = FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_SALES).whereEqualTo("userId", userId).get();

        Tasks.whenAllSuccess(perfTask, salesTask).addOnSuccessListener(results -> {
            QuerySnapshot perfSnap = (QuerySnapshot) results.get(0);
            QuerySnapshot salesSnap = (QuerySnapshot) results.get(1);
            if (perfSnap.isEmpty()) return;

            Double salesPct = computeSalesPercent(salesSnap);

            WriteBatch batch = FirebaseHelper.getDb().batch();
            for (DocumentSnapshot doc : perfSnap.getDocuments()) {
                Double basePerf = parseDoubleSafely(doc.get("kpiScore"));
                Double attendance = parseDoubleSafely(doc.get("attendancePercentage"));
                double[] weighted = computeFinalKpi(basePerf, attendance, salesPct, aiScore);

                Map<String, Object> update = new HashMap<>();
                update.put("aiFeedbackScore", aiScore);
                update.put("salesScore", salesPct);
                update.put("finalKpiScore", weighted[0] < 0 ? null : weighted[0]);
                
                // Using 'performanceInsight' to match the PerformanceReport model and UI adapter
                if (latestInsight != null) {
                    update.put("performanceInsight", latestInsight);
                }
                update.put("kpiComputedAt", System.currentTimeMillis());
                
                batch.update(doc.getReference(), update);
            }
            batch.commit();
        });
    }

    private static Double computeSalesPercent(QuerySnapshot salesSnap) {
        if (salesSnap.isEmpty()) return null;
        double target = 0, achieved = 0;
        for (QueryDocumentSnapshot d : salesSnap) {
            Double t = parseDoubleSafely(d.get("targetAmount"));
            Double a = parseDoubleSafely(d.get("achievedAmount"));
            if (t != null) target += t;
            if (a != null) achieved += a;
        }
        if (target <= 0) return null;
        return Math.min(100.0, Math.round((achieved / target) * 1000.0) / 10.0);
    }

    private static double[] computeFinalKpi(Double basePerf, Double attendance, Double sales, Double aiFeedback) {
        double totalWeight = 0, weightedSum = 0;
        if (basePerf != null) { totalWeight += KPIWeights.BASE_PERFORMANCE; weightedSum += basePerf * KPIWeights.BASE_PERFORMANCE; }
        if (attendance != null) { totalWeight += KPIWeights.ATTENDANCE; weightedSum += attendance * KPIWeights.ATTENDANCE; }
        if (sales != null) { totalWeight += KPIWeights.SALES; weightedSum += sales * KPIWeights.SALES; }
        if (aiFeedback != null) { totalWeight += KPIWeights.AI_FEEDBACK; weightedSum += aiFeedback * KPIWeights.AI_FEEDBACK; }
        if (totalWeight <= 0) return new double[]{-1};
        return new double[]{Math.round((weightedSum / totalWeight) * 10.0) / 10.0};
    }

    // Utility method to safely parse any Object to Double
    private static Double parseDoubleSafely(Object value) {
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble(((String) value).replace("%", "").trim());
            } catch (Exception ignored) {}
        }
        return null;
    }
}
