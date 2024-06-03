package org.thewindsrise.faiz.base.reflect;

import java.util.List;

/**
 * 原始数据类型相关的操作。
 *
 * @author: wjf
 * @date: 2024/4/6
 */
public final class Primitive {

    private static final List<Class<?>> primitives = List.of(
            byte.class,
            short.class,
            int.class,
            long.class,
            float.class,
            double.class,
            char.class,
            boolean.class,
            void.class
    );

    private static final List<Class<?>> wrappers = List.of(
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            Character.class,
            Boolean.class,
            Void.class
    );

    private Primitive() {}

    /**
     * 是否是原始类。
     * @param clazz 被检查的class。
     * @return 是否是原始类。
     */
    public static boolean isPrimitive(final Class<?> clazz) {
        return primitives.contains(clazz);
    }

    /**
     * 是否是原始类对应的包装类。
     * @param clazz 被检查的class。
     * @return 是否是原始类对应的包装类。
     */
    public static boolean isWrapper(final Class<?> clazz) {
        return wrappers.contains(clazz);
    }

    /**
     * 获取原始类对应的包装类。
     * @param primitiveClass 原始类。
     * @return 包装类。
     */
    public static Class<?> wrap(final Class<?> primitiveClass) {
        return wrappers.get(primitives.indexOf(primitiveClass));
    }

    /**
     * 获取包装类对应的原始类。
     * @param wrapperClass 包装类。
     * @return 原始类。
     */
    public static Class<?> unwrap(final Class<?> wrapperClass) {
        return primitives.get(wrappers.indexOf(wrapperClass));
    }

}
