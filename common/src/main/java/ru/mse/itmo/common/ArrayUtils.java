package ru.mse.itmo.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ArrayUtils {
    private static final Random random = new Random();

    private ArrayUtils() {
    }

    public static List<Integer> generateRandomArray(int n) {
        return random.ints(0, 100).limit(n).boxed().collect(Collectors.toList());
    }

    public static List<Integer> insertionSort(List<Integer> array) {
        List<Integer> sortedArray = new ArrayList<>(array);
        int n = sortedArray.size();
        for (int i = 1; i < n; i++) {
            int cur = sortedArray.get(i);
            int j = i - 1;
            while (j >= 0 && sortedArray.get(j) > cur) {
                sortedArray.set(j + 1, sortedArray.get(j));
                j--;
            }
            sortedArray.set(j + 1, cur);
        }
//        System.out.println("FROM Sorting:");
//        sortedArray.forEach(elem -> System.out.print(elem + " "));
        return sortedArray;
    }
}
