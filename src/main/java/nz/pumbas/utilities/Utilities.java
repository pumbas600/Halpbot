package nz.pumbas.utilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import nz.pumbas.utilities.functionalinterfaces.IOFunction;

public final class Utilities
{

    //A constant 'empty' UUID
    public static final UUID emptyUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static final float fullRotation = 360F;
    public static final float threeQuarterRotation = 270F;
    public static final float halfRotation = 180F;
    public static final float quarterRotation = 90F;

    public static final String line = "-----------------------------------------------------------";

    /**
     * Retrieves all the methods of a class with the specified annotation.
     *
     * @param target
     *     The class to search for methods with the specified annotation
     * @param annotation
     *     The annotation to check if methods have
     * @param getSuperMethods
     *     Whether it should check for annotated methods in super classes
     *
     * @return A {@link List} containing all the methods with the specified annotation
     */
    public static List<Method> getAnnotatedMethods(Class<?> target, Class<? extends Annotation> annotation, boolean getSuperMethods)
    {
        List<Method> methods = new ArrayList<>();

        for (Method method : target.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotation))
                methods.add(method);
        }

        if (getSuperMethods) {
            Class<?> superclass = target.getSuperclass();
            if (Object.class != superclass)
                methods.addAll(getAnnotatedMethods(superclass, annotation, getSuperMethods));
        }
        return methods;
    }

    /**
     * Retrieves all the methods of a class with the specified annotation which pass all the specified filters.
     *
     * @param target
     *     The class to search for methods with the specified annotation
     * @param annotation
     *     The annotation to check if methods have
     * @param getSuperMethods
     *     Whether it should check for annotated methods in super classes
     * @param filters
     *     A varargs of {@link Predicate filters} to check the methods against
     *
     * @return A {@link List} containing all the matching methods
     */
    @SafeVarargs
    public static List<Method> getAnnotatedMethods(Class<?> target, Class<? extends Annotation> annotation, boolean getSuperMethods, Predicate<Method>... filters)
    {
        return getAnnotatedMethods(target, annotation, getSuperMethods)
            .stream()
            .filter(m ->
                Arrays.stream(filters).allMatch(filter -> filter.test(m)))
            .collect(Collectors.toList());
    }

    /**
     * Retrieves all the methods of a class with the specified annotation which have the specified modifiers.
     * You can specify modifiers as follows: {@code Modifier::isPublic, Modifier::isStatic}, etc.
     *
     * @param target
     *     The class to search for methods with the specified annotation
     * @param annotation
     *     The annotation to check if methods have
     * @param getSuperMethods
     *     Whether it should check for annotated methods in super classes
     * @param modifierFilters
     *     A varargs of {@link Predicate modifiers} to check if the methods have
     *
     * @return A {@link List} containing all the matching methods
     */
    @SafeVarargs
    public static List<Method> getAnnotatedMethodsWithModifiers(Class<?> target, Class<? extends Annotation> annotation, boolean getSuperMethods, Predicate<Integer>... modifierFilters)
    {
        return getAnnotatedMethods(target, annotation, getSuperMethods)
            .stream()
            .filter(m ->
                Arrays.stream(modifierFilters).allMatch(modifier -> modifier.test(m.getModifiers())))
            .collect(Collectors.toList());
    }

    /**
     * If an object has the specified annotation, call the {@link Consumer} with the annotation.
     *
     * @param object
     *     The object being checked for the annotation
     * @param annotationClass
     *     The class of the annotation
     * @param consumer
     *     The {@link Consumer} to be called if the object has the annotation
     * @param <T>
     *     The type of the annotation
     */
    public static <T extends Annotation> void ifAnnotationPresent(Object object, Class<T> annotationClass, Consumer<T> consumer)
    {
        if (object.getClass().isAnnotationPresent(annotationClass)) {
            consumer.accept(object.getClass().getAnnotation(annotationClass));
        }
    }

    /**
     * If an object has the specific annotation, retrieve a field using the {@link Function} otherwise return the default value.
     *
     * @param object
     *     The object being checked for the annotation
     * @param annotationClass
     *     The class of the annotation
     * @param function
     *     The function to retrieve the annotation field
     * @param defaultValue
     *     The default value to return if the object doesn't have the annotation
     * @param <T>
     *     The type of the annotation
     * @param <R>
     *     The type of the annotation's field being retreived
     *
     * @return
     */
    public static <T extends Annotation, R> R getAnnotationFieldElse(Object object, Class<T> annotationClass, Function<T, R> function, R defaultValue)
    {
        if (object.getClass().isAnnotationPresent(annotationClass)) {
            return function.apply(object.getClass().getAnnotation(annotationClass));
        }
        return defaultValue;
    }

    /**
     * Returns the first {@link Method} in a class with the specified name.
     *
     * @param clazz
     *      The class to check, for the method with the specified name
     * @param name
     *      The name of the method to find
     *
     * @return An {@link Optional} containing the {@link Method}
     */
    public static Optional<Method> getMethod(Class<?> clazz, String name)
    {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                method.setAccessible(true);
                return Optional.of(method);
            }
        }
        return Optional.empty();
    }


    /**
     * Calls a method on each element in a queue as they're dequeued.
     *
     * @param queue
     *     The queue to loop through
     * @param consumer
     *     The method to apply each element in the queue
     * @param <T>
     *     The type of the elements in the queue
     */
    public static <T> void dequeueForEach(Queue<T> queue, Consumer<T> consumer)
    {
        while (!queue.isEmpty()) {
            consumer.accept(queue.remove());
        }
    }

    /**
     * Maps the values of a list to the passed in objects.
     *
     * @param list
     *     The {@link List} to get the values from
     * @param objects
     *     The objects to set the values for
     * @param <T>
     *     The type of the elements in the list
     */
    @SafeVarargs
    public static <T> void mapListToObjects(List<T> list, T... objects)
    {
        for (int i = 0; i < objects.length; i++) {
            if (i >= objects.length || i >= list.size()) break;

            objects[i] = list.get(i);
        }
    }

    /**
     * Retrieves the first line from a file.
     *
     * @param filename
     *     The name of the file to open
     *
     * @return The first line, or an empty {@link String} if there was an error reading the line
     */
    public static String getFirstLineFromFile(String filename)
    {
        return parseFile(filename, BufferedReader::readLine, "");
    }

    /**
     * Parses a file using the {@link IOFunction fileParser} provided.
     *
     * @param filename
     *     The name of the file to open
     * @param fileParser
     *     The {@link IOFunction} to parse the file
     * @param defaultValue
     *     The value to be returned if there is an error opening the file
     * @param <T>
     *     The type of the parsed file
     *
     * @return The parsed file
     */
    public static <T> T parseFile(String filename, IOFunction<BufferedReader, T> fileParser, T defaultValue)
    {

        Optional<InputStream> oIn = retrieveReader(filename);

        if (oIn.isPresent()) {
            InputStream in = oIn.get();
            try (InputStreamReader inputReader = new InputStreamReader(in)) {

                BufferedReader reader = new BufferedReader(inputReader);
                T parsedFile = fileParser.apply(reader);
                reader.close();

                return parsedFile;

            } catch (IOException e) {
                System.out.printf("There was an error trying to access the file %s%n", filename);
                e.printStackTrace();
            }
        }
        return defaultValue;
    }

    /**
     * Retrieves the InputStream for a file in the resources folder.
     *
     * @param filename
     *     The name of the file to retrieve
     *
     * @return The InputStream, if there were no errors retrieving it
     */
    private static Optional<InputStream> retrieveReader(String filename)
    {
        InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(filename);

        if (null == in) {
            System.out.printf("The file, %s, couldn't be found!%n", filename);
            return Optional.empty();
        }
        return Optional.of(in);

    }

    /**
     * Replaces the first occurance of the target in the passed str with the replacement without using regex.
     *
     * @param str
     *     The string being searched for the target
     * @param target
     *     The string to replace
     * @param replacement
     *     What to replace the target with
     *
     * @return The string with the first occurance of the target replaced with the replacement
     */
    public static String replaceFirst(@NotNull String str, @NotNull String target, @NotNull String replacement)
    {
        int targetIndex = str.indexOf(target);
        int endIndex = targetIndex + target.length();

        if (-1 != targetIndex) {
            String before = str.substring(0, targetIndex);
            String after = endIndex < str.length() ? str.substring(endIndex) : "";

            return before + replacement + after;
        }

        return str;
    }

    /**
     * Capitalises the first letter of the {@link String} and sets the rest of the word to lowercase.
     *
     * @param str
     *      The {@link String} to capitalise
     *
     * @return The capitalised {@link String}
     */
    public static String capitalise(String str) {
        if (null == str || str.isEmpty())
            return str;

        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Capitalises the first letter of each word in the {@link String} and sets the rest of the word to lowercase.
     *
     * @param str
     *      The {@link String sentence} to have each word capitalised
     *
     * @return The capitalised sentence
     */
    public static String capitaliseEachWord(@NotNull String str) {
        String[] words = str.split(" ");
        return Arrays.stream(words)
            .map(Utilities::capitalise)
            .collect(Collectors.joining(" "));
    }
}
