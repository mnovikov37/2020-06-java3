import lesson6Homework.ArrayCalculation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ArrayCalculationTest {
    private ArrayCalculation arrayCalculation;

    @BeforeEach
    public void init() {
        arrayCalculation = new ArrayCalculation();
    }

    // Тест на выходной массив
    @ParameterizedTest
    @MethodSource("dataForSubArrayArrayTest")
    public void subArrayArrayTest(int[] inputArray, int[] result) {
        Assertions.assertArrayEquals(result, arrayCalculation.subArray(inputArray));
    }

    public static Stream<Arguments> dataForSubArrayArrayTest() {
        List<Arguments> out = new ArrayList<>();
        out.add(Arguments.arguments(new int[]{1, 2, 3, 4, 5, 6, 7}, new int[]{5,6,7}));
        out.add(Arguments.arguments(new int[]{1, 2, 3, 4, 4, 6, 7}, new int[]{6,7}));
        out.add(Arguments.arguments(new int[]{1, 2, 3, 4, 4}, new int[]{}));
        return out.stream();
    }

    // Тест на выброс исключения
    @ParameterizedTest
    @MethodSource("dataForSubArrayExceptionTest")
    public void subArrayExceptionTest(int[] inputArray) {
        Assertions.assertThrows(RuntimeException.class,() -> arrayCalculation.subArray(inputArray));
    }

    public static Stream<Arguments> dataForSubArrayExceptionTest() {
        List<Arguments> out = new ArrayList<>();
        out.add(Arguments.arguments(new int[]{1, 2, 3, 7, 5, 6, 7}));
        out.add(Arguments.arguments(new int[]{1, 2, 3, 8, 8, 6, 7}));
        out.add(Arguments.arguments(new int[]{1, 2, 3, 2, 1}));
        return out.stream();
    }

    // numberFinder: тест на значение true
    @ParameterizedTest
    @MethodSource("dataForNumberFinderTrueTest")
    public void NumberFinderTrueTest(int[] inputArray) {
        Assertions.assertTrue(arrayCalculation.numberFinder(inputArray));
    }

    public static Stream<Arguments> dataForNumberFinderTrueTest() {
        List<Arguments> out = new ArrayList<>();
        out.add(Arguments.arguments(new int[]{1, 1, 1, 4, 4, 4}));
        out.add(Arguments.arguments(new int[]{1, 4, 1, 4, 1, 4}));
        out.add(Arguments.arguments(new int[]{1, 1, 4, 4, 1, 4, 1, 4}));
        return out.stream();
    }

    // numberFinder: тест на значение false
    @ParameterizedTest
    @MethodSource("dataForNumberFinderFalseTest")
    public void NumberFinderFalseTest(int[] inputArray) {
        Assertions.assertFalse(arrayCalculation.numberFinder(inputArray));
    }

    public static Stream<Arguments> dataForNumberFinderFalseTest() {
        List<Arguments> out = new ArrayList<>();
        out.add(Arguments.arguments(new int[]{1, 1, 1, 1, 1, 1}));
        out.add(Arguments.arguments(new int[]{1, 1, 1}));
        out.add(Arguments.arguments(new int[]{4, 4, 4, 4}));
        return out.stream();
    }

}
