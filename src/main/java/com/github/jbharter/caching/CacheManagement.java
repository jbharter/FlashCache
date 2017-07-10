package com.github.jbharter.caching;

import java.util.concurrent.atomic.AtomicLong;

class CacheManagement {
    private AtomicLong purgeStep;
    private AtomicLong upperBound;

    CacheManagement() {
        this.purgeStep = new AtomicLong(BaseCache.DEFAULT_PURGE_STEP);
        this.upperBound = new AtomicLong(BaseCache.DEFAULT_UPPER_BOUND);
    }

    CacheManagement(Long purgeStep, Long upperBound) {
        this.purgeStep = new AtomicLong(purgeStep);
        this.upperBound = new AtomicLong(upperBound);
    }

    public Long getPurgeStep() {
        return purgeStep.get();
    }

    public void setPurgeStep(Number purgeStep) {
        this.purgeStep.set(purgeStep.longValue());
    }

    public Long getUpperBound() {
        return upperBound.get();
    }

    public void setUpperBound(Number upperBound) {
        this.upperBound.set(upperBound.longValue());
    }

}
