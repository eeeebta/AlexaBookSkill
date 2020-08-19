package com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.model;

public class ReadingListResponse {
    private String responseMessage;
    private boolean isSuccessful;

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }
}
