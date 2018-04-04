package com.github.jbharter.caching;

public interface PurgeEvent {
    void basicPurgeEvent();
    void criticalPurgeEvent();
}
