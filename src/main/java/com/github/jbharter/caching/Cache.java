package com.github.jbharter.caching;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface Cache<K,V> extends PurgeEvent {

    // Management
    void purge();
    void purge(Long purgeDepth);
    void clear();

    Map<K,V> getCache();

    // Query
    V get(K key);
    V getOrDefault(K key, V defaultValue);

    Long size();
    boolean isEmpty();
    boolean containsKey(K key);
    boolean containsValue(V value);

    Set<K> keySet();
    Stream<K> keysStream();
    Stream<K> keysWhere(Predicate<? super K> predicate);

    Collection<V> values();
    Stream<V> valuesStream();
    Stream<V> valuesWhere(Predicate<? super V> predicate);

    Set<AbstractMap.Entry<K, V>> entrySet();
    Stream<AbstractMap.Entry<K,V>> entryStream();
    Stream<AbstractMap.Entry<K,V>> entriesWhere(Predicate<? super Map.Entry<K,V>> predicate);

    // Modify
    V remove(K key);
}
