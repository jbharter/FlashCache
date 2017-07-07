package com.github.jbharter.caching;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import java.util.stream.*;

public abstract class BaseCache<K,V> implements Map<K,V> {
    // Parent Class members
    private static double doubleMemPressureMax = 0.7;
    // Store instances and management functions
    static ConcurrentHashMap<BaseCache,Function> instanceSet = new ConcurrentHashMap<>();
    static int numInstances() { return instanceSet.size(); }
    static Long numEntries() { return instanceSet.keySet().parallelStream().mapToLong(BaseCache::size).sum(); }
    static Long usedMem () {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }
    static double memPressure () {
        return 1.0 - (Runtime.getRuntime().freeMemory()/(double)Runtime.getRuntime().totalMemory());
    }
    public static void setMemPressureMax(double max) {
        if (max > 0 && max < 1) {
            doubleMemPressureMax = max;
        }
    }

    // Instance Members
    ConcurrentHashMap<K,V> internalCache;
    Long meanMemberSize = 0L;


    // Instance methods
    abstract V internalRemove(K key);
    abstract V internalPut(K key, V val);
    abstract V internalPut(K key, Function<? super K,? extends V> mapper);
    abstract void internalPutAll(Map<? extends K, ? extends V> m);
    abstract void purge();
    abstract void purge(int num);
    abstract void purgeHard(int num);
    abstract Long getMeanMemberSize();
    public abstract void finalize();

    protected ConcurrentHashMap<K,V> getInternalCache() { return internalCache; }

    protected void makeWay() {
        if(memPressure() > doubleMemPressureMax) {
            instanceSet.entrySet().stream().filter(each -> each.getKey().getMeanMemberSize() != null).sorted((left,right) -> (left.getKey().getMeanMemberSize() > right.getKey().getMeanMemberSize()) ? 1 : -1).forEach(sortedOnSize -> {
                if (memPressure() > doubleMemPressureMax) sortedOnSize.getValue().apply(20);
            });
            makeWay(0);
        }
    }
    private void makeWay(int numIter) {
        if(memPressure() > doubleMemPressureMax && numIter < 20) {
            instanceSet.entrySet().stream().sorted((left,right) -> (left.getKey().getMeanMemberSize() > right.getKey().getMeanMemberSize()) ? 1 : -1).forEach(sortedOnSize -> {
                if (memPressure() > doubleMemPressureMax) sortedOnSize.getValue().apply(20);
            });
            makeWay(++numIter);
        }
        if (numIter == 20) { instanceSet.keySet().forEach(BaseCache::clear); } // The nuclear option
    }

    // Streams && Helpers
    public Stream<Map.Entry<K,V>> stream()                                                                   { return internalCache.entrySet().stream(); }
    public Stream<Map.Entry<K,V>> parallelStream()                                                           { return internalCache.entrySet().parallelStream(); }
    public Stream<Map.Entry<K,V>> keysWhere    (Predicate<? super K> predicate)                              { return parallelStream().filter(each -> predicate.test(each.getKey())); }
    public Stream<Map.Entry<K,V>> valsWhere    (Predicate<? super V> predicate)                              { return parallelStream().filter(each -> predicate.test(each.getValue())); }
    public Stream<Map.Entry<K,V>> entriesOr    (Predicate<? super K> keyPred, Predicate<? super V> valPred)  { return parallelStream().filter(each -> keyPred.test(each.getKey()) || valPred.test(each.getValue())); }
    public Stream<Map.Entry<K,V>> entriesWhere (Predicate<? super Map.Entry<K,V>> predicate)                 { return parallelStream().filter(predicate); }

    // Functional Interface
    public LongSupplier                                          count          = ()        -> internalCache.entrySet().size();
    public Function<Predicate<? super Map.Entry<K,V>>,Boolean>   anyMatch       = predicate -> internalCache.entrySet().parallelStream().anyMatch(predicate);
    public Function<Predicate<? super Map.Entry<K,V>>,Boolean>   allMatch       = predicate -> internalCache.entrySet().parallelStream().allMatch(predicate);
    public Function<Predicate<? super Map.Entry<K,V>>,Boolean>   noneMatch      = predicate -> internalCache.entrySet().parallelStream().noneMatch(predicate);
    public Consumer<Consumer<? super Map.Entry<K,V>>>            forEach        = action    -> internalCache.entrySet().forEach(action);
    public Consumer<Consumer <? super Map.Entry<K,V>>>           forEachOrdered = action    -> internalCache.entrySet().forEach(action);

    // Map Interface
    public V put(K key, V value)                        { return internalPut(key,value); }
    public void putAll(Map<? extends K, ? extends V> m) { internalPutAll(m); }
    public int size()                                   { return internalCache.size(); }
    public boolean isEmpty()                            { return internalCache.isEmpty(); }
    public boolean containsKey(Object key)              { return internalCache.containsKey(key); }
    public boolean containsValue(Object value)          { return internalCache.containsValue(value); }
    public V get(Object key)                            { return internalCache.get(key); }
    public Set<K> keySet()                              { return internalCache.keySet(); }
    public Collection<V> values()                       { return internalCache.values(); }
    public Set<Entry<K, V>> entrySet()                  { return internalCache.entrySet(); }
    public V remove(Object key)                         { return (key != null) ? internalRemove((K)key) : null; }
    public abstract void clear();

    // Stream Interface
    public <R> Stream<R>                map(Function<? super Map.Entry<K, V>, ? extends R> mapper)                                                      { return internalCache.entrySet().parallelStream().map(mapper); }
    public <R> Stream<R>                flatMap(Function<? super Map.Entry<K, V>, ? extends Stream<? extends R>> mapper)                                { return internalCache.entrySet().parallelStream().flatMap(mapper); }
    public IntStream                    mapToInt(ToIntFunction<? super Map.Entry<K, V>> mapper)                                                         { return internalCache.entrySet().parallelStream().mapToInt(mapper); }
    public IntStream                    flatMapToInt(Function<? super Map.Entry<K, V>, ? extends IntStream> mapper)                                     { return internalCache.entrySet().parallelStream().flatMapToInt(mapper); }
    public LongStream                   mapToLong(ToLongFunction<? super Map.Entry<K, V>> mapper)                                                       { return internalCache.entrySet().parallelStream().mapToLong(mapper); }
    public LongStream                   flatMapToLong(Function<? super Map.Entry<K, V>, ? extends LongStream> mapper)                                   { return internalCache.entrySet().parallelStream().flatMapToLong(mapper); }
    public DoubleStream                 mapToDouble(ToDoubleFunction<? super Map.Entry<K, V>> mapper)                                                   { return internalCache.entrySet().parallelStream().mapToDouble(mapper); }
    public DoubleStream                 flatMapToDouble(Function<? super Map.Entry<K, V>, ? extends DoubleStream> mapper)                               { return internalCache.entrySet().parallelStream().flatMapToDouble(mapper); }
    public Stream<Map.Entry<K, V>>      filter(Predicate<? super Map.Entry<K, V>> predicate)                                                            { return internalCache.entrySet().parallelStream().filter(predicate); }
    public Stream<Map.Entry<K, V>>      distinct()                                                                                                      { return internalCache.entrySet().parallelStream().distinct(); }
    public Stream<Map.Entry<K, V>>      sorted()                                                                                                        { return internalCache.entrySet().parallelStream().sorted(); }
    public Stream<Map.Entry<K, V>>      sorted(Comparator<? super Map.Entry<K, V>> comparator)                                                          { return internalCache.entrySet().parallelStream().sorted(comparator); }
    public Stream<Map.Entry<K, V>>      peek(Consumer<? super Map.Entry<K, V>> action)                                                                  { return internalCache.entrySet().parallelStream().peek(action); }
    public Stream<Map.Entry<K, V>>      limit(long maxSize)                                                                                             { return internalCache.entrySet().parallelStream().limit(maxSize); }
    public Stream<Map.Entry<K, V>>      skip(long n)                                                                                                    { return internalCache.entrySet().parallelStream().skip(n); }
    public Object[]                     toArray()                                                                                                       { return internalCache.entrySet().toArray(); }
    public <A> A[]                      toArray(IntFunction<A[]> generator)                                                                             { return internalCache.entrySet().stream().toArray(generator); }
    public <R> R                        collect(Supplier<R> supplier, BiConsumer<R, ? super Map.Entry<K, V>> accumulator, BiConsumer<R, R> combiner)    { return internalCache.entrySet().parallelStream().collect(supplier, accumulator, combiner); }
    public <R, A> R                     collect(Collector<? super Map.Entry<K, V>, A, R> collector)                                                     { return internalCache.entrySet().parallelStream().collect(collector); }
    public Map.Entry<K, V>              reduce(Map.Entry<K, V> identity, BinaryOperator<Map.Entry<K, V>> accumulator)                                   { return internalCache.entrySet().parallelStream().reduce(identity,accumulator); }
    public <U> U                        reduce(U identity, BiFunction<U, ? super Map.Entry<K, V>, U> accumulator, BinaryOperator<U> combiner)           { return internalCache.entrySet().parallelStream().reduce(identity, accumulator, combiner); }
    public Optional<Map.Entry<K, V>>    reduce(BinaryOperator<Map.Entry<K, V>> accumulator)                                                             { return internalCache.entrySet().parallelStream().reduce(accumulator); }
    public Optional<Map.Entry<K, V>>    min(Comparator<? super Map.Entry<K, V>> comparator)                                                             { return internalCache.entrySet().parallelStream().min(comparator); }
    public Optional<Map.Entry<K, V>>    max(Comparator<? super Map.Entry<K, V>> comparator)                                                             { return internalCache.entrySet().parallelStream().max(comparator); }
    public long                         count()                                                                                                         { return internalCache.entrySet().parallelStream().count(); }
    public boolean                      anyMatch(Predicate<? super Map.Entry<K, V>> predicate)                                                          { return internalCache.entrySet().parallelStream().anyMatch(predicate); }
    public boolean                      allMatch(Predicate<? super Map.Entry<K, V>> predicate)                                                          { return internalCache.entrySet().parallelStream().allMatch(predicate); }
    public boolean                      noneMatch(Predicate<? super Map.Entry<K, V>> predicate)                                                         { return internalCache.entrySet().parallelStream().noneMatch(predicate); }
    public void                         forEach(Consumer<? super Map.Entry<K, V>> action)                                                               { internalCache.entrySet().forEach(action); }
    public void                         forEachOrdered(Consumer<? super Map.Entry<K, V>> action)                                                        { internalCache.entrySet().stream().sorted().forEach(action); }

}
