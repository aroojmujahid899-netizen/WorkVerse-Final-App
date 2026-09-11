package com.workverse.app.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GeminiFeedbackService {

    private static final String TAG = "GroqAIService";
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String API_KEY = com.workverse.app.BuildConfig.GROQ_API_KEY;
    private static final String MODEL = "openai/gpt-oss-20b";

    public interface Callback {
        void onSuccess(Result result);
        void onError(String error);
    }

    public static class Result {
        public String sentiment;
        public int score;
        public String summary;
        public String concerns;
        public String confidence;
        public String model;
        public List<String> positivePoints;
        public List<String> negativePoints;

        public Result(String sentiment, int score, String summary, String concerns, String confidence, String model) {
            this.sentiment = sentiment;
            this.score = score;
            this.summary = summary;
            this.concerns = concerns;
            this.confidence = confidence;
            this.model = model;
            this.positivePoints = new ArrayList<>();
            this.negativePoints = new ArrayList<>();
        }
    }

    public static void analyze(String title, String message, Callback callback) {
        analyze(null, title, message, callback);
    }

    public static void analyze(Context context, String title, String message, Callback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(GROQ_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("User-Agent", "WorkVerseApp/1.0");
                conn.setDoOutput(true);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("model", MODEL);

                JSONArray messagesArray = new JSONArray();

                JSONObject systemMessage = new JSONObject();
                systemMessage.put("role", "system");
                systemMessage.put("content", "You are an expert AI Feedback Analyzer. Analyze the user's feedback and respond ONLY with a valid JSON object. Do not include any markdown formatting like ```json or any extra text outside the JSON. JSON structure:\n" +
                        "{\n" +
                        "  \"sentiment\": \"Positive\" or \"Negative\" or \"Neutral\",\n" +
                        "  \"score\": integer between 0 and 100,\n" +
                        "  \"summary\": \"A concise summary of the feedback\",\n" +
                        "  \"concerns\": \"Key concerns mentioned or None\",\n" +
                        "  \"confidence\": \"High\",\n" +
                        "  \"positivePoints\": [\"array of positive aspects mentioned\"],\n" +
                        "  \"negativePoints\": [\"array of negative aspects mentioned\"]\n" +
                        "}");
                messagesArray.put(systemMessage);

                JSONObject userMessage = new JSONObject();
                userMessage.put("role", "user");
                userMessage.put("content", "Title: " + title + "\nMessage: " + message);
                messagesArray.put(userMessage);

                jsonBody.put("messages", messagesArray);

                JSONObject responseFormat = new JSONObject();
                responseFormat.put("type", "json_object");
                jsonBody.put("response_format", responseFormat);

                OutputStream os = conn.getOutputStream();
                os.write(jsonBody.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    if (choices.length() > 0) {
                        String content = choices.getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content").trim();

                        if (content.startsWith("```json")) {
                            content = content.substring(7);
                        } else if (content.startsWith("```")) {
                            content = content.substring(3);
                        }
                        if (content.endsWith("```")) {
                            content = content.substring(0, content.length() - 3);
                        }
                        content = content.trim();

                        JSONObject contentJson = new JSONObject(content);
                        String sentiment = contentJson.optString("sentiment", "Neutral");
                        int score = contentJson.optInt("score", 50);
                        String summary = contentJson.optString("summary", "No summary provided.");
                        String concerns = contentJson.optString("concerns", "None");
                        String confidence = contentJson.optString("confidence", "High");

                        Result result = new Result(sentiment, score, summary, concerns, confidence, MODEL);

                        JSONArray posArray = contentJson.optJSONArray("positivePoints");
                        if (posArray != null) {
                            for (int i = 0; i < posArray.length(); i++) {
                                result.positivePoints.add(posArray.getString(i));
                            }
                        }
                        JSONArray negArray = contentJson.optJSONArray("negativePoints");
                        if (negArray != null) {
                            for (int i = 0; i < negArray.length(); i++) {
                                result.negativePoints.add(negArray.getString(i));
                            }
                        }

                        new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(result));
                    } else {
                        new Handler(Looper.getMainLooper()).post(() -> callback.onError("Empty AI response"));
                    }
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError("Server returned code: " + responseCode));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error during analysis", e);
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }

    public static void generatePerformanceInsight(String employeeName, double kpi, double attendance, Double sales, Double aiFeedback, Callback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(GROQ_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("User-Agent", "WorkVerseApp/1.0");
                conn.setDoOutput(true);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("model", MODEL);

                JSONArray messagesArray = new JSONArray();

                JSONObject systemMessage = new JSONObject();
                systemMessage.put("role", "system");
                systemMessage.put("content", "You are an expert HR Performance Analyst. Analyze the employee performance data and provide a concise, professional, and actionable insight (1-2 sentences). Respond ONLY with a valid JSON object. JSON structure:\n" +
                        "{\n" +
                        "  \"insight\": \"Your professional analysis here\"\n" +
                        "}");
                messagesArray.put(systemMessage);

                JSONObject userMessage = new JSONObject();
                String data = "Employee: " + employeeName +
                        "\nKPI Score: " + kpi +
                        "\nAttendance: " + attendance + "%" +
                        "\nTasks Assigned: " + sales +
                        "\nTasks Completed: " + aiFeedback;

                userMessage.put("role", "user");
                userMessage.put("content", "Analyze this performance data and provide a concise professional insight (max 2 sentences) based on task completion and KPI:\n" + data);
                messagesArray.put(userMessage);

                jsonBody.put("messages", messagesArray);

                JSONObject responseFormat = new JSONObject();
                responseFormat.put("type", "json_object");
                jsonBody.put("response_format", responseFormat);

                OutputStream os = conn.getOutputStream();
                os.write(jsonBody.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    if (choices.length() > 0) {
                        String content = choices.getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content").trim();

                        JSONObject contentJson = new JSONObject(content);
                        String insight = contentJson.optString("insight", "Performance is consistent.");

                        Result result = new Result("Neutral", (int) kpi, insight, "None", "High", MODEL);
                        result.summary = insight;

                        new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(result));
                    } else {
                        new Handler(Looper.getMainLooper()).post(() -> callback.onError("Empty AI response"));
                    }
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError("Server returned code: " + responseCode));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error during insight generation", e);
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }

    public static void calculateKpiAndInsight(String employeeName, int tasksAssigned, int tasksCompleted, double attendancePercentage, Callback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(GROQ_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("User-Agent", "WorkVerseApp/1.0");
                conn.setDoOutput(true);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("model", MODEL);

                JSONArray messagesArray = new JSONArray();

                JSONObject systemMessage = new JSONObject();
                systemMessage.put("role", "system");
                systemMessage.put("content", "You are an expert HR Performance Analyst. Based on task completion ratio and attendance percentage, calculate a fair KPI score (0-100) for this employee, and give a concise professional insight (1-2 sentences). Respond ONLY with a valid JSON object. JSON structure:\n" +
                        "{\n" +
                        "  \"kpiScore\": integer between 0 and 100,\n" +
                        "  \"insight\": \"Your professional analysis here\"\n" +
                        "}");
                messagesArray.put(systemMessage);

                JSONObject userMessage = new JSONObject();
                String data = "Employee: " + employeeName +
                        "\nTasks Assigned: " + tasksAssigned +
                        "\nTasks Completed: " + tasksCompleted +
                        "\nAttendance: " + attendancePercentage + "%";

                userMessage.put("role", "user");
                userMessage.put("content", "Calculate a KPI score and give an insight for this employee based on their task completion and attendance:\n" + data);
                messagesArray.put(userMessage);

                jsonBody.put("messages", messagesArray);

                JSONObject responseFormat = new JSONObject();
                responseFormat.put("type", "json_object");
                jsonBody.put("response_format", responseFormat);

                OutputStream os = conn.getOutputStream();
                os.write(jsonBody.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    if (choices.length() > 0) {
                        String content = choices.getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content").trim();

                        JSONObject contentJson = new JSONObject(content);
                        int kpiScore = contentJson.optInt("kpiScore", 50);
                        String insight = contentJson.optString("insight", "Performance is consistent.");

                        Result result = new Result("Neutral", kpiScore, insight, "None", "High", MODEL);
                        result.summary = insight;

                        new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(result));
                    } else {
                        new Handler(Looper.getMainLooper()).post(() -> callback.onError("Empty AI response"));
                    }
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError("Server returned code: " + responseCode));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error calculating KPI", e);
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }
}