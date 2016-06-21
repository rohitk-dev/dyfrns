package com.googlesource.gerrit.plugins.dyfrns;

import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Timer;
import java.util.TimerTask;

public class TimerQueue {
    private PriorityQueue<TimerEvent> queue;
    private Timer timer;
    private boolean timerRunning;

    TimerQueue() {
        queue = new PriorityQueue<TimerEvent>();
        timer = new Timer();
    }

    private void startNewTimer(final TimerEvent event) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                event.run();
            }
        };
        timer.schedule(task, event.getExpire());
        timerRunning = true;
    }

    public synchronized void add(TimerEvent event) throws Exception {
        System.out.println("Adding " + event);
        printQueue();

        if (queue.contains(event)) {
            throw new Exception("Duplicate element");
        }

        event.setTimerQueue(this);

        queue.add(event);

        if (queue.peek().equals(event)) {
            if (timerRunning) {
                timer.cancel();
                timerRunning = false;
                timer = new Timer();
            }
            startNewTimer(event);
        }
    }

    public synchronized void cancel(TimerEvent event) throws Exception {
        System.out.println("Canceling " + event);
        printQueue();

        if (event.equals(queue.peek())) {
            event = queue.poll();
            if (timerRunning) {
                timer.cancel();
                timerRunning = false;
                timer = new Timer();
            }
            if (!queue.isEmpty()) {
                startNewTimer(queue.peek());
            }
        } else if (!queue.remove(event)) {
            throw new Exception("No such element");
        }
    }

    public synchronized void reschedule(TimerEvent event) throws Exception {
        System.out.println("Rescheduling " + event);
        printQueue();

        if (!queue.contains(event)) {
            // this event has been canceled, so we are not going to reschedule it
            return;
        }

        queue.remove(event);
        add(event);
    }

    private void printQueue() {
        System.out.println("========================");

        Iterator<TimerEvent> iterator = queue.iterator();
        boolean first = true;
        while (iterator.hasNext()) {
            if (!first) {
                System.out.println("----------------------");
            }
            System.out.println("-" + iterator.next());
            first = false;
        }

        System.out.println("========================");
    }
}
