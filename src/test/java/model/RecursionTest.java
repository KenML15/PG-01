package model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

class RecursionTest {

    @Test
    void factorialTest() {
        int[] list = {5, 10, 12, 15, 20};

        for (int n : list) {
            AtomicInteger counter = new AtomicInteger(0);
            long t1 = System.nanoTime();
            long result = Recursion.factorial(n, counter);
            long t2 = System.nanoTime();

            System.out.println("Factorial " + n + " es: " + util.Utility.format(result) +
                    "| Total de llamadas recursivas: " + util.Utility.format(counter.get()) +
                    "| T(n): " + util.Utility.format(t2 - t1) + " ns");
        }
        }

        @Test
        void matryoshkaTest () {
            Recursion.matryoshka(5);
        }

        @Test
        void matryoshkaSTest () {
            System.out.println(Recursion.matryoshkaS(5));
        }

        @Test
        void fibonacciTest () {
            int[] list = {5, 10, 12, 15, 20};

            for (int n : list) {
                AtomicInteger counter = new AtomicInteger(0);
                long t1 = System.nanoTime();
                long result = Recursion.fibonacci(n, counter);
                long t2 = System.nanoTime();

                System.out.println("Fibonacci " + n + " es: " + util.Utility.format(result) +
                        "| Total de llamadas recursivas: " + util.Utility.format(counter.get()) +
                        "| T(n): " + util.Utility.format(t2 - t1) + " ns");
            }

        }
        @Test
        void fibMemoTest () {
            int[] list = {5, 10, 12, 15, 20};

            for (int n : list) {
                AtomicInteger counter = new AtomicInteger(0);
                Map<Integer, Long> memo = new HashMap<>();
                long t1 = System.nanoTime();
                long result = Recursion.fibMemo(n, memo, counter);
                long t2 = System.nanoTime();

                System.out.println("Fibonacci Memoria " + n + " es: " + util.Utility.format(result) +
                        "| Total de llamadas recursivas: " + util.Utility.format(counter.get()) +
                        "| T(n): " + util.Utility.format(t2 - t1) + " ns");
            }

        }
        @Test
        void fibMemoArrayTest () {

            int[] list = {5, 10, 12, 15, 20};

            for (int n : list) {

                AtomicInteger counter = new AtomicInteger(0);
                long[] memo = new long[n + 1];

                //inicializo el arreglo en -1
                for (int i = 0; i < n + 1; i++) {
                    memo[i] = -1;
                }
                long t1 = System.nanoTime();
                long result = Recursion.fibMemoArray(n, memo, counter);
                long t2 = System.nanoTime();

                System.out.println("Fibonacci Memoria Array" + n + " es: " + util.Utility.format(result) +
                        "| Total de llamadas recursivas: " + util.Utility.format(counter.get()) +
                        "| T(n): " + util.Utility.format(t2 - t1) + " ns");
            }

        }


    }