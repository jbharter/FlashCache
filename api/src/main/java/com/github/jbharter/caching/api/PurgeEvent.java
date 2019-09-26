package com.github.jbharter.caching.api;

public interface PurgeEvent {
    void basicPurgeEvent();
    void criticalPurgeEvent();
}
