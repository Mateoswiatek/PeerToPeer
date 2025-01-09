package pl.agh.hasher;

import java.math.BigInteger;

public class PermutationIteratorExample {

    private static char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray(); // Alfabet
    private static int maxLength = 3; // Maksymalna długość hasła

    public static BigInteger calculateTotalPermutations() {
        BigInteger total = BigInteger.ZERO;
        BigInteger currentPower = BigInteger.ONE;
        BigInteger alphabetSize = BigInteger.valueOf(alphabet.length);

        for (int i = 1; i <= maxLength; i++) {
            currentPower = currentPower.multiply(alphabetSize);
            total = total.add(currentPower);
        }

        return total;
    }

    public static void main(String[] args) {
        // Używamy BigInteger do reprezentowania indeksów
        BigInteger startIndex = new BigInteger("400");
        BigInteger endIndex = new BigInteger("500");

        // Tworzymy iterator na podstawie zakresu
        PermutationIterator iterator = new PermutationIterator(startIndex, endIndex, maxLength, alphabet);

        System.out.println("Max permutations: " + calculateTotalPermutations());

        // Iterujemy po permutacjach w zadanym zakresie
        while (iterator.hasNext()) {
            System.out.println("Permutacja: " + iterator.next());
        }
    }
}
