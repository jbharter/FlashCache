package com.github.jbharter.basetypes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import java.util.stream.*;

public class ConcurrentMultiMap<K,V> implements Map<K, Collection<V>> {
    private ConcurrentHashMap<K,Collection<V>> internal;

    public ConcurrentMultiMap() {
        internal = new ConcurrentHashMap<>();
    }

    public int size()                                               { return internal.values().parallelStream().mapToInt(Collection::size).sum(); }
    public int keySize()                                            { return internal.keySet().size(); }
    public boolean isEmpty()                                        { return keySize() == 0; }
    public boolean containsKey(Object key)                          { return this.internal.containsKey(key); }
    public boolean containsValue(Object value)                      { return this.internal.contains(value); }
    public boolean contains(K key, V val)                           { return this.containsKey(key) && this.get(key).contains(val);}
    public Collection<V> get(Object key)                            { return this.internal.get(key); }
    public Collection<V> put(K key, Collection<V> value)            { return this.internal.put(key,value); }
    public Collection<V> remove(Object key)                         { return this.internal.remove(key); }
    public void putAll(Map<? extends K, ? extends Collection<V>> m) { this.internal.putAll(m); }
    public void clear()                                             { this.internal.clear(); }
    public Set<K> keySet()                                          { return this.internal.keySet(); }
    public Collection<Collection<V>> values()                       { return this.internal.values(); }
    public Set<Entry<K, Collection<V>>> entrySet()                  { return this.internal.entrySet(); }
    public Stream<Entry<K,Collection<V>>> stream()                  { return this.internal.entrySet().stream(); }
    public Stream<Entry<K,Collection<V>>> parallelStream()          { return this.internal.entrySet().parallelStream(); }

    public <R> Stream<R> map(Function<? super Entry<K, Collection<V>>, ? extends R> mapper)                                             { return parallelStream().map(mapper); }
    public <R> Stream<R> flatMap(Function<? super Entry<K, Collection<V>>, ? extends Stream<? extends R>> mapper)                       { return parallelStream().flatMap(mapper); }
    public IntStream mapToInt(ToIntFunction<? super Entry<K, Collection<V>>> mapper)                                                    { return parallelStream().mapToInt(mapper); }
    public IntStream flatMapToInt(Function<? super Entry<K, Collection<V>>, ? extends IntStream> mapper)                                { return parallelStream().flatMapToInt(mapper); }
    public LongStream mapToLong(ToLongFunction<? super Entry<K, Collection<V>>> mapper)                                                 { return parallelStream().mapToLong(mapper); }
    public LongStream flatMapToLong(Function<? super Entry<K, Collection<V>>, ? extends LongStream> mapper)                             { return parallelStream().flatMapToLong(mapper); }
    public DoubleStream mapToDouble(ToDoubleFunction<? super Entry<K, Collection<V>>> mapper)                                           { return parallelStream().mapToDouble(mapper); }
    public DoubleStream flatMapToDouble(Function<? super Entry<K, Collection<V>>, ? extends DoubleStream> mapper)                       { return parallelStream().flatMapToDouble(mapper); }
    public Stream<Entry<K, Collection<V>>> filter(Predicate<? super Entry<K, Collection<V>>> predicate)                                 { return parallelStream().filter(predicate); }
    public Stream<Entry<K, Collection<V>>> distinct()                                                                                   { return parallelStream().distinct(); }
    public Stream<Entry<K, Collection<V>>> sorted()                                                                                     { return parallelStream().sorted(); }
    public Stream<Entry<K, Collection<V>>> sorted(Comparator<? super Entry<K, Collection<V>>> comparator)                               { return parallelStream().sorted(comparator); }
    public Stream<Entry<K, Collection<V>>> peek(Consumer<? super Entry<K, Collection<V>>> action)                                       { return parallelStream().peek(action); }
    public Stream<Entry<K, Collection<V>>> limit(long maxSize)                                                                          { return parallelStream().limit(maxSize); }
    public Stream<Entry<K, Collection<V>>> skip(long n)                                                                                 { return parallelStream().skip(n); }
    public Stream<Entry<K, Collection<V>>> sequential()                                                                                 { return parallelStream().sequential(); }
    public Stream<Entry<K, Collection<V>>> parallel()                                                                                   { return parallelStream().parallel(); }
    public Stream<Entry<K, Collection<V>>> unordered()                                                                                  { return parallelStream().unordered(); }
    public Stream<Entry<K, Collection<V>>> onClose(Runnable closeHandler)                                                               { return parallelStream().onClose(closeHandler); }
    public boolean anyMatch(Predicate<? super Entry<K, Collection<V>>> predicate)                                                       { return parallelStream().anyMatch(predicate); }
    public boolean allMatch(Predicate<? super Entry<K, Collection<V>>> predicate)                                                       { return parallelStream().allMatch(predicate); }
    public boolean noneMatch(Predicate<? super Entry<K, Collection<V>>> predicate)                                                      { return parallelStream().noneMatch(predicate); }
    public void forEach(Consumer<? super Entry<K, Collection<V>>> action)                                                               { parallelStream().forEach(action); }
    public void forEachOrdered(Consumer<? super Entry<K, Collection<V>>> action)                                                        { parallelStream().forEachOrdered(action); }
    public Object[] toArray()                                                                                                           { return parallelStream().toArray(); }
    public <A> A[] toArray(IntFunction<A[]> generator)                                                                                  { return parallelStream().toArray(generator); }
    public Entry<K, Collection<V>> reduce(Entry<K, Collection<V>> identity, BinaryOperator<Entry<K, Collection<V>>> accumulator)        { return parallelStream().reduce(identity,accumulator); }
    public <U> U reduce(U identity, BiFunction<U, ? super Entry<K, Collection<V>>, U> accumulator, BinaryOperator<U> combiner)          { return parallelStream().reduce(identity,accumulator,combiner); }
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super Entry<K, Collection<V>>> accumulator, BiConsumer<R, R> combiner)   { return parallelStream().collect(supplier,accumulator,combiner); }
    public <R, A> R collect(Collector<? super Entry<K, Collection<V>>, A, R> collector)                                                 { return parallelStream().collect(collector); }
    public Optional<Entry<K, Collection<V>>> reduce(BinaryOperator<Entry<K, Collection<V>>> accumulator)                                { return parallelStream().reduce(accumulator); }
    public Optional<Entry<K, Collection<V>>> min(Comparator<? super Entry<K, Collection<V>>> comparator)                                { return parallelStream().min(comparator); }
    public Optional<Entry<K, Collection<V>>> max(Comparator<? super Entry<K, Collection<V>>> comparator)                                { return parallelStream().max(comparator); }
    public Optional<Entry<K, Collection<V>>> findFirst()                                                                                { return parallelStream().findFirst(); }
    public Optional<Entry<K, Collection<V>>> findAny()                                                                                  { return parallelStream().findAny(); }
    public Iterator<Entry<K, Collection<V>>> iterator()                                                                                 { return stream().iterator(); }
    public Spliterator<Entry<K, Collection<V>>> spliterator()                                                                           { return stream().spliterator(); }
    public long count()                                                                                                                 { return parallelStream().count(); }
}