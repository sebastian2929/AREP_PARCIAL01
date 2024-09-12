package org.example;

import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflexCalculator {

    public String compute(String command, Double[] params) {
        try {
            if ("bbl".equalsIgnoreCase(command)) {
                bubbleSort(params);
                return Arrays.toString(params);
            } else {
                Method method = Math.class.getMethod(command, Double.TYPE);
                Double result = (Double) method.invoke(null, params[0]);
                return result.toString();
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private void bubbleSort(Double[] array) {
        boolean sorted;
        do {
            sorted = true;
            for (int i = 0; i < array.length - 1; i++) {
                if (array[i] > array[i + 1]) {
                    Double temp = array[i];
                    array[i] = array[i + 1];
                    array[i + 1] = temp;
                    sorted = false;
                }
            }
        } while (!sorted);
    }
}