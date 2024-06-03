package org.thewindsrise.faiz.base.value;

/**
 * 值接口，实现此接口的类，可以充当值容器使用，在一些场景会很有用。
 *
 * @author: wjf
 * @date: 2024/6/2
 */
public interface Value {

    @SuppressWarnings("unchecked")
    default <V> V castValue(Object value) {
        return (V) value;
    }

}
