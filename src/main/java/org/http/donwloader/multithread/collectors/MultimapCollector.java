package org.http.donwloader.multithread.collectors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class MultimapCollector<T, K, V> implements Collector<T, Multimap<K, V>, Multimap<K, V>> {
    private final Function<? super T, ? extends K> keyMapper;
    private final Function<? super T, ? extends V> valueMapper;

    private MultimapCollector(Function<? super T, ? extends K> keyMapper,
                              Function<? super T, ? extends V> valueMapper) {

        this.keyMapper = keyMapper;
        this.valueMapper = valueMapper;
    }

    public static <T, K, V> MultimapCollector<T, K, V> toMultimap(Function<? super T, ? extends K> keyMapper,
                                                                  Function<? super T, ? extends V> valueMapper) {
        return new MultimapCollector<>(keyMapper, valueMapper);
    }

    @Override
    public Supplier<Multimap<K, V>> supplier() {
        return ArrayListMultimap::create;
    }

    @Override
    public BiConsumer<Multimap<K, V>, T> accumulator() {
        return (kvMultimap, t) -> kvMultimap.put(keyMapper.apply(t), valueMapper.apply(t));
    }

    @Override
    public BinaryOperator<Multimap<K, V>> combiner() {
        return (kvMultimap, kvMultimap2) -> {
            kvMultimap.putAll(kvMultimap2);
            return kvMultimap;
        };
    }

    @Override
    public Function<Multimap<K, V>, Multimap<K, V>> finisher() {
        return kvMultimap -> kvMultimap;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return ImmutableSet.of(Characteristics.IDENTITY_FINISH);
    }
}
