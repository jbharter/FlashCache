package com.github.jbharter.caching;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class BaseCache<K,V> implements Cache<K,V>, PurgeRequest {

    protected AtomicLong                      purgeStep;
    protected AtomicLong                      upperBound;
    protected AtomicLong                      meanMemberSize;
    private Map<K,V>                          cache;
    private Function<? super K,? extends V>   mapper;

    BaseCache() {
        this(DEFAULT_UPPER_BOUND,DEFAULT_PURGE_STEP);
    }

    BaseCache(long maxElements) {
        this(maxElements,DEFAULT_PURGE_STEP);
    }

    BaseCache(long maxElements, long purgeStepSize) {
        this(maxElements, purgeStepSize, ConcurrentHashMap::new);
    }

    BaseCache(long maxElements, long purgeStepSize, Supplier<Map<K,V>> mapImplementation) {
        // caches should always register themselves
        CacheManager.getInstance().registerCache(this);

        this.purgeStep = new AtomicLong(maxElements);
        this.upperBound = new AtomicLong(purgeStepSize);
        this.meanMemberSize = new AtomicLong(0);

        this.cache = mapImplementation.get();
    }

//    boolean notfull() { return upperBound.get() > cache.size(); }

    @Override
    public Map<K,V> getCache() {
        return cache;
    }

    @Override
    public Function<? super K, ? extends V> getCacheMappingFunction() {
        return mapper;
    }

    @Override
    public void setCacheMappingFunction(Function<? super K, ? extends V> mappingFunction) {
        this.mapper = mappingFunction;
    }

    K keyCast(Object o) {
        return (K) o;
    }

    protected void purge() { purge(purgeStep.intValue()); }
    protected abstract void purge(int purgeDepth);


}
