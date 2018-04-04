package com.github.jbharter.caching;


import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class BaseCache<K,V> implements Cache<K,V>, PurgeEvent {
    // Parent Class members
    static final Long DEFAULT_PURGE_STEP = 50L;
    static final Long DEFAULT_UPPER_BOUND = 1000000L;
    static double doubleMemPressureMax = 0.7;
    //private static ConcurrentHashMap<BaseCache,CacheManagement> instanceSet = new ConcurrentHashMap<>();

    // Class instances and management functions
    //protected static int getNumInstances()              { return instanceSet.size(); }
    //protected static Long getNumEntries()               { return instanceSet.keySet().parallelStream().mapToLong(BaseCache::size).sum(); }
    protected static Long getUsedMem()                  { return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(); }
    protected static double getMemPressure()            { return 1.0 - (Runtime.getRuntime().freeMemory()/(double)Runtime.getRuntime().totalMemory()); }
    protected static void setMaxMemPressure(double max) { if (max > 0 && max < 1) { doubleMemPressureMax = max; } }

    // Instance Specific
    ConcurrentHashMap<K,V> internalCache;
    Long meanMemberSize = 0L;
    Function<? super K,? extends V> mapper;

    private AtomicLong purgeStep;
    private AtomicLong upperBound;

    BaseCache() {
        this(DEFAULT_UPPER_BOUND,DEFAULT_PURGE_STEP);
    }

    BaseCache(Long maxElements) {
        this(maxElements,DEFAULT_PURGE_STEP);
    }

    BaseCache(long maxElements, long purgeStepSize) {
        // caches should always register themselves
        CacheManager.getInstance().registerCache(this);

        purgeStep = new AtomicLong(maxElements);
        upperBound = new AtomicLong(purgeStepSize);
    }

    // Instance methods
    public abstract V remove(K key);
    public abstract V get(K key);

    boolean notfull() { return getUpperBound() > internalCache.size(); }

    public Map<K,V> getCache() { return internalCache; }

    public Long getUpperBound(){
        return upperBound.get();
    }
    public Long getPurgeStep() {
        return purgeStep.get();
    }

    // Streams && Helpers
    public Stream<Map.Entry<K,V>> stream()                                                                   { return internalCache.entrySet().stream(); }
    public Stream<Map.Entry<K,V>> parallelStream()                                                           { return internalCache.entrySet().parallelStream(); }
    public Stream<Map.Entry<K,V>> valsWhere    (Predicate<? super V> predicate)                              { return parallelStream().filter(each -> predicate.test(each.getValue())); }
    public Stream<Map.Entry<K,V>> entriesOr    (Predicate<? super K> keyPred, Predicate<? super V> valPred)  { return parallelStream().filter(each -> keyPred.test(each.getKey()) || valPred.test(each.getValue())); }

    // Functional Interface
//    public LongSupplier                                          count          = ()        -> internalCache.entrySet().size();
//    public Function<Predicate<? super Map.Entry<K,V>>,Boolean>   anyMatch       = predicate -> internalCache.entrySet().parallelStream().anyMatch(predicate);
//    public Function<Predicate<? super Map.Entry<K,V>>,Boolean>   allMatch       = predicate -> internalCache.entrySet().parallelStream().allMatch(predicate);
//    public Function<Predicate<? super Map.Entry<K,V>>,Boolean>   noneMatch      = predicate -> internalCache.entrySet().parallelStream().noneMatch(predicate);
//    public Consumer<Consumer<? super Map.Entry<K,V>>>            forEach        = action    -> internalCache.entrySet().forEach(action);
//    public Consumer<Consumer <? super Map.Entry<K,V>>>           forEachOrdered = action    -> internalCache.entrySet().forEach(action);

    // Map Interface (Generics that concretes shouldn't have to worry about.
    public Long size()                                  { return (long) internalCache.size(); }
    public boolean isEmpty()                            { return internalCache.isEmpty(); }
    public boolean containsKey(K key)                   { return internalCache.containsKey(key); }
    public boolean containsValue(V value)               { return internalCache.containsValue(value); }

    public Set<K> keySet()                              { return internalCache.keySet(); }
    public Stream<K> keysStream()                       { return keySet().stream(); }
    public Stream<K> keysWhere(Predicate<? super K> predicate) { return keysStream().filter(predicate); }

    public Collection<V> values()                       { return internalCache.values(); }
    public Stream<V> valuesStream()                     { return values().stream(); }
    public Stream<V> valuesWhere(Predicate<? super V> predicate) { return valuesStream().filter(predicate); }

    public Set<AbstractMap.Entry<K, V>> entrySet()      { return internalCache.entrySet(); }
    public Stream<AbstractMap.Entry<K,V>> entryStream() { return entrySet().stream(); }
    public Stream<AbstractMap.Entry<K,V>> entriesWhere(Predicate<? super Map.Entry<K,V>> predicate) { return entryStream().filter(predicate); }

}
