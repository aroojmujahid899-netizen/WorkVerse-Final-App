package com.workverse.app.utils;

/**
 * AI Feedback Analyzer
 * Analyzes employee feedback text using keyword-based sentiment analysis.
 * Compares feedback sentiment against actual performance data (calls, sales)
 * to generate a credibility score for KPI evaluation.
 */
public class AIFeedbackAnalyzer {

    public static final String SENTIMENT_POSITIVE = "Positive";
    public static final String SENTIMENT_NEGATIVE = "Negative";
    public static final String SENTIMENT_NEUTRAL  = "Neutral";

    // Positive keywords
    private static final String[] POSITIVE_WORDS = {
        "great", "good", "excellent", "amazing", "awesome", "fantastic",
        "well", "acha", "best", "perfect", "productive", "achieved",
        "success", "target", "sales", "happy", "motivated", "improved",
        "better", "outstanding", "superb", "nice", "wonderful", "positive",
        "accomplished", "completed", "quality", "leads", "interested",
        "customers", "revenue", "profit", "growth", "high", "strong"
    };

    // Negative keywords
    private static final String[] NEGATIVE_WORDS = {
        "bad", "poor", "worst", "terrible", "difficult", "hard", "struggle",
        "bura", "mushkil", "problem", "issue", "fail", "failed", "low",
        "slow", "no sales", "no calls", "rejection", "negative", "sad",
        "frustrated", "boring", "not good", "no leads", "weak", "decrease",
        "loss", "below", "missed", "absent", "late", "incomplete"
    };

    /**
     * Analyze sentiment of feedback text
     * @param feedbackText employee written feedback
     * @return POSITIVE, NEGATIVE, or NEUTRAL
     */
    public static String analyzeSentiment(String feedbackText) {
        if (feedbackText == null || feedbackText.trim().isEmpty()) {
            return SENTIMENT_NEUTRAL;
        }
        String lower = feedbackText.toLowerCase();
        int positiveScore = 0;
        int negativeScore = 0;

        for (String word : POSITIVE_WORDS) {
            if (lower.contains(word)) positiveScore++;
        }
        for (String word : NEGATIVE_WORDS) {
            if (lower.contains(word)) negativeScore++;
        }

        if (positiveScore > negativeScore) return SENTIMENT_POSITIVE;
        if (negativeScore > positiveScore) return SENTIMENT_NEGATIVE;
        return SENTIMENT_NEUTRAL;
    }

    /**
     * Calculate Feedback Accuracy Score
     * Compares employee's written sentiment vs actual performance data
     * @param sentiment     analyzed sentiment (Positive/Negative/Neutral)
     * @param totalCalls    actual calls made by employee
     * @param totalSales    actual sales achieved
     * @return accuracy score: "High", "Medium", or "Low"
     */
    public static String calculateFeedbackAccuracy(String sentiment, int totalCalls, int totalSales) {
        boolean performanceGood = (totalCalls >= 50 && totalSales >= 3);
        boolean performancePoor = (totalCalls < 20 || totalSales == 0);

        if (SENTIMENT_POSITIVE.equals(sentiment) && performanceGood) return "High";
        if (SENTIMENT_NEGATIVE.equals(sentiment) && performancePoor) return "High";
        if (SENTIMENT_NEUTRAL.equals(sentiment)) return "Medium";
        if (SENTIMENT_POSITIVE.equals(sentiment) && performancePoor) return "Low";
        if (SENTIMENT_NEGATIVE.equals(sentiment) && performanceGood) return "Low";
        return "Medium";
    }

    /**
     * Generate Performance Insight based on sentiment + actual data
     * @param sentiment     feedback sentiment
     * @param accuracy      feedback accuracy vs actual data
     * @param kpiScore      KPI score from attendance + sales
     * @return human-readable performance insight
     */
    public static String generateInsight(String sentiment, String accuracy, double kpiScore) {
        if (SENTIMENT_POSITIVE.equals(sentiment) && "High".equals(accuracy) && kpiScore >= 70) {
            return "Excellent — feedback matches strong performance";
        } else if (SENTIMENT_POSITIVE.equals(sentiment) && "Low".equals(accuracy)) {
            return "Needs Review — positive feedback but low actual performance";
        } else if (SENTIMENT_NEGATIVE.equals(sentiment) && "High".equals(accuracy)) {
            return "Honest — employee correctly identified own challenges";
        } else if (SENTIMENT_NEGATIVE.equals(sentiment) && "Low".equals(accuracy)) {
            return "Mismatch — negative feedback but performance data is good";
        } else if (kpiScore >= 80) {
            return "Good Performance";
        } else if (kpiScore >= 50) {
            return "Average Performance";
        } else {
            return "Below Target — needs improvement";
        }
    }

    /**
     * Get color resource name for sentiment badge
     */
    public static String getSentimentColor(String sentiment) {
        switch (sentiment) {
            case SENTIMENT_POSITIVE: return "status_approved";   // green
            case SENTIMENT_NEGATIVE: return "status_rejected";   // red
            default:                 return "status_pending";    // orange
        }
    }
}
