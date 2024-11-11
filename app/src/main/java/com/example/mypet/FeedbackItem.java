package com.example.mypet;

import android.net.Uri;

public class FeedbackItem {
    private String feedbackText;
    private Uri feedbackImageUri;
    private String feedbackKey;

    public FeedbackItem(String feedbackText, Uri feedbackImageUri) {
        this.feedbackText = feedbackText;
        this.feedbackImageUri = feedbackImageUri;
    }

    // Constructor with all fields
    public FeedbackItem(String feedbackText, Uri feedbackImageUri, String feedbackKey) {
        this.feedbackText = feedbackText;
        this.feedbackImageUri = feedbackImageUri;
        this.feedbackKey = feedbackKey;
    }

    // Getters and setters for all fields
    public String getFeedbackText() {
        return feedbackText;
    }

    public void setFeedbackText(String feedbackText) {
        this.feedbackText = feedbackText;
    }

    public Uri getFeedbackImageUri() {
        return feedbackImageUri;
    }

    public void setFeedbackImageUri(Uri feedbackImageUri) {
        this.feedbackImageUri = feedbackImageUri;
    }

    public String getFeedbackKey() {
        return feedbackKey;
    }

    public void setFeedbackKey(String feedbackKey) {
        this.feedbackKey = feedbackKey;
    }
}