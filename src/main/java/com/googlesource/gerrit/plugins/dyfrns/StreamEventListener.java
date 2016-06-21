package com.googlesource.gerrit.plugins.dyfrns;

import com.google.gerrit.common.EventListener;
import com.google.gerrit.server.events.Event;
import com.google.gerrit.server.events.ReviewerAddedEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class StreamEventListener implements EventListener {
    @Inject
    StreamEventListener() {

    }

    @Override
    public void onEvent(Event event) {
        String eventType = event.getType();
        switch (eventType) {
            case "reviewer-added":
                onReviewerAdded((ReviewerAddedEvent)event);
                break;
            default:
                return;
        }
    }

    private void onReviewerAdded(ReviewerAddedEvent reviewerAddedEvent) {

    }
}
