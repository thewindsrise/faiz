package org.thewindsrise.faiz.base.value;

/**
 * 抽象值。
 *
 * @author: wjf
 * @date: 2024/6/2
 */
public abstract class AbstractValue<K> implements Value {

    /**
     * 获取值容器的值。
     * @param key 获取值的key，具体是什么含义，由实现来定义。
     * @return 值。
     * @param <V> 值。
     */
    public <V> V getValue(Object key) {
        return castValue(value(key));
    }

    /**
     * 将key具象化。
     * @param key key。
     * @return K。
     */
    protected abstract K castKey(Object key);

    /**
     * 根据key获取值。
     * @param key key。
     * @return 值。
     */
    protected abstract Object value(Object key);

}
