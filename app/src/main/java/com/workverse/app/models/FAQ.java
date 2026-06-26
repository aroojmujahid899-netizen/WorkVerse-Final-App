package com.workverse.app.models;

public class FAQ {
    private String id, question, answer, category;
    private long timestamp;

    public FAQ() {}

    public FAQ(String question, String answer, String category) {
        this.question = question;
        this.answer = answer;
        this.category = category;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() { return id; } public void setId(String v) { id = v; }
    public String getQuestion() { return question; } public void setQuestion(String v) { question = v; }
    public String getAnswer() { return answer; } public void setAnswer(String v) { answer = v; }
    public String getCategory() { return category; } public void setCategory(String v) { category = v; }
    public long getTimestamp() { return timestamp; } public void setTimestamp(long v) { timestamp = v; }
}
