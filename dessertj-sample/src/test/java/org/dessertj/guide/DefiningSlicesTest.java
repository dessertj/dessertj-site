package org.dessertj.guide;

import org.dessertj.classfile.ClassFile;
import org.dessertj.classfile.attribute.Attributes;
import org.dessertj.classfile.attribute.SourceFileAttribute;
import org.dessertj.modules.ModuleRegistry;
import org.dessertj.modules.core.ModuleSlice;
import org.dessertj.modules.fixed.JavaModules;
import org.dessertj.partitioning.ClazzPredicates;
import org.dessertj.resolve.ClassPackage;
import org.dessertj.resolve.ClassResolver;
import org.dessertj.resolve.ClassRoot;
import org.dessertj.resolve.TraversalRoot;
import org.dessertj.slicing.Classpath;
import org.dessertj.slicing.Clazz;
import org.dessertj.slicing.Root;
import org.dessertj.slicing.Slice;
import org.dessertj.util.AnnotationPattern;
import org.dessertj.util.Predicate;
import org.dessertj.util.Predicates;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apiguardian.api.API;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.JpaRepositoryConfigExtension;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;

import java.lang.invoke.MethodHandles;

import static org.dessertj.util.AnnotationPattern.member;
import static org.assertj.core.api.Assertions.assertThat;

public class DefiningSlicesTest {
    private static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());
    private static final Classpath cp = new Classpath();
    private static final ModuleRegistry mr = new ModuleRegistry(cp);
    private static final JavaModules java = new JavaModules(mr);

    private final ModuleSlice junit = mr.getModule("org.junit.jupiter.api");
    private static final Root dessert = cp.rootOf(Slice.class);
    private static final Slice spring = cp.slice("org.springframework..*");

    @Test
    void operations() {
        Slice slice1 = cp.sliceOf(ClassPackage.class, ClassResolver.class, ClassRoot.class);
        Slice slice2 = cp.sliceOf(ClassRoot.class, TraversalRoot.class);

        // tag::union[]
        Slice union = slice1.plus(slice2);
        // end::union[]
        // tag::intersection[]
        Slice intersection = slice1.slice(slice2);
        // end::intersection[]
        // tag::difference[]
        Slice difference = slice1.minus(slice2);
        // end::difference[]

        assertThat(union.getClazzes()).hasSize(4)
                .containsOnlyOnceElementsOf(cp.sliceOf(ClassPackage.class, ClassResolver.class, ClassRoot.class,
                        TraversalRoot.class).getClazzes());
        assertThat(intersection.getClazzes()).hasSize(1)
                .containsOnlyOnceElementsOf(cp.sliceOf(ClassRoot.class).getClazzes());
        assertThat(difference.getClazzes()).hasSize(2)
                .containsOnlyOnceElementsOf(cp.sliceOf(ClassPackage.class, ClassResolver.class).getClazzes());
    }

    @Test
    void patterns() {
        // tag::patterns[]
        // All classes within org.springframework
        Slice spring = cp.slice("org.springframework..*");

        // All classes with name ending to 'Impl'
        Slice impl = spring.slice("..*Impl");

        // All classes that contain 'Service' within their name
        Slice service = spring.slice("..*Service*");

        // All classes in any internal package
        Slice internal = spring.slice("..internal..*");

        // All classes with the 3rd package named 'core', in this case
        // these are all classes within org.springframework.core
        Slice core = spring.slice("*.*.core..*");

        // A sample for a more complex pattern
        Slice complex = cp.slice("..*frame*|hiber*..schema|codec..*Impl");
        // end::patterns[]

        assertThat(impl.getClazzes()).isNotEmpty();
        assertThat(service.getClazzes()).isNotEmpty();
        assertThat(internal.getClazzes()).isNotEmpty();
        assertThat(core.getClazzes()).isNotEmpty();
        assertThat(complex.getClazzes()).isNotEmpty();
    }

    @Test
    void predicates() {
        // tag::predicates[]
        // All interfaces
        Slice interfaces = spring.slice(ClazzPredicates.INTERFACE);

        // All deprecated classes
        Slice deprecated = spring.slice(ClazzPredicates.DEPRECATED);

        // All final public classes
        Slice finalpublic = spring.slice(Predicates.and(ClazzPredicates.FINAL, ClazzPredicates.PUBLIC));

        // All classes that implement InitializingBean directly
        Slice implementsDirectly = spring.slice(ClazzPredicates.implementsInterface(InitializingBean.class.getName()));

        // All classes that implement Slice or another interface that extends Slice
        // or that extend such a super-class
        Slice implementsRecursive = dessert.slice(ClazzPredicates.matches(Slice.class::isAssignableFrom));

        // All classes that's simple name matches a regex-pattern
        Slice regex = spring.slice(ClazzPredicates.matchesSimpleName(".*[Ss]ervice.*"));

        // All classes located in a META-INF/versions/9 directory
        Slice version = cp.slice(clazz -> Integer.valueOf(9).equals(clazz.getVersion()));

        // All classes throwing a NoClassDefFoundError when trying to load
        Slice nodef = spring.slice(this::causesNoClassDefFoundError);

        // All classes that have a SourceFileAttribute
        Predicate<ClassFile> hasSourceFile = cf ->
                !Attributes.filter(cf.getAttributes(), SourceFileAttribute.class).isEmpty();
        Slice source = spring.slice(ClazzPredicates.matchesClassFile(hasSourceFile));

        // Some complex predicate using Predicates' combinator logic
        Predicate<Clazz> complexPredicate = Predicates.or(
                Predicates.and(ClazzPredicates.ABSTRACT, Predicates.not(ClazzPredicates.INNER_TYPE)),
                ClazzPredicates.ENUM
        );
        Slice complex = spring.slice(complexPredicate);
        // end::predicates[]

        assertThat(interfaces.getClazzes()).isNotEmpty();
        assertThat(deprecated.getClazzes()).isNotEmpty();
        assertThat(finalpublic.getClazzes()).isNotEmpty();
        assertThat(implementsDirectly.getClazzes()).isNotEmpty();
        assertThat(implementsRecursive.getClazzes()).isNotEmpty();
        assertThat(regex.getClazzes()).isNotEmpty();
        assertThat(version.getClazzes()).isNotEmpty();
        assertThat(nodef.getClazzes()).isNotEmpty();
        assertThat(source.getClazzes()).isNotEmpty();
        assertThat(complex.getClazzes()).isNotEmpty();
    }

    // tag::nodef[]
    private boolean causesNoClassDefFoundError(Clazz clazz) {
        try {
            clazz.getClassImpl();
            return false;
        } catch (NoClassDefFoundError er) {
            return true;
        } catch (Throwable th) {
            log.info("{} caused: {}", clazz.getName(), th);
            return false;
        }
    }
    // end::nodef[]

    // tag::dump[]
    private void dump(Slice slice) {
        slice.getClazzes().stream()
                .map(Clazz::getName)
                .sorted()
                .forEach(System.out::println);
    }
    // end::dump[]


    @Test
    void annotations() {
        // tag::annotations[]
        // classes annotated with @Configuration
        Slice config = spring.slice(ClazzPredicates.matchesAnnotation(
                AnnotationPattern.of(Configuration.class)));

        // classes that have methods annotated with @Bean
        Slice beans = spring.slice(ClazzPredicates.matchesAnnotation(
                AnnotationPattern.of(Bean.class)));

        // classes that have methods or fields annotated with @Autowired
        Slice autowired = spring.slice(ClazzPredicates.matchesAnnotation(
                AnnotationPattern.of(Autowired.class)));

        // classes that have methods or fields annotated with @Autowired(required = false)
        Slice autowiredOptional = spring.slice(ClazzPredicates.matchesAnnotation(
                AnnotationPattern.of(Autowired.class,
                        AnnotationPattern.member("required", false))));

        // classes annotated with @ConditionalOnMissingBean({JpaRepositoryFactoryBean.class, JpaRepositoryConfigExtension.class})
        Slice conditional = spring.slice(ClazzPredicates.matchesAnnotation(
                AnnotationPattern.of(ConditionalOnMissingBean.class,
                        member("value", JpaRepositoryFactoryBean.class, JpaRepositoryConfigExtension.class))));

        // a complex annotation pattern which matches to:
        //    @ComponentScan(
        //            excludeFilters = {@ComponentScan.Filter(
        //                    type = FilterType.CUSTOM,
        //                    classes = {TypeExcludeFilter.class}
        //            ), @ComponentScan.Filter(
        //                    type = FilterType.CUSTOM,
        //                    classes = {AutoConfigurationExcludeFilter.class}
        //            )}
        //    )
        Slice scan = spring.slice(ClazzPredicates.matchesAnnotation(
                AnnotationPattern.of(ComponentScan.class,
                        member("excludeFilters",
                                AnnotationPattern.of(ComponentScan.Filter.class,
                                        member("type", FilterType.CUSTOM),
                                        member("classes", TypeExcludeFilter.class)
                                ),
                                AnnotationPattern.of(ComponentScan.Filter.class)
                        ))
        ));

        // the experimental junit classes
        Slice experimental = junit.slice(ClazzPredicates.matchesAnnotation(
                AnnotationPattern.of(API.class, member("status", API.Status.EXPERIMENTAL))
        ));
        // end::annotations[]

        assertThat(config.getClazzes()).isNotEmpty();
        assertThat(beans.getClazzes()).isNotEmpty();
        assertThat(autowired.getClazzes()).isNotEmpty();
        assertThat(autowiredOptional.getClazzes()).isNotEmpty();
        assertThat(conditional.getClazzes()).isNotEmpty();
        assertThat(scan.getClazzes()).isNotEmpty();
        assertThat(experimental.getClazzes()).isNotEmpty();
    }
}
