package pl.agh;

import pl.agh.task.PermutationIterator;

import java.math.BigInteger;

public class PermutationIteratorExample {

    private static char[] alphabet = "abc".toCharArray(); // Alfabet // defghijklmnopqrstuvwxyz
    private static int maxLength = 10; // Maksymalna długość hasła

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
        BigInteger startIndex = new BigInteger("0");
        BigInteger endIndex = new BigInteger("30");

        // Tworzymy iterator na podstawie zakresu
        PermutationIterator iterator = new PermutationIterator(startIndex, endIndex, maxLength, alphabet);

        System.out.println("Max permutations: " + calculateTotalPermutations());

        // Iterujemy po permutacjach w zadanym zakresie
        while (iterator.hasNext()) {
            System.out.println("Permutacja: " + iterator.next());
        }
    }
}
