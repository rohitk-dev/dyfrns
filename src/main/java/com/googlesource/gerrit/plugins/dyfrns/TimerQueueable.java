package com.googlesource.gerrit.plugins.dyfrns;

public interface TimerQueueable {

    void add(TimerEvent event) throws Exception;

    void cancel(TimerEvent event) throws Exception;

    void reschedule(TimerEvent event) throws Exception;
}
