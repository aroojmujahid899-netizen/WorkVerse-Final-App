package com.workverse.app.utils;

import com.google.firebase.firestore.FieldValue;
import com.workverse.app.models.Feedback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ties GeminiFeedbackService (the actual AI call) to Firestore: marks a
 * feedback doc "processing", calls the real AI, then writes the validated
 * result back — or writes aiStatus="failed" if anything goes wrong. Never
 * fabricates a score on failure.
 *
 * Call analyzeAndStore() once right after a feedback doc is created, and
 * again from a "Retry AI analysis" action if aiStatus == "failed".
 */
public class FeedbackAnalysisHelper {

    public interface OnDone {
        void done(); // called after Firestore write completes, success or failure
    }

    public static void analyzeAndStore(String feedbackId, String userId, String title, String message, OnDone onDone) {
        if (feedbackId == null || userId == null) { if (onDone != null) onDone.done(); return; }

        FirebaseHelper.getDb().collection(FirebaseHelper.COL_FEEDBACK).document(feedbackId)
                .update("aiStatus", "processing")
                .addOnCompleteListener(ignored -> GeminiFeedbackService.analyze(title, message, new GeminiFeedbackService.Callback() {
                    @Override
                    public void onSuccess(GeminiFeedbackService.Result result) {
                        Map<String, Object> update = new HashMap<>();
                        update.put("aiStatus", "completed");
                        update.put("aiSentiment", result.sentiment);
                        update.put("aiScore", result.score);
                        update.put("aiSummary", result.summary);
                        update.put("aiPositivePoints", result.positivePoints);
                        update.put("aiNegativePoints", result.negativePoints);

                        // Convert concerns String to List to match Feedback model
                        List<String> concernsList = new ArrayList<>();
                        if (result.concerns != null && !result.concerns.isEmpty()) {
                            concernsList.add(result.concerns);
                        }
                        update.put("aiConcerns", concernsList);

                        // Convert confidence String to Double to match Feedback model
                        try {
                            update.put("aiConfidence", Double.parseDouble(result.confidence.replace("%", "")));
                        } catch (Exception e) {
                            update.put("aiConfidence", 0.0);
                        }
                        update.put("aiProvider", "Groq AI");
                        update.put("aiModel", result.model);
                        update.put("aiAnalyzedAt", System.currentTimeMillis());
                        update.put("aiError", FieldValue.delete());
                        // Legacy field kept in sync so any older UI reading getSentiment() still works.
                        update.put("sentiment", result.sentiment);

                        FirebaseHelper.getDb().collection(FirebaseHelper.COL_FEEDBACK).document(feedbackId)
                                .update(update)
                                .addOnCompleteListener(ignored2 -> {
                                    KPISyncHelper.recomputeForUser(userId);
                                    if (onDone != null) onDone.done();
                                });
                    }

                    @Override
                    public void onError(String error) {
                        Map<String, Object> update = new HashMap<>();
                        update.put("aiStatus", "failed");
                        update.put("aiError", error);
                        update.put("aiAnalyzedAt", System.currentTimeMillis());
                        FirebaseHelper.getDb().collection(FirebaseHelper.COL_FEEDBACK).document(feedbackId)
                                .update(update)
                                .addOnCompleteListener(ignored2 -> { if (onDone != null) onDone.done(); });
                    }
                }));
    }

    /** Convenience overload for retrying from a Feedback object already loaded in a list. */
    public static void retry(Feedback feedback, OnDone onDone) {
        analyzeAndStore(feedback.getId(), feedback.getUserId(), feedback.getTitle(), feedback.getMessage(), onDone);
    }
}
