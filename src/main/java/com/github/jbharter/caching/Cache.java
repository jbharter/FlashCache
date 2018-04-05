package com.github.jbharter.caching;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * I mean, basically, this is just a wrapped map. We default all the mapping functions,
 * because we want default behavior to be identical to a traditional Map. The magic of
 * it is that, given a mapping function, we can poll a cache continuously, it will
 * keep itself from getting too large, while reducing the overall number of times that
 * the value has to computed.
 *
 * @param <K> Type used to key the cache
 * @param <V> Values the keys map to
 */
public interface Cache<K,V> extends PurgeRequest, Map<K,V> {

    // Cache Defaults
    long   DEFAULT_PURGE_STEP = 50L;
    long   DEFAULT_UPPER_BOUND = 1000000L;
    double DEFAULT_PRESSURE_MAX = 0.7;

    // Cache specific behaviour [Things we probably should actually override]
    /**
     *
     * @return returns the underlying KV mappings
     */
    Map<K,V> getCache();

    /**
     * Get the mapping function for the cache.
     * @return cache mapping function
     */
    Function<? super K, ? extends V> getCacheMappingFunction();

    /**
     * Set the mapping function for the cache. Allows cache to automatically compute values given just a key.
     * @param cacheMappingFunction function to apply
     */
    void setCacheMappingFunction(Function<? super K, ? extends V> cacheMappingFunction);

    /**
     * Adds entry to the cache, automatically applying the mapping function if it exists.
     * @param key key to apply the mapping function to.
     * @return the value returned by the mapper, and entered into the cache.
     */
    V put(K key);

    /**
     * Similar to the put method, this method accepts a collection of keys to map, mapping each key,
     * and entering them all into the cache.
     * @param keyCollection collection of keys to map
     */
    void putAll(Collection<? extends K> keyCollection);

    /**
     * Removes what would be the next entry to purge, and returns the value.
     * @return polled value
     */
    V poll();

    /**
     * @deprecated since 1.1 in favor of putAll()
     * @param keyCollection collection of keys
     */
    @Deprecated
    default void put(Collection<K> keyCollection) {
        putAll(keyCollection);
    }

    /**
     * @deprecated since 1.1 in favor of setCacheMappingFunction()
     * Setter for the internal cache mapping function that is applied to keys when entries are absent.
     * @param cacheMappingFunction function to apply
     */
    @Deprecated
    default void setMapper(Function<? super K, ? extends V> cacheMappingFunction) {
        setCacheMappingFunction(cacheMappingFunction);
    }

    // Map extensions
    /**
     * Convenience method to return a stream of cache keys.
     * @return stream of cache keys
     */
    default Stream<K> keysStream() {
        return keySet().stream();
    }

    /**
     * Convenience method to return a stream of cache keys that adhere to the predicate supplied.
     * @param predicate predicate to filter cache keys
     * @return stream of cache keys
     */
    default Stream<K> keysWhere(Predicate<? super K> predicate) {
        return keysStream().filter(predicate);
    }

    /**
     * Convenience method to return a stream of cache values.
     * @return stream of cache values
     */
    default Stream<V> valuesStream() {
        return values().stream();
    }

    /**
     * Convenience method to return a stream of cache values that adhere to the predicate supplied.
     * @param predicate predicate to filter cache values
     * @return stream of cache values
     */
    default Stream<V> valuesWhere(Predicate<? super V> predicate) {
        return valuesStream().filter(predicate);
    }

    /**
     * Convenience method to returns a stream of cache entries.
     * @return stream of cache entries
     */
    default Stream<AbstractMap.Entry<K,V>> entryStream() {
        return entrySet().stream();
    }

    /**
     * Convenience method to return a stream of cache entries that adhere to the predicate supplied.
     * @param predicate predicate to filter cache entries
     * @return stream of cache entries
     */
    default Stream<AbstractMap.Entry<K,V>> entriesWhere(Predicate<? super Map.Entry<K,V>> predicate) {
        return entryStream().filter(predicate);
    }

    // Map implementation defaulting
    /**
     * Returns the number of key-value mappings in this cache.  If the
     * cache contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of key-value mappings in this cache
     */
    @Override
    default int size() {
        return getCache().size();
    }

    /**
     * Returns <tt>true</tt> if this cache contains no key-value mappings.
     *
     * @return <tt>true</tt> if this cache contains no key-value mappings
     */
    @Override
    default boolean isEmpty() {
        return getCache().isEmpty();
    }

    /**
     * Returns <tt>true</tt> if this cache contains a mapping for the specified
     * key.  More formally, returns <tt>true</tt> if and only if
     * this cache contains a mapping for a key <tt>k</tt> such that
     * <tt>(key==null ? k==null : key.equals(k))</tt>.  (There can be
     * at most one such mapping.)
     *
     * @param key key whose presence in this cache is to be tested
     * @return <tt>true</tt> if this cache contains a mapping for the specified
     * key
     * @throws ClassCastException   if the key is of an inappropriate type for
     *                              this cache
     *                              (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified key is null and the underlying map implementation of the cache
     *                              does not permit null keys
     *                              (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    default boolean containsKey(Object key) {
        return getCache().containsKey(key);
    }

    /**
     * Returns <tt>true</tt> if this cache maps one or more keys to the
     * specified value.  More formally, returns <tt>true</tt> if and only if
     * this cache contains at least one mapping to a value <tt>v</tt> such that
     * <tt>(value==null ? v==null : value.equals(v))</tt>.
     *
     * @param value value whose presence in this cache is to be tested
     * @return <tt>true</tt> if this cache maps one or more keys to the
     * specified value
     * @throws ClassCastException   if the value is of an inappropriate type for
     *                              this cache
     *                              (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified value is null and the underlying map implementation of the cache
     *                              does not permit null values
     *                              (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    default boolean containsValue(Object value) {
        return getCache().containsValue(value);
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this cache contains no mapping for the key.
     *
     * <p>More formally, if this cache contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
     * key.equals(k))}, then this method returns {@code v}; otherwise
     * it returns {@code null}.  (There can be at most one such mapping.)
     *
     * <p>If this cache permits null values, then a return value of
     * {@code null} does not <i>necessarily</i> indicate that the cache
     * contains no mapping for the key; it's also possible that the cache
     * explicitly maps the key to {@code null}.  The {@link #containsKey
     * containsKey} operation may be used to distinguish these two cases.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or
     * {@code null} if this cache contains no mapping for the key
     * @throws ClassCastException   if the key is of an inappropriate type for
     *                              this cache
     *                              (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified key is null and the underlying map implementation of the cache
     *                              does not permit null keys
     *                              (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    default V get(Object key) {
        return getCache().get(key);
    }

    /**
     * Associates the specified value with the specified key in this cache
     * (optional operation).  If the cache previously contained a mapping for
     * the key, the old value is replaced by the specified value.  (A cache
     * <tt>m</tt> is said to contain a mapping for a key <tt>k</tt> if and only
     * if {@link #containsKey(Object) m.containsKey(k)} would return
     * <tt>true</tt>.)
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     * <tt>null</tt> if there was no mapping for <tt>key</tt>.
     * (A <tt>null</tt> return can also indicate that the cache
     * previously associated <tt>null</tt> with <tt>key</tt>,
     * if the implementation supports <tt>null</tt> values.)
     * @throws UnsupportedOperationException if the <tt>put</tt> operation
     *                                       is not supported by this cache
     * @throws ClassCastException            if the class of the specified key or value
     *                                       prevents it from being stored in this map
     * @throws NullPointerException          if the specified key or value is null
     *                                       and the underlying map implementation of the cache
     *                                       does not permit null keys or values
     * @throws IllegalArgumentException      if some property of the specified key
     *                                       or value prevents it from being stored in this cache
     */
    @Override
    default V put(K key, V value) {
        return getCache().put(key, value);
    }

    /**
     * Removes the mapping for a key from this cache if it is present
     * (optional operation).   More formally, if this cache contains a mapping
     * from key <tt>k</tt> to value <tt>v</tt> such that
     * <code>(key==null ?  k==null : key.equals(k))</code>, that mapping
     * is removed.  (The cache can contain at most one such mapping.)
     *
     * <p>Returns the value to which this cache previously associated the key,
     * or <tt>null</tt> if the cache contained no mapping for the key.
     *
     * <p>If this cache permits null values, then a return value of
     * <tt>null</tt> does not <i>necessarily</i> indicate that the cache
     * contained no mapping for the key; it's also possible that the cache
     * explicitly mapped the key to <tt>null</tt>.
     *
     * <p>The cache will not contain a mapping for the specified key once the
     * call returns.
     *
     * @param key key whose mapping is to be removed from the cache
     * @return the previous value associated with <tt>key</tt>, or
     * <tt>null</tt> if there was no mapping for <tt>key</tt>.
     * @throws UnsupportedOperationException if the <tt>remove</tt> operation
     *                                       is not supported by this cache
     * @throws ClassCastException            if the key is of an inappropriate type for
     *                                       this cache
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if the specified key is null and the underlying map implementation of the cache
     *                                       does not permit null keys
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    default V remove(Object key) {
        return getCache().remove(key);
    }

    /**
     * Copies all of the mappings from the specified map to this cache
     * (optional operation).  The effect of this call is equivalent to that
     * of calling {@link #put(Object, Object) put(k, v)} on this cache once
     * for each mapping from key <tt>k</tt> to value <tt>v</tt> in the
     * specified map.  The behavior of this operation is undefined if (the
     * underlying map implementation is not thread safe, and) the
     * specified map is modified while the operation is in progress.
     *
     * @param m mappings to be stored in this cache
     * @throws UnsupportedOperationException if the <tt>putAll</tt> operation
     *                                       is not supported by this cache
     * @throws ClassCastException            if the class of a key or value in the
     *                                       specified map prevents it from being stored in this cache
     * @throws NullPointerException          if the specified map is null, or if
     *                                       the underlying map implementation of the cache does not permit null keys or values, and the
     *                                       specified map contains null keys or values
     * @throws IllegalArgumentException      if some property of a key or value in
     *                                       the specified map prevents it from being stored in this cache
     */
    @Override
    default void putAll(Map<? extends K, ? extends V> m) {
        getCache().putAll(m);
    }

    /**
     * Removes all of the mappings from this cache (optional operation).
     * The cache will be empty after this call returns.
     *
     * @throws UnsupportedOperationException if the <tt>clear</tt> operation
     *                                       is not supported by this cache
     */
    @Override
    default void clear() {
        getCache().clear();
    }

    /**
     * Returns a {@link Set} view of the keys contained in this cache.
     * The set is backed by the cache, so changes to the cache are
     * reflected in the set, and vice-versa.  If the cache is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation), the results of
     * the iteration are undefined. (Unless the underlying map implementation
     * supports concurrent modification) The set supports element removal,
     * which removes the corresponding mapping from the cache, via the
     * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
     * operations.  It does not support the <tt>add</tt> or <tt>addAll</tt>
     * operations.
     *
     * @return a set view of the keys contained in this cache
     */
    @Override
    default Set<K> keySet() {
        return getCache().keySet();
    }

    /**
     * Returns a {@link Collection} view of the values contained in this cache.
     * The collection is backed by the cache, so changes to the cache are
     * reflected in the collection, and vice-versa.  If the cache is
     * modified while an iteration over the collection is in progress
     * (except through the iterator's own <tt>remove</tt> operation),
     * the results of the iteration are undefined. (Unless the underlying map implementation
     * supports concurrent modification) The collection
     * supports element removal, which removes the corresponding
     * mapping from the cache, via the <tt>Iterator.remove</tt>,
     * <tt>Collection.remove</tt>, <tt>removeAll</tt>,
     * <tt>retainAll</tt> and <tt>clear</tt> operations.  It does not
     * support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a collection view of the values contained in this cache
     */
    @Override
    default Collection<V> values() {
        return getCache().values();
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this cache.
     * The set is backed by the cache, so changes to the cache are
     * reflected in the set, and vice-versa.  If the cache is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation, or through the
     * <tt>setValue</tt> operation on a map entry returned by the
     * iterator) the results of the iteration are undefined. (Unless
     * the underlying map implementation supports concurrent modification)
     * The set supports element removal, which removes the corresponding
     * mapping from the cache, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
     * <tt>clear</tt> operations.  It does not support the
     * <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a set view of the mappings contained in this cache
     */
    @Override
    default Set<Entry<K, V>> entrySet() {
        return getCache().entrySet();
    }

    /**
     * Returns the value to which the specified key is mapped, or
     * {@code defaultValue} if this cache contains no mapping for the key.
     *
     * @param key          the key whose associated value is to be returned
     * @param defaultValue the default mapping of the key
     * @return the value to which the specified key is mapped, or
     * {@code defaultValue} if this cache contains no mapping for the key
     * @throws ClassCastException   if the key is of an inappropriate type for
     *                              this cache
     *                              (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified key is null and the underlying map implementation of the cache
     *                              does not permit null keys
     *                              (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    default V getOrDefault(Object key, V defaultValue) {
        return getCache().getOrDefault(key, defaultValue);
    }

    /**
     * Performs the given action for each entry in this cache until all entries
     * have been processed or the action throws an exception.   Unless
     * otherwise specified by the implementing class, actions are performed in
     * the order of entry set iteration (if an iteration order is specified.)
     * Exceptions thrown by the action are relayed to the caller.
     *
     * @param action The action to be performed for each entry
     * @throws NullPointerException            if the specified action is null
     * @throws ConcurrentModificationException if the underlying map backing the cache does not support concurrent
     *                                         modification, and an entry is found to be removed during iteration
     */
    @Override
    default void forEach(BiConsumer<? super K, ? super V> action) {
        getCache().forEach(action);
    }

    /**
     * Replaces each entry's value with the result of invoking the given
     * function on that entry until all entries have been processed or the
     * function throws an exception.  Exceptions thrown by the function are
     * relayed to the caller.
     *
     * @param function the function to apply to each entry
     * @throws UnsupportedOperationException   if the {@code set} operation
     *                                         is not supported by this cache's entry set iterator.
     * @throws ClassCastException              if the class of a replacement value
     *                                         prevents it from being stored in this cache
     * @throws NullPointerException            if the specified function is null, or the
     *                                         specified replacement value is null, and the backing map for this cache
     *                                         does not permit null values
     * @throws ClassCastException              if a replacement value is of an inappropriate
     *                                         type for this cache
     *                                         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException            if function or a replacement value is null, and the backing map for this
     *                                         cache does not permit null keys or values
     *                                         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws IllegalArgumentException        if some property of a replacement value
     *                                         prevents it from being stored in this cache
     *                                         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ConcurrentModificationException if the underlying map backing the cache does not support concurrent
     *                                         modification, and an entry is found to be removed during iteration
     */
    @Override
    default void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        getCache().replaceAll(function);
    }

    /**
     * If the specified key is not already associated with a value (or is mapped
     * to {@code null}) associates it with the given value and returns
     * {@code null}, else returns the current value.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or
     * {@code null} if there was no mapping for the key.
     * (A {@code null} return can also indicate that the cache
     * previously associated {@code null} with the key,
     * if the implementation supports null values.)
     * @throws UnsupportedOperationException if the {@code put} operation
     *                                       is not supported by this cache
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ClassCastException            if the key or value is of an inappropriate
     *                                       type for this cache
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if the specified key or value is null,
     *                                       and the backing map application does not permit null keys or values
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws IllegalArgumentException      if some property of the specified key
     *                                       or value prevents it from being stored in this cache
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    default V putIfAbsent(K key, V value) {
        return getCache().putIfAbsent(key, value);
    }

    /**
     * Removes the entry for the specified key only if it is currently
     * mapped to the specified value.
     *
     * @param key   key with which the specified value is associated
     * @param value value expected to be associated with the specified key
     * @return {@code true} if the value was removed
     * @throws UnsupportedOperationException if the {@code remove} operation
     *                                       is not supported by this cache
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ClassCastException            if the key or value is of an inappropriate
     *                                       type for this cache
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if the specified key or value is null,
     *                                       and the backing map does not permit null keys or values
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    default boolean remove(Object key, Object value) {
        return getCache().remove(key, value);
    }

    /**
     * Replaces the entry for the specified key only if currently
     * mapped to the specified value.
     *
     * @param key      key with which the specified value is associated
     * @param oldValue value expected to be associated with the specified key
     * @param newValue value to be associated with the specified key
     * @return {@code true} if the value was replaced
     * @throws UnsupportedOperationException if the {@code put} operation
     *                                       is not supported by this cache
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ClassCastException            if the class of a specified key or value
     *                                       prevents it from being stored in this cache
     * @throws NullPointerException          if a specified key or newValue is null,
     *                                       and the backing map for this cache does not permit null keys or values
     * @throws NullPointerException          if oldValue is null the backing map for this cache does not
     *                                       permit null values
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws IllegalArgumentException      if some property of a specified key
     *                                       or value prevents it from being stored in this cache
     */
    @Override
    default boolean replace(K key, V oldValue, V newValue) {
        return getCache().replace(key, oldValue, newValue);
    }

    /**
     * Replaces the entry for the specified key only if it is
     * currently mapped to some value.
     *
     * @param key   key with which the specified value is associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or
     * {@code null} if there was no mapping for the key.
     * (A {@code null} return can also indicate that the cache
     * previously associated {@code null} with the key,
     * if the backing map implementation supports null values.)
     * @throws UnsupportedOperationException if the {@code put} operation
     *                                       is not supported by this cache
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ClassCastException            if the class of the specified key or value
     *                                       prevents it from being stored in this cache
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if the specified key or value is null,
     *                                       and the backing map implementation for this cache does not permit null
     *                                       keys or values
     * @throws IllegalArgumentException      if some property of the specified key
     *                                       or value prevents it from being stored in this cache
     */
    @Override
    default V replace(K key, V value) {
        return getCache().replace(key, value);
    }

    /**
     * If the specified key is not already associated with a value (or is mapped
     * to {@code null}), attempts to compute its value using the given mapping
     * function and enters it into this cache unless {@code null}.
     *
     * <p>If the function returns {@code null} no mapping is recorded. If
     * the function itself throws an (unchecked) exception, the
     * exception is rethrown, and no mapping is recorded.
     *
     * @param key             key with which the specified value is to be associated
     * @param mappingFunction the function to compute a value
     * @return the current (existing or computed) value associated with
     * the specified key, or null if the computed value is null
     * @throws NullPointerException          if the specified key is null and
     *                                       the backing map implementation for this cache does not support null keys,
     *                                       or the mappingFunction is null
     * @throws UnsupportedOperationException if the {@code put} operation
     *                                       is not supported by this cache
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ClassCastException            if the class of the specified key or value
     *                                       prevents it from being stored in this cache
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    default V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return getCache().computeIfAbsent(key, mappingFunction);
    }

    /**
     * If the value for the specified key is present and non-null, attempts to
     * compute a new mapping given the key and its current mapped value.
     *
     * <p>If the function returns {@code null}, the mapping is removed.  If the
     * function itself throws an (unchecked) exception, the exception is
     * rethrown, and the current mapping is left unchanged.
     *
     * @param key               key with which the specified value is to be associated
     * @param remappingFunction the function to compute a value
     * @return the new value associated with the specified key, or null if none
     * @throws NullPointerException          if the specified key is null and
     *                                       the backing map implementation for this cache does not support null keys,
     *                                       or the remappingFunction is null
     * @throws UnsupportedOperationException if the {@code put} operation
     *                                       is not supported by this cache
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ClassCastException            if the class of the specified key or value
     *                                       prevents it from being stored in this cache
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    default V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return getCache().computeIfPresent(key, remappingFunction);
    }

    /**
     * Attempts to compute a mapping for the specified key and its current
     * mapped value (or {@code null} if there is no current mapping). For
     * example, to either create or append a {@code String} msg to a value
     * mapping:
     *
     * <pre> {@code
     * cache.compute(key, (k, v) -> (v == null) ? msg : v.concat(msg))}</pre>
     * (Method {@link #merge merge()} is often simpler to use for such purposes.)
     *
     * <p>If the function returns {@code null}, the mapping is removed (or
     * remains absent if initially absent).  If the function itself throws an
     * (unchecked) exception, the exception is rethrown, and the current mapping
     * is left unchanged.
     *
     * @param key               key with which the specified value is to be associated
     * @param remappingFunction the function to compute a value
     * @return the new value associated with the specified key, or null if none
     * @throws NullPointerException          if the specified key is null and
     *                                       the backing map implementation for this cache does not support null keys,
     *                                       or the remappingFunction is null
     * @throws UnsupportedOperationException if the {@code put} operation
     *                                       is not supported by this cache
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ClassCastException            if the class of the specified key or value
     *                                       prevents it from being stored in this cache
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    default V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return getCache().compute(key, remappingFunction);
    }

    /**
     * If the specified key is not already associated with a value or is
     * associated with null, associates it with the given non-null value.
     * Otherwise, replaces the associated value with the results of the given
     * remapping function, or removes if the result is {@code null}. This
     * method may be of use when combining multiple mapped values for a key.
     * For example, to either create or append a {@code String msg} to a
     * value mapping:
     *
     * <pre> {@code
     * cache.merge(key, msg, String::concat)
     * }</pre>
     *
     * <p>If the function returns {@code null} the mapping is removed.  If the
     * function itself throws an (unchecked) exception, the exception is
     * rethrown, and the current mapping is left unchanged.
     *
     * @param key               key with which the resulting value is to be associated
     * @param value             the non-null value to be merged with the existing value
     *                          associated with the key or, if no existing value or a null value
     *                          is associated with the key, to be associated with the key
     * @param remappingFunction the function to recompute a value if present
     * @return the new value associated with the specified key, or null if no
     * value is associated with the key
     * @throws UnsupportedOperationException if the {@code put} operation
     *                                       is not supported by this cache
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ClassCastException            if the class of the specified key or value
     *                                       prevents it from being stored in this cache
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if the specified key is null the backing map implementation for this cache
     *                                       does not support null keys or the value or remappingFunction is
     *                                       null
     */
    @Override
    default V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return getCache().merge(key, value, remappingFunction);
    }
}
