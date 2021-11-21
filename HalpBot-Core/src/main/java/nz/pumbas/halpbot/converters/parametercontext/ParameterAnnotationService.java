package nz.pumbas.halpbot.converters.parametercontext;

import org.dockbox.hartshorn.core.context.ContextCarrier;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import nz.pumbas.halpbot.converters.annotations.ParameterAnnotation;

public interface ParameterAnnotationService extends ContextCarrier
{
    ParameterAnnotationContextFactory factory();

    default void register(@NotNull TypeContext<? extends Annotation> annotationType) {
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
                                    .map(TypeContext::of)
                                    .collect(Collectors.toSet())
                    ));

            // I've made sure to add the parameter annotation context to the map before checking these, in case
            // there's a circular reference, so that this doesn't get stuck in an infinite loop. The circular
            // reference will be identified at a later point when it goes to order the parameter annotations.
            for (Class<? extends Annotation> before : parameterAnnotation.before()) {
                this.getAndRegister(TypeContext.of(before))
                        .addAfterAnnotation(annotationType);
            }
        }
    }

    @NotNull
    default ParameterAnnotationContext getAndRegister(@NotNull TypeContext<? extends Annotation> annotationType) {
        if (!this.contains(annotationType))
            this.register(annotationType);
        return this.get(annotationType);
    }

    default boolean isValid(@NotNull TypeContext<?> parameterType,
                            @NotNull List<TypeContext<? extends Annotation>> parameterAnnotations) {
        return parameterAnnotations.stream()
                .map(this::get)
                .allMatch(annotationContext ->
                        annotationContext.isValidType(parameterType) && annotationContext.noConflictingAnnotations(parameterAnnotations));
    }

    @NotNull
    ParameterAnnotationContext get(@NotNull TypeContext<? extends Annotation> annotationType);

    void add(@NotNull TypeContext<? extends Annotation> annotationType,
             @NotNull ParameterAnnotationContext annotationContext);

    boolean contains(@NotNull TypeContext<? extends Annotation> annotationType);

    @NotNull
    <T extends  Annotation> List<T> sort(@NotNull List<T> annotations);

}
