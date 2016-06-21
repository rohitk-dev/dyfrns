package com.googlesource.gerrit.plugins.dyfrns;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimerTask;

public class TimerEvent implements Comparable<TimerEvent>, Runnable {
    private int id;
    private Date expire;
    private TimerQueue timerQueue;
    private String[] emails;

    private int reminder = 0;

    private final int TIMEOUT = 1 * 2 * 1000;

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.S");

    TimerEvent(int id, String[] emails) {
        this.id = id;
        this.emails = emails;
        this.expire = new Date(System.currentTimeMillis() + TIMEOUT);
    }

    @Override
    public int compareTo(TimerEvent o) {
        if (id == o.id) {
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
        return id == ((TimerEvent)obj).id;
    }

    @Override
    public void run() {
        if (timerQueue == null) {
            System.out.println("TimerQueue reference is null");
            return;
        }

        // perform the actual task
        System.out.println(this);
        reminder++;

        // update the expiration time
        expire = new Date(System.currentTimeMillis() + TIMEOUT);

        // reschedule, if needed; if not, remove ourselves from the queue
        try {
            timerQueue.reschedule(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Date getExpire() {
        return expire;
    }

    public void setTimerQueue(TimerQueue timerQueue) {
        this.timerQueue = timerQueue;
    }

    @Override
    public String toString() {
        return String.format("Task %d:" +
                        "\n\texpire: %s" +
                        "\n\treminder: #%d" +
                        "\n\temails: %s",
                id, dateFormat.format(expire), reminder, Arrays.toString(emails));
    }
}
