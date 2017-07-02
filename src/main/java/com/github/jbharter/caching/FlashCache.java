package com.github.jbharter.caching;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.*;
import java.util.stream.*;

public class FlashCache<K,V> implements Map<K,V> {

    private ConcurrentHashMap<K,V> internalCache;

    private AtomicLong maxNumElements;
    private AtomicLong numElements;
    private ConcurrentLinkedQueue<K> keyQueue = new ConcurrentLinkedQueue<>();
    private AtomicBoolean near = new AtomicBoolean(false);
    private int optBufferSize;
    // TODO --> manage size based on how much free memory we have

    public FlashCache() {
        internalCache   = new ConcurrentHashMap<>();
        maxNumElements  = new AtomicLong(1000000);
        numElements     = new AtomicLong(0);
        optBufferSize   = 20;
    }
    public FlashCache(Long maxElements) {
        internalCache   = new ConcurrentHashMap<>();
        maxNumElements  = new AtomicLong(maxElements);
        numElements     = new AtomicLong(0);
        optBufferSize   = 20;
    }
    public FlashCache(Long maxElements, int buffSize) {
        internalCache   = new ConcurrentHashMap<>();
        maxNumElements  = new AtomicLong(maxElements);
        numElements     = new AtomicLong(0);
        optBufferSize   = buffSize;
    }

    private void usize() { numElements.getAndSet(keyQueue.size()); }
    private void purge(int num){
        if (keyQueue.size() > num)
            for (int i = 0; i < num; ++i) internalCache.remove(keyQueue.poll());
        else {
            internalCache.clear();
            keyQueue.clear();
        }
    }
    private V internalRemove(K key) {
        keyQueue.remove(key);
        usize();
        return internalCache.remove(key);
    }

    // Map interface
    public int size() {
        numElements.getAndSet(internalCache.size());
        return numElements.intValue();
    }
    public boolean isEmpty() { return internalCache.isEmpty(); }
    public boolean containsKey(Object key) { return internalCache.containsKey(key); }
    public boolean containsValue(Object value) { return internalCache.containsValue(value); }
    public V get(Object key) { return internalCache.get(key); }
    public Set<K> keySet() { return internalCache.keySet(); }
    public Collection<V> values() { return internalCache.values(); }
    public Set<Entry<K, V>> entrySet() { return internalCache.entrySet(); }
    public void clear() {
        internalCache.clear();
        keyQueue.clear();
        usize();
    }
    public V remove(Object key) { return (key != null) ? internalRemove((K)key) : null; }
    public V put(K key, V value) {
        if (!near.get()) {
            near.getAndSet(maxNumElements.get() - optBufferSize > numElements.incrementAndGet());
            return internalCache.put(key,value);
        } else if (maxNumElements.get() > numElements.get()) {
            numElements.incrementAndGet();
            return internalCache.put(key,value);
        } else {
            purge(optBufferSize);
            return put(key,value);
        }
    }
    public V put(K key, Function<K,V> mapper) { return put(key,mapper.apply(key)); }
    public void putAll(Map<? extends K, ? extends V> m) {
        if (m.size() < maxNumElements.get()) {
            internalCache.putAll(m);
        } else {
            Long added = m.entrySet().parallelStream().map(entry -> put(entry.getKey(),entry.getValue())).count();
            if (added == m.size()) near.getAndSet(m.size() - optBufferSize > numElements.get());
        }
    }

    // Streams
    public Stream<Entry<K,V>> stream()          { return internalCache.entrySet().stream(); }
    public Stream<Entry<K,V>> parallelStream()  { return internalCache.entrySet().parallelStream(); }

    // Stream Helpers
    public Stream<Entry<K,V>> keysWhere    (Predicate<? super K> predicate)                                                     { return parallelStream().filter(each -> predicate.test(each.getKey())); }
    public Stream<Entry<K,V>> valsWhere    (Predicate<? super V> predicate)                                                     { return parallelStream().filter(each -> predicate.test(each.getValue())); }
    public Stream<Entry<K,V>> entriesOr    (Predicate<? super K> keyPred,Predicate<? super V> valPred)                          { return parallelStream().filter(each -> keyPred.test(each.getKey()) || valPred.test(each.getValue())); }
    public Stream<Entry<K,V>> entriesWhere (Predicate<? super Entry<K,V>> predicate)                                            { return parallelStream().filter(predicate); }

    // Functional Interface
    public LongSupplier                                         count = ()          -> internalCache.entrySet().size();
    public Function<Predicate<? super Entry<K,V>>,Boolean>   anyMatch = predicate   -> internalCache.entrySet().parallelStream().anyMatch(predicate);
    public Function<Predicate<? super Entry<K,V>>,Boolean>   allMatch = predicate   -> internalCache.entrySet().parallelStream().allMatch(predicate);
    public Function<Predicate<? super Entry<K,V>>,Boolean>  noneMatch = predicate   -> internalCache.entrySet().parallelStream().noneMatch(predicate);
    public Consumer<Consumer <? super Entry<K,V>>>            forEach = action      -> internalCache.entrySet().forEach(action);
    public Consumer<Consumer <? super Entry<K,V>>>     forEachOrdered = action      -> internalCache.entrySet().forEach(action);

    // Stream Interface
    public <R> Stream<R>            map(Function<? super Entry<K, V>, ? extends R> mapper)                                                  { return internalCache.entrySet().parallelStream().map(mapper); }
    public <R> Stream<R>        flatMap(Function<? super Entry<K, V>, ? extends Stream<? extends R>> mapper)                                { return internalCache.entrySet().parallelStream().flatMap(mapper); }
    public IntStream           mapToInt(ToIntFunction<? super Entry<K, V>> mapper)                                                          { return internalCache.entrySet().parallelStream().mapToInt(mapper); }
    public IntStream       flatMapToInt(Function<? super Entry<K, V>, ? extends IntStream> mapper)                                          { return internalCache.entrySet().parallelStream().flatMapToInt(mapper); }
    public LongStream         mapToLong(ToLongFunction<? super Entry<K, V>> mapper)                                                         { return internalCache.entrySet().parallelStream().mapToLong(mapper); }
    public LongStream     flatMapToLong(Function<? super Entry<K, V>, ? extends LongStream> mapper)                                         { return internalCache.entrySet().parallelStream().flatMapToLong(mapper); }
    public DoubleStream     mapToDouble(ToDoubleFunction<? super Entry<K, V>> mapper)                                                       { return internalCache.entrySet().parallelStream().mapToDouble(mapper); }
    public DoubleStream flatMapToDouble(Function<? super Entry<K, V>, ? extends DoubleStream> mapper)                                       { return internalCache.entrySet().parallelStream().flatMapToDouble(mapper); }
    public Stream<Entry<K, V>>   filter(Predicate<? super Entry<K, V>> predicate)                                                           { return internalCache.entrySet().parallelStream().filter(predicate); }
    public Stream<Entry<K, V>> distinct()                                                                                                   { return internalCache.entrySet().parallelStream().distinct(); }
    public Stream<Entry<K, V>>   sorted()                                                                                                   { return internalCache.entrySet().parallelStream().sorted(); }
    public Stream<Entry<K, V>>   sorted(Comparator<? super Entry<K, V>> comparator)                                                         { return internalCache.entrySet().parallelStream().sorted(comparator); }
    public Stream<Entry<K, V>>     peek(Consumer<? super Entry<K, V>> action)                                                               { return internalCache.entrySet().parallelStream().peek(action); }
    public Stream<Entry<K, V>>    limit(long maxSize)                                                                                       { return internalCache.entrySet().parallelStream().limit(maxSize); }
    public Stream<Entry<K, V>>     skip(long n)                                                                                             { return internalCache.entrySet().parallelStream().skip(n); }
    public Object[]             toArray()                                                                                                   { return internalCache.entrySet().toArray(); }
    public <A> A[]              toArray(IntFunction<A[]> generator)                                                                         { return internalCache.entrySet().stream().toArray(generator); }
    public <R> R                collect(Supplier<R> supplier, BiConsumer<R, ? super Entry<K, V>> accumulator, BiConsumer<R, R> combiner)    { return internalCache.entrySet().parallelStream().collect(supplier, accumulator, combiner); }
    public <R, A> R             collect(Collector<? super Entry<K, V>, A, R> collector)                                                     { return internalCache.entrySet().parallelStream().collect(collector); }
    public Entry<K, V>           reduce(Entry<K, V> identity, BinaryOperator<Entry<K, V>> accumulator)                                      { return internalCache.entrySet().parallelStream().reduce(identity,accumulator); }
    public <U> U                 reduce(U identity, BiFunction<U, ? super Entry<K, V>, U> accumulator, BinaryOperator<U> combiner)          { return internalCache.entrySet().parallelStream().reduce(identity, accumulator, combiner); }
    public Optional<Entry<K, V>> reduce(BinaryOperator<Entry<K, V>> accumulator)                                                            { return internalCache.entrySet().parallelStream().reduce(accumulator); }
    public Optional<Entry<K, V>>    min(Comparator<? super Entry<K, V>> comparator)                                                         { return internalCache.entrySet().parallelStream().min(comparator); }
    public Optional<Entry<K, V>>    max(Comparator<? super Entry<K, V>> comparator)                                                         { return internalCache.entrySet().parallelStream().max(comparator); }
    public long                   count()                                                                                                   { return internalCache.entrySet().parallelStream().count(); }
    public boolean             anyMatch(Predicate<? super Entry<K, V>> predicate)                                                           { return internalCache.entrySet().parallelStream().anyMatch(predicate); }
    public boolean             allMatch(Predicate<? super Entry<K, V>> predicate)                                                           { return internalCache.entrySet().parallelStream().allMatch(predicate); }
    public boolean            noneMatch(Predicate<? super Entry<K, V>> predicate)                                                           { return internalCache.entrySet().parallelStream().noneMatch(predicate); }
    public void                 forEach(Consumer<? super Entry<K, V>> action)                                                               { internalCache.entrySet().forEach(action); }
    public void          forEachOrdered(Consumer<? super Entry<K, V>> action)                                                               { internalCache.entrySet().stream().sorted().forEach(action); }
}
