package com.workverse.app.utils;

/**
 * Documents the KPI weighting used by the backend (functions/index.js,
 * KPI_WEIGHTS + computeFinalKpi). The actual calculation runs server-side
 * in KPISyncHelper so it stays consistent no matter which screen or
 * role is viewing the data — this class exists so the weights are visible
 * and explainable from the Android codebase too, and so the app can label
 * the breakdown correctly.
 *
 * Audit finding: this project does not track a "Calls" metric anywhere
 * (no Calls model, Firestore collection, or screen exists in WorkVerse),
 * so it is intentionally excluded from the formula. The Final KPI instead
 * combines the four metrics that actually exist in the app:
 *
 *   Final KPI = (BasePerformance * 0.45 + Attendance * 0.20
 *                + Sales * 0.15 + AI Feedback * 0.20) / sum(weights of
 *                the components that are actually available)
 *
 * If a component isn't available yet for an employee (e.g. no feedback has
 * been AI-analyzed, or they have no sales records), it is dropped and the
 * remaining weights are renormalized rather than treating the missing
 * component as a 0.
 */
public final class KPIWeights {
    public static final double BASE_PERFORMANCE = 0.45;
    public static final double ATTENDANCE = 0.20;
    public static final double SALES = 0.15;
    public static final double AI_FEEDBACK = 0.20;

    private KPIWeights() {}
}
