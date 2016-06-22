package com.googlesource.gerrit.plugins.dyfrns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimerTask;

public class TimerEvent implements Comparable<TimerEvent>, Runnable {
    private static final Logger log = LoggerFactory.getLogger(TimerEvent.class);

    private String id;
    private Date expire;
    private TimerQueue timerQueue;
    private String[] emails;

    private int reminder = 0;

    private final int TIMEOUT = 1 * 10 * 1000;

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.S");

    TimerEvent(String id, String[] emails) {
        this.id = id;
        this.emails = emails;
        this.expire = new Date(System.currentTimeMillis() + TIMEOUT);
    }

    @Override
    public int compareTo(TimerEvent o) {
        if (id.equals(o.id)) {
            return 0;
        } else {
            return expire.compareTo(o.expire);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TimerEvent)) {
            return false;
        }
        return id.equals(((TimerEvent)obj).id);
    }

    @Override
    public void run() {
        if (timerQueue == null) {
            log.warn("TimerQueue reference is null");
            return;
        }

        // perform the actual task
        //System.out.println(this);
        reminder++;

        // update the expiration time
        expire = new Date(System.currentTimeMillis() + TIMEOUT);

        // reschedule, if needed; if not, remove ourselves from the queue
        try {
            log.info("About to call timerQueue.reschedule");
            timerQueue.reschedule(this);
            log.info("Just called timerQueue.reschedule");
        } catch (Exception e) {
            log.info("Got an error: ", e);
            //e.printStackTrace();
        }
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return emails[0];
    }

    public Date getExpire() {
        return expire;
    }

    public void setTimerQueue(TimerQueue timerQueue) {
        this.timerQueue = timerQueue;
    }


    public int getReminder() {
        return reminder;
    }

    @Override
    public String toString() {
        return String.format("Task %s:" +
                        "; expire: %s" +
                        "; reminder: #%d" +
                        "; emails: %s",
                id, dateFormat.format(expire), reminder, Arrays.toString(emails));
    }
}
