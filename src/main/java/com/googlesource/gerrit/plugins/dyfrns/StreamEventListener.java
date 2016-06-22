package com.googlesource.gerrit.plugins.dyfrns;

import com.google.gerrit.common.EventListener;
import com.google.gerrit.server.events.Event;
import com.google.gerrit.server.events.ReviewerAddedEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamEventListener implements EventListener {
    private static final Logger log = LoggerFactory.getLogger(StreamEventListener.class);

    private static TimerQueue timerQueue;

    @Inject
    StreamEventListener(TimerQueue.Factory timerQueueFactory) {
        log.info("Module created");
        if (timerQueue == null) {
            timerQueue = timerQueueFactory.create();
        }
        log.info("Timer queue's version is " + timerQueue.version);
    }

    @Override
    public void onEvent(Event event) {
        log.info("Got an event of type " + event.getType());

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
        TimerEvent event = new TimerEvent(
                reviewerAddedEvent.change.number,
                new String[]{reviewerAddedEvent.reviewer.email});
        try {
            timerQueue.add(event);
        } catch (Exception e) {
            log.info("Got an error: ", e);
        }
    }
}
