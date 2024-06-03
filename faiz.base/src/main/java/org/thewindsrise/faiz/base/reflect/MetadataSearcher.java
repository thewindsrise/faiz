package org.thewindsrise.faiz.base.reflect;

import org.thewindsrise.faiz.base.FaizException;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 元数据搜索器，主要用于搜索某些包下的类。
 *
 * @author: wjf
 * @date: 2024/6/2
 */
@FunctionalInterface
public interface MetadataSearcher {

    /**
     * 搜索符合要求的类。
     * @return Set<Class<?>>。
     */
    Set<Class<?>> search();

    static Builder builder() {
        return new Builder();
    }

    /**
     * 元数据搜索器构造器。
     */
    class Builder {

        private final Set<String> basePackages = new LinkedHashSet<>();

        private final List<MetadataSelector> metadataSelectors = new ArrayList<>();

        private Builder() {}

        public Builder addBasePackage(final String basePackage) {
            this.basePackages.add(basePackage);
            return this;
        }

        public Builder addMetadataSelector(MetadataSelector metadataSelector) {
            this.metadataSelectors.add(metadataSelector);
            return this;
        }

        public MetadataSearcher build() {
            this.checkBeforeSearch();
            return () -> {
                Set<String> classNames = new LinkedHashSet<>();
                this.findClasses(classNames);

                if (classNames.isEmpty()) {
                    return Collections.emptySet();
                }

                List<ExcludePackage> excludePackages = new ArrayList<>();
                ExtendsSuperClass extendsSuperClasses = null;
                List<ImplementsInterfaces> implementsInterfaces = new ArrayList<>();
                List<ContainsAnnotations> containsAnnotations = new ArrayList<>();

                for (MetadataSelector metadataSelector : this.metadataSelectors) {
                    if (metadataSelector instanceof ExcludePackage excludePackage) {
                        excludePackages.add(excludePackage);
                    }
                    if (metadataSelector instanceof ExtendsSuperClass extendsSuperClass) {
                        extendsSuperClasses = extendsSuperClass;
                    }
                    if (metadataSelector instanceof ImplementsInterfaces implementsInterface) {
                        implementsInterfaces.add(implementsInterface);
                    }
                    if (metadataSelector instanceof ContainsAnnotations containsAnnotation) {
                        containsAnnotations.add(containsAnnotation);
                    }
                }

                if (!excludePackages.isEmpty()) {
                    Iterator<String> iterator = classNames.iterator();
                    while (iterator.hasNext()) {
                        String className = iterator.next();
                        for (ExcludePackage excludePackage : excludePackages) {
                            List<String> excludePackageNames = excludePackage.get();
                            for (String excludePackageName : excludePackageNames) {
                                if (className.startsWith(excludePackageName)) {
                                    iterator.remove();
                                }
                            }
                        }
                    }
                }

                if (classNames.isEmpty()) {
                    return Collections.emptySet();
                }

                Set<Class<?>> classes = new LinkedHashSet<>();
                classNames.forEach(className -> {
                   try {
                       classes.add(Class.forName(className, false, Thread.currentThread().getContextClassLoader()));
                   } catch (Exception exception) {
                       FaizException.throwFaizException(STR."\{className} load fail ---> \{exception}");
                   }
                });

                if (extendsSuperClasses != null) {
                    Iterator<Class<?>> iterator = classes.iterator();
                    while (iterator.hasNext()) {
                        Class<?> clazz = iterator.next();
                        Class<?> superClass = extendsSuperClasses.get();
                        if (!superClass.isAssignableFrom(clazz)) {
                            iterator.remove();
                        }
                    }
                }

                if (!implementsInterfaces.isEmpty()) {
                    Iterator<Class<?>> iterator = classes.iterator();
                    while (iterator.hasNext()) {
                        Class<?> clazz = iterator.next();
                        List<Class<?>> interfaces = new ArrayList<>();
                        this.findInterfacesForClass(clazz, interfaces);
                        boolean implementsAllInterfaces = true;
                        for (ImplementsInterfaces implementsInterface : implementsInterfaces) {
                            List<Class<?>> implementInterfaces = implementsInterface.get();
                            for (Class<?> implementInterface : implementInterfaces) {
                                if (!interfaces.contains(implementInterface)) {
                                    implementsAllInterfaces = false;
                                    break;
                                }
                            }
                            if (!implementsAllInterfaces) {
                                break;
                            }
                        }
                        if (!implementsAllInterfaces) {
                            iterator.remove();
                        }
                    }
                }

                if (!containsAnnotations.isEmpty()) {
                    Iterator<Class<?>> iterator = classes.iterator();
                    while (iterator.hasNext()) {
                        Class<?> clazz = iterator.next();
                        List<? extends Class<? extends Annotation>> annotationClasses = Arrays.stream(clazz.getAnnotations())
                                .map(Annotation::getClass)
                                .toList();
                        boolean containsAllAnnotations = true;
                        for (ContainsAnnotations containsAnnotation : containsAnnotations) {
                            List<Class<? extends Annotation>> containAnnotations = containsAnnotation.get();
                            for (Class<? extends Annotation> containAnnotation : containAnnotations) {
                                if (!annotationClasses.contains(containAnnotation)) {
                                    containsAllAnnotations = false;
                                    break;
                                }
                            }
                            if (!containsAllAnnotations) {
                                break;
                            }
                        }
                        if (!containsAllAnnotations) {
                            iterator.remove();
                        }
                    }
                }
                return classes;
            };
        }

        private void checkBeforeSearch() throws FaizException {
            if (this.basePackages.isEmpty()) {
                FaizException.throwFaizException("basePackages is empty");
            }
            long count = this.metadataSelectors
                    .stream()
                    .filter(metadataSelector -> metadataSelector instanceof ExtendsSuperClass)
                    .count();
            if (count > 1) {
                FaizException.throwFaizException("There can only be one ExtendsSuperClass at most");
            }
        }

        private void findInterfacesForClass(final Class<?> clazz, final List<Class<?>> interfaces) {
            if (clazz == Object.class) {
                return;
            }
            Class<?>[] clazzInterfaces = clazz.getInterfaces();
            interfaces.addAll(Arrays.asList(clazzInterfaces));
            this.findInterfacesForClass(clazz.getSuperclass(), interfaces);
        }

        private void findClasses(final Set<String> classNames) {
            this.basePackages.forEach(basePackage -> {
                String basePackagePath = basePackage.replace('.', '/');
                try {
                    Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(basePackagePath);
                    while (resources.hasMoreElements()) {
                        URL resource = resources.nextElement();
                        String protocol = resource.getProtocol();
                        if ("file".equals(protocol)) {
                            String filepath = URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8);
                            this.findClassesWithFile(new File(filepath), basePackage, classNames);
                        } else if ("jar".equals(protocol)) {
                            JarURLConnection urlConnection = (JarURLConnection) resource.openConnection();
                            try (JarFile jarFile = urlConnection.getJarFile()) {
                                this.findClassesWithJar(jarFile, basePackage, classNames);
                            }
                        }
                    }
                } catch (Exception exception) {
                    FaizException.throwFaizException(exception);
                }
            });
        }

        private void findClassesWithFile(File nodeFile, String packageName, Set<String> classNames) {
            if (!nodeFile.exists()) {
                return;
            }
            File[] files = nodeFile.listFiles();
            if (files == null) {
                return;
            }
            for (File file : files) {
                String filename = file.getName();
                if (file.isDirectory()) {
                    this.findClassesWithFile(file, STR."\{packageName}.\{file.getName()}", classNames);
                } else if (filename.endsWith(".class")) {
                    String className = STR."\{packageName}.\{filename.substring(0, file.getName().length() - 6)}";
                    classNames.add(className);
                }
            }
        }

        private void findClassesWithJar(JarFile nodeFile, String packageName, Set<String> classNames) {
            String basePackagePath = packageName.replace('.', '/');
            Enumeration<JarEntry> jarEntries = nodeFile.entries();
            while (jarEntries.hasMoreElements()) {
                JarEntry jarEntry = jarEntries.nextElement();
                String jarEntryName = jarEntry.getName();
                if (jarEntryName.startsWith(basePackagePath) && jarEntryName.endsWith(".class")) {
                    String filename = jarEntryName.replace('/', '.');
                    classNames.add(filename.substring(0, filename.length() - 6));
                }
            }
        }

    }

    /**
     * 元数据选择器，元数据搜索器需要根据选择器来进行元数据搜索。
     */
    interface MetadataSelector {}

    /**
     * 排除某个包。
     */
    interface ExcludePackage extends MetadataSelector, Supplier<List<String>> {}

    /**
     * 继承某个超类。
     */
    interface ExtendsSuperClass extends MetadataSelector, Supplier<Class<?>> {}

    /**
     * 实现某些接口。
     */
    interface ImplementsInterfaces extends MetadataSelector, Supplier<List<Class<?>>> {}

    /**
     * 添加了某些注解。
     */
    interface ContainsAnnotations extends MetadataSelector, Supplier<List<Class<? extends Annotation>>> {}

}
