package org.thewindsrise.faiz.base.reflect;

import org.junit.Test;

import java.util.Set;

/**
 * @author: wjf
 * @date: 2024/6/2
 */
public class MetadataSearcherTest {

    @Test
    public void search() {
        MetadataSearcher metadataSearcher = MetadataSearcher.builder()
                .addBasePackage("org.thewindsrise")
                .addBasePackage("io.netty")
                .build();
        Set<Class<?>> searched = metadataSearcher.search();

        for (Class<?> aClass : searched) {
            System.out.println(aClass.getTypeName());
        }
    }

}
