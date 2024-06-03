package org.thewindsrise.faiz.base.value;

import org.thewindsrise.faiz.base.Args;

import java.util.Objects;

/**
 * 数组类型的值。K：数组的索引值。
 *
 * @author: wjf
 * @date: 2024/6/2
 */
public class ArrayValue extends AbstractValue<Integer> implements Args {

    private final Object[] values;

    private ArrayValue(final Object[] values) {
        this.values = Objects.requireNonNull(values);
    }

    public static ArrayValue of(final Object... values) {
        return new ArrayValue(values);
    }

    @Override
    public <V> V getValue(Object index) {
        return super.getValue(index);
    }

    @Override
    protected Integer castKey(Object key) {
        return Integer.valueOf(key.toString());
    }

    @Override
    protected Object value(Object key) {
        return values[castKey(key)];
    }
}
