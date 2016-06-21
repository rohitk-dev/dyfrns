package com.googlesource.gerrit.plugins.dyfrns;

import java.util.Random;

public class Main {

    public static void main(String[] args) {
        Random random = new Random();
        TimerQueue queue = new TimerQueue();

        int count = 5;

        TimerEvent[] events = new TimerEvent[count];
        for (int i = 0; i < count; i++) {
            events[i] = new TimerEvent(i, new String[]{"test@test.com"});
        }

        boolean[] added = new boolean[count];

        while (true) {
            try {
                int index = random.nextInt(count);

                TimerEvent event = events[index];
                if (added[index]) {
                    queue.cancel(event);
                    added[index] = false;
                } else {
                    queue.add(event);
                    added[index] = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            try {
                Thread.sleep(random.nextInt(500));
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
