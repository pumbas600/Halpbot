package nz.pumbas.halpbot.converters.parametercontext;

import org.dockbox.hartshorn.core.Enableable;
import org.dockbox.hartshorn.core.context.ContextCarrier;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import nz.pumbas.halpbot.converters.annotations.ParameterAnnotation;
import nz.pumbas.halpbot.converters.types.ArrayTypeContext;
import nz.pumbas.halpbot.utilities.Reflect;

public interface ParameterAnnotationService extends ContextCarrier, Enableable
{
    Comparator<TypeContext<? extends Annotation>> comparator();

    ParameterAnnotationContextFactory factory();

    @Override
    @SuppressWarnings("unchecked")
    default void enable() {
        Collection<TypeContext<?>> parameterAnnotations = this.applicationContext().environment()
                .types(ParameterAnnotation.class)
                .stream()
                .filter(type -> type.childOf(Annotation.class))
                .toList();
        for (TypeContext<?> parameterAnnotation : parameterAnnotations) {
            this.register((TypeContext<? extends Annotation>) parameterAnnotation);
        }
        this.applicationContext().log().info("Registered %d parameter annotations".formatted(parameterAnnotations.size()));
    }

    default void register(TypeContext<? extends Annotation> annotationType) {
        Exceptional<ParameterAnnotation> eParameterAnnotation = annotationType.annotation(ParameterAnnotation.class);

        // It's possible to specify that an annotation comes after another one which doesn't have the annotation
        if (eParameterAnnotation.absent()) {
            this.add(annotationType, HalpbotParameterAnnotationContext.generic());
        }
        else {
            ParameterAnnotation parameterAnnotation = eParameterAnnotation.get();
            this.add(annotationType,
                    this.factory()
                    .create(
                            Stream.of(parameterAnnotation.after())
                                    .map(TypeContext::of)
                                    .collect(Collectors.toSet()),
                            Stream.of(parameterAnnotation.conflictingAnnotations())
                                    .map(TypeContext::of)
                                    .collect(Collectors.toSet()),
                            Stream.of(parameterAnnotation.allowedType())
                                    .map(type -> type.equals(ArrayTypeContext.class)
                                            ? ArrayTypeContext.TYPE : TypeContext.of(type))
                                    .collect(Collectors.toSet())
                    ));

            // I've made sure to add the parameter annotation context to the map before checking these, in case
            // there's a circular reference, so that this doesn't get stuck in an infinite loop.
            for (Class<? extends Annotation> before : parameterAnnotation.before()) {
                this.getAndRegister(TypeContext.of(before))
                        .addAfterAnnotation(annotationType);
            }
        }
    }
    
    default ParameterAnnotationContext getAndRegister(TypeContext<? extends Annotation> annotationType) {
        if (!this.contains(annotationType))
            this.register(annotationType);
        return this.get(annotationType);
    }

    default boolean isValid(TypeContext<?> parameterType,
                            List<TypeContext<? extends Annotation>> parameterAnnotations) {
        return parameterAnnotations.stream()
                .map(this::get)
                .allMatch(annotationContext ->
                        annotationContext.isValidType(Reflect.wrapPrimative(parameterType)) &&
                        annotationContext.noConflictingAnnotations(parameterAnnotations));
    }
    
    ParameterAnnotationContext get(TypeContext<? extends Annotation> annotationType);

    boolean isRegisteredParameterAnnotation(TypeContext<? extends Annotation> annotationType);

    void add(TypeContext<? extends Annotation> annotationType,
             ParameterAnnotationContext annotationContext);

    boolean contains(TypeContext<? extends Annotation> annotationType);

    default List<TypeContext<? extends Annotation>> sortAndFilter(List<TypeContext<? extends Annotation>> annotations) {
        return this.sortAndFilter(annotations.stream());
    }

    default List<TypeContext<? extends Annotation>> sortAndFilter(Stream<TypeContext<? extends Annotation>> annotations) {
        return annotations
                .filter(this::isRegisteredParameterAnnotation)
                .sorted(this.comparator())
                .collect(Collectors.toList());
    }
}