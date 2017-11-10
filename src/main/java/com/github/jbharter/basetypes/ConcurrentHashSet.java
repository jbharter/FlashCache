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

    private ConcurrentHashMap<T,Boolean> internal;

    public ConcurrentHashSet() {
        this.internal = new ConcurrentHashMap<>();
    }

    public ConcurrentHashSet(ConcurrentHashSet<T> set) {
        this.internal = new ConcurrentHashMap<>();
        set.forEach( thing -> this.internal.put(thing,true));
    }
    public boolean add(T t)                                 { return this.internal.put(t,true); }
    public boolean remove(Object o)                         { return this.internal.remove(o); }
    public boolean containsAll(Collection<?> c)             { return this.internal.keySet().containsAll(c); }
    public boolean addAll(Collection<? extends T> c)        { return this.internal.keySet().addAll(c); }
    public boolean equals(Object o)                         { return this.internal.equals(o); }
    public boolean retainAll(Collection<?> c)               { return this.internal.keySet().retainAll(c); }
    public boolean removeAll(Collection<?> c)               { return this.internal.keySet().removeAll(c); }
    public boolean removeIf(Predicate<? super T> filter)    { return this.internal.keySet().removeIf(filter); }
    public boolean isEmpty()                                { return this.internal.keySet().size() == 0; }
    public boolean contains(Object o)                       { return this.internal.containsKey(o); }
    public void clear()                                     { this.internal.clear(); }
    public void forEach(Consumer<? super T> action)         { new ConcurrentHashSet<>(this).parallelStream().forEach(action); }
    public Object[] toArray()                               { return this.internal.keySet().toArray(); }
    public Iterator<T> iterator()                           { return this.internal.keySet().iterator(); }
    public Spliterator<T> spliterator()                     { return this.internal.keySet().spliterator(); }
    public Stream<T> stream()                               { return this.internal.keySet().stream(); }
    public Stream<T> parallelStream()                       { return this.internal.keySet().parallelStream(); }
    public <T1> T1[] toArray(T1[] a)                        { return this.internal.keySet().toArray(a); }
    public int size()                                       { return this.internal.keySet().size(); }
    public int hashCode()                                   { return this.internal.hashCode(); }

}