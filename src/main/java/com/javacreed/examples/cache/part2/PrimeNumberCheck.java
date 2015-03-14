package com.javacreed.examples.cache.part2;

import java.math.BigInteger;
import java.util.concurrent.Callable;

public class PrimeNumberCheck {

    public static void main(final String[] args) throws Exception {
        final BigInteger number = new BigInteger("2074722246773485207821695222107608587480996474721117292752992589912196684750549658310084416732550077");
        final PrimeNumberCheck example = new PrimeNumberCheck();
        final boolean isPrime = example.isPrimeNumber(number);
        System.out.printf("The number %d is a prime number: %s%n", number, isPrime);
    }

    private final GenericCacheExample<BigInteger, Boolean> cache = new GenericCacheExample<>();

    public boolean isPrimeNumber(final BigInteger number) throws Exception {
        return cache.getValue(number, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return number.isProbablePrime(Integer.MAX_VALUE);
            }
        });
    }
}
