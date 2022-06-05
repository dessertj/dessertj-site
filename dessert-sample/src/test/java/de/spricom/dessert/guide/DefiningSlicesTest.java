package de.spricom.dessert.guide;

import de.spricom.dessert.classfile.ClassFile;
import de.spricom.dessert.classfile.attribute.Attributes;
import de.spricom.dessert.classfile.attribute.SourceFileAttribute;
import de.spricom.dessert.partitioning.ClazzPredicates;
import de.spricom.dessert.resolve.ClassPackage;
import de.spricom.dessert.resolve.ClassResolver;
import de.spricom.dessert.resolve.ClassRoot;
import de.spricom.dessert.resolve.TraversalRoot;
import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Clazz;
import de.spricom.dessert.slicing.Root;
import de.spricom.dessert.slicing.Slice;
import de.spricom.dessert.util.Predicate;
import de.spricom.dessert.util.Predicates;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.InitializingBean;

import java.lang.invoke.MethodHandles;

import static org.assertj.core.api.Assertions.assertThat;

public class DefiningSlicesTest {
    private static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());
    private static final Classpath cp = new Classpath();

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
        Slice spring = cp.slice("org.springframework..*");
        Root dessert = cp.rootOf(Slice.class);

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
}
