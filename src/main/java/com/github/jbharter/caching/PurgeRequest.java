package com.github.jbharter.caching;

public interface PurgeRequest {
    void basicPurgeEvent();
    void criticalPurgeEvent();
}
