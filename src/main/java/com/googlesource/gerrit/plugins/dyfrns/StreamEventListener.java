package com.googlesource.gerrit.plugins.dyfrns;

import com.google.gerrit.common.EventListener;
import com.google.gerrit.server.events.Event;
import com.google.gerrit.server.events.ReviewerAddedEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class StreamEventListener implements EventListener {
    private static final Logger log = LoggerFactory.getLogger(StreamEventListener.class);

    EventListener eventListener;

    @Inject
    StreamEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
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
        log.info("Reviewer " + reviewerAddedEvent.reviewer.email + " added");
    }
}
