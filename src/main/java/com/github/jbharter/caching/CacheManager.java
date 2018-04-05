package com.github.jbharter.caching;

// Cache manager (PGP Signing Test)
//  - Keeps track of active caches
//  - Monitors approximate memory usage of each cache
//  - Will ask caches to reduce their size

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Supplier;

public class CacheManager {

    private volatile static CacheManager _instance;

    private Collection<Cache> cacheCollection = new CopyOnWriteArraySet<>();

    private Timer timer;

    private Double threshold = 0.7;
    private Long cleanupDelay = 5000L;
    private Long cleanupPeriod = 5000L;


    // Memory Monitoring
    Supplier<Long> heapSize = () -> Runtime.getRuntime().totalMemory();
    Supplier<Long> heapMax  = () -> Runtime.getRuntime().maxMemory();
    Supplier<Long> heapFree = () -> Runtime.getRuntime().freeMemory();
    Supplier<Long> heapUsed = () -> heapSize.get() - heapFree.get();
    Supplier<Long> heapRatio = () -> heapFree.get()/heapSize.get();

    Supplier<Double> maxPressure = () -> 1.0 - heapFree.get()/heapMax.get();
    Supplier<Double> memPressure = () -> 1.0 - heapFree.get()/heapSize.get();

    private CacheManager() {
        timer = new Timer("cache-manager",false);

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                // Observe and report
                Double pressure = memPressure.get();
                System.out.println("Pressure: " + pressure);
                if (pressure > threshold) {
                    System.out.println("Pressure is above threshold.");

                    if (maxPressure.get() > threshold) { //critical

                        cacheCollection.forEach(PurgeRequest::basicPurgeEvent);
                    } else {
                        cacheCollection.forEach(PurgeRequest::criticalPurgeEvent);
                    }
                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, cleanupDelay, cleanupPeriod);
    }

    public static CacheManager getInstance() {
        if (_instance == null) {
            synchronized (CacheManager.class) {
                if (_instance == null) {
                    _instance = new CacheManager();
                }
            }
        }
        return _instance;
    }

    static double getMemPressure()            { return 1.0 - (Runtime.getRuntime().freeMemory()/(double)Runtime.getRuntime().totalMemory()); }


    void registerCache(Cache c) {
        getInstance().cacheCollection.add(c);
    }







}
