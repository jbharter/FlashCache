package com.github.jbharter.basetypes;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ConcurrentHashSet<T> implements Set<T>, Collection<T> {

    private Set<T> internal;

    public ConcurrentHashSet() {
        this.internal = ConcurrentHashMap.newKeySet();
    }

    public ConcurrentHashSet(ConcurrentHashSet<T> set) {
        this();
        this.internal.addAll(set);
    }
    public boolean add(T t)                                 { return this.internal.add(t); }
    public boolean remove(Object o)                         { return this.internal.remove(o); }
    public boolean containsAll(Collection<?> c)             { return this.internal.containsAll(c); }
    public boolean addAll(Collection<? extends T> c)        { return this.internal.addAll(c); }
    public boolean equals(Object o)                         { return this.internal.equals(o); }
    public boolean retainAll(Collection<?> c)               { return this.internal.retainAll(c); }
    public boolean removeAll(Collection<?> c)               { return this.internal.removeAll(c); }
    public boolean removeIf(Predicate<? super T> filter)    { return this.internal.removeIf(filter); }
    public boolean isEmpty()                                { return this.internal.size() == 0; }
    public boolean contains(Object o)                       { return this.internal.contains(o); }
    public void clear()                                     { this.internal.clear(); }
    public void forEach(Consumer<? super T> action)         { new ConcurrentHashSet<>(this).parallelStream().forEach(action); }
    public Object[] toArray()                               { return this.internal.toArray(); }
    public Iterator<T> iterator()                           { return this.internal.iterator(); }
    public Spliterator<T> spliterator()                     { return this.internal.spliterator(); }
    public Stream<T> stream()                               { return this.internal.stream(); }
    public Stream<T> parallelStream()                       { return this.internal.parallelStream(); }
    public <T1> T1[] toArray(T1[] a)                        { return this.internal.toArray(a); }
    public int size()                                       { return this.internal.size(); }
    public int hashCode()                                   { return this.internal.hashCode(); }

}