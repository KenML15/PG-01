package model;


import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Recursion {

    public static long factorial(int n, AtomicInteger counter) {
        counter.incrementAndGet();
        if (n <=1 ) return 1;
        return n * factorial(n - 1,counter);
    }

    public static void matryoshka(int n) {
        if (n <= 1) {
            System.out.println("abriendo la muñeca mas pequeña: " + n);

        } else {
            System.out.println("abriendo la muñeca número: " + n);
            matryoshka(n - 1);
        }

    }
    public static String matryoshkaS(int n) {
        if (n <= 1) {
            return ("\n Abriendo la muñeca mas pequeña: " + n);

        } else {
            return ("\n Abriendo la muñeca número: " + n+ matryoshkaS(n - 1));

        }

    }

    public static long fibonacci(int n, AtomicInteger counter ){
        counter.incrementAndGet(); //contar cada llamada recursiva
        if(n<=1) return n;
        return fibonacci(n-1,counter)+fibonacci(n-2,counter);
    }

    public static long fibMemo(int n, Map<Integer,Long> memo,AtomicInteger counter){ //hashmap
        counter.incrementAndGet();
        if(n<=1) return n;
        if(memo.containsKey(n)) return memo.get(n);

        long result=fibMemo(n-1,memo,counter)+fibMemo(n-2,memo,counter);
        memo.put(n,result); //guardar en caché
        return result;
    }

    public static long fibMemoArray(int n, long[] memo,AtomicInteger counter ){ //Arreglos
        counter.incrementAndGet();
        if(n<=1) return n;
        if(memo[n] != -1) return memo[n]; //si tiene un resultado almacenado
        memo[n]=fibMemoArray(n-1,memo,counter)+fibMemoArray(n-2,memo,counter);

        return memo[n];

    }
}
