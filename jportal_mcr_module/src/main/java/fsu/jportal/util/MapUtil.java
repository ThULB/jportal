package fsu.jportal.util;

import java.util.Map;
import java.util.function.Function;

public class MapUtil<K,V> {
    private final Map<K, V> map;

    public MapUtil(Map<K, V> map) {
        this.map = map;
    }

    public <R> R getAndMap(K key, Function<V,R> mapper){
        return mapper.apply(map.get(key));
    }

    public V get(K key){
        return map.get(key);
    }
}
