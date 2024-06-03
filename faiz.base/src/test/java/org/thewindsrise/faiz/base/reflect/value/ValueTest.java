package org.thewindsrise.faiz.base.reflect.value;

import org.junit.Test;
import org.thewindsrise.faiz.base.value.AbstractValue;
import org.thewindsrise.faiz.base.value.ArrayValue;
import org.thewindsrise.faiz.base.value.MapValue;

/**
 * @author: wjf
 * @date: 2024/6/2
 */
public class ValueTest {

    @Test
    public void arrayValue() {
        AbstractValue<Integer> abstractValue = ArrayValue.of(1, 2, "3");
        String value = abstractValue.getValue(2);
        System.out.println(value);
    }

    @Test
    public void mapValue() {
        MapValue mapValue = MapValue.create();
        mapValue.put("name", "wjf");
        String value = mapValue.getValue("name");
        System.out.println(value);
    }

}
