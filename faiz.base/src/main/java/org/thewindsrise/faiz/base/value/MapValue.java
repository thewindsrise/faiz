package org.thewindsrise.faiz.base.value;

import org.thewindsrise.faiz.base.Args;

import java.util.HashMap;

/**
 * Map类型的值，底层为 {@link java.util.HashMap}。K：Map的key。
 *
 * @author: wjf
 * @date: 2024/6/2
 */
public class MapValue extends AbstractValue<String> implements Args {

    private final HashMap<String, Object> values;

    private MapValue() {
        this.values = new HashMap<>();
    }

    public static MapValue create() {
        return new MapValue();
    }

    public MapValue put(String key, Object value) {
        this.values.put(key, value);
        return this;
    }

    @Override
    protected String castKey(Object key) {
        return (String) key;
    }

    @Override
    protected Object value(Object key) {
        return this.values.get(castKey(key));
    }
}
