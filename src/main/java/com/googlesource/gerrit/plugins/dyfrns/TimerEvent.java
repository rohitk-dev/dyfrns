package com.googlesource.gerrit.plugins.dyfrns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

public class TimerEvent implements Comparable<TimerEvent>, Runnable {
    private static final Logger log = LoggerFactory.getLogger(TimerEvent.class);

    private String id;
    private String subject;
    private Date expire;
    private TimerQueue timerQueue;
    private ArrayList<Info> infos;

    private final int TIMEOUT = 1 * 10 * 1000;

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.S");

    TimerEvent(String id, String subject, String email, String name) {
        this.id = id;
        this.subject = subject;
        this.infos = new ArrayList<>();
        this.infos.add(new Info(email, name));
        this.expire = new Date(System.currentTimeMillis() + TIMEOUT);
    }

    @Override
    public int compareTo(TimerEvent o) {
        int compare = expire.compareTo(o.expire);

        if (compare == 0) {
            return id.compareTo(o.id);
        } else {
            return compare;
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
        for (Info info : infos) {
            info.count++;
        }

        // update the expiration time
        expire = new Date(System.currentTimeMillis() + TIMEOUT);

        // reschedule, if needed; if not, remove ourselves from the queue
        try {
            log.info("About to call timerQueue.reschedule");
            timerQueue.reschedule(this);
            log.info("Just called timerQueue.reschedule");
        } catch (Exception e) {
            log.info("Got an error: ", e);
        }
    }

    public String getId() {
        return id;
    }

    public ArrayList<Info> getInfos() {
        return infos;
    }

    public Date getExpire() {
        return expire;
    }

    public String getSubject() {
        return subject;
    }

    public void addReviewer(String email, String name) {
        Info newInfo = new Info(email, name);
        if (!infos.contains(newInfo)) {
            infos.add(newInfo);
        } else {
            log.warn("Reviewer " + email + " is already assigned to " + this);
        }
    }

    public void removeReviewer(String email) {
        Info newInfo = new Info(email);
        if (infos.contains(newInfo)) {
            infos.remove(newInfo);
        } else {
            log.warn("Reviewer " + email + " is not assigned to " + this);
        }
    }

    public void setTimerQueue(TimerQueue timerQueue) {
        this.timerQueue = timerQueue;
    }

    @Override
    public String toString() {
        return String.format("Task %s:" +
                        "; expire: %s" +
                        "; infos: %s",
                id, dateFormat.format(expire), infos);
    }

    class Info {
        String email;
        String name;
        int count;

        public Info(String email) {
            this.email = email;
        }

        public Info(String email, String name) {
            this.email = email;
            this.name = name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof Info)) {
                return false;
            }
            return email.equals(((Info) obj).email);
        }

        @Override
        public String toString() {
            return name + "(" + email + "): reminder #" + count;
        }
    }
}
