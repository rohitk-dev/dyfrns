package com.googlesource.gerrit.plugins.dyfrns;

import com.google.gerrit.common.EventListener;
import com.google.gerrit.server.events.*;
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
            case "change-abandoned":
                onChangeAbandoned((ChangeAbandonedEvent)event);
                break;
            case "change-merged":
                onChangeMerged((ChangeMergedEvent)event);
                break;
            case "comment-added":
                onCommentAdded((CommentAddedEvent)event);
                break;
            default:
                return;
        }
    }

    private void onReviewerAdded(ReviewerAddedEvent reviewerAddedEvent) {
        log.info("Reviewer " + reviewerAddedEvent.reviewer.email + " added");
        try {
            timerQueue.addReviewer(reviewerAddedEvent.change.number, reviewerAddedEvent.reviewer.email);
        } catch (Exception e) {
            log.info("Got an error: ", e);
        }
    }

    private void onChangeAbandoned(ChangeAbandonedEvent changeAbandonedEvent) {
        try {
            timerQueue.cancel(changeAbandonedEvent.change.number);
        } catch (Exception e) {
            log.info("Got an error: ", e);
        }
    }

    private void onChangeMerged(ChangeMergedEvent changeMergedEvent) {
        try {
            timerQueue.cancel(changeMergedEvent.change.number);
        } catch (Exception e) {
            log.info("Got an error: ", e);
        }
    }

    private void onCommentAdded(CommentAddedEvent commentAddedEvent) {
        try {
            timerQueue.removeReviewer(commentAddedEvent.change.number, commentAddedEvent.author.email);
        } catch (Exception e) {
            log.info("Got an error: ", e);
        }
    }
}
