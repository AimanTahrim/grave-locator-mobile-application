package com.example.glmaclient.persistentcloudanchor;

public class AnchorItem {
    private final String anchorId;
    private final String anchorName;
    private final long minutesSinceCreation;
    private boolean selected;

    public AnchorItem(String anchorId, String anchorName, long minutesSinceCreation) {
        this.anchorId = anchorId;
        this.anchorName = anchorName;
        this.minutesSinceCreation = minutesSinceCreation;
        this.selected = false;
    }

    public String getAnchorName() {
        return anchorName;
    }

    public String getAnchorId() {
        return anchorId;
    }

    public String getMinutesSinceCreation() {
        if (minutesSinceCreation < 60) {
            return minutesSinceCreation + "m ago";
        } else {
            return (int) minutesSinceCreation / 60 + "hr ago";
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
