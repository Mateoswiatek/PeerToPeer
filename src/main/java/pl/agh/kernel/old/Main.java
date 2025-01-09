package pl.agh.kernel.old;

import java.math.BigInteger;

public class Main {

    public static void main(String[] args) {
        // Przykładowe użycie
        String alphabet = "abc";
        int maxLength = 3;

        PermutationGenerator generator = new PermutationGenerator(alphabet, maxLength);

        // Liczba wszystkich permutacji
        BigInteger totalPermutations = generator.calculateTotalPermutations();
        System.out.println("Liczba wszystkich permutacji: " + totalPermutations);

        for (BigInteger index = BigInteger.ZERO; index.compareTo(totalPermutations) < 0; index = index.add(BigInteger.ONE)) {
            System.out.println("Permutacja dla indeksu " + index + ": " + generator.getPermutation(index));
        }
    }
}
