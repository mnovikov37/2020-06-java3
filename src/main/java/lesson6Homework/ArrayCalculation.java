package lesson6Homework;

import java.util.ArrayList;

public class ArrayCalculation {

    public int[] subArray(int[] inputArray) {
        ArrayList<Integer> array = new ArrayList<>();
        boolean add = false;
        for (int element:inputArray) {
            if (add) { array.add(element); }
            if (element == 4) {
                array.clear();
                add = true;
            }
        }
        if (!add) { throw new RuntimeException("Входной массив должен содержать хотя бы одну цифру 4"); }
        else {
            int[] result = new int[array.size()];
            for (int i = 0; i < array.size(); i++) { result[i] = array.get(i); }
            return result;
        }
    }

    public boolean numberFinder(int[] inputArray) {
        boolean isFind1 = false, isFind4 = false;
        for (int element:inputArray) {
            if (element == 1) { isFind1 = true; }
            if (element == 4) { isFind4 = true; }
        }
        return isFind1 && isFind4;
    }

    public static void main(String[] args) {
        ArrayCalculation arrayCalculation = new ArrayCalculation();
        int[] array = arrayCalculation.subArray(new int[]{1, 2, 3});
        System.out.println(array);
        for (int element:array) { System.out.print(element + " ");}
        System.out.println();
        System.out.println(arrayCalculation.numberFinder(new int[]{1, 1, 4, 4, 1}));
    }
}
