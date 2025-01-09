package pl.agh.hasher;

public class PermutationIteratorExample {

    private static char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray(); // Alfabet
    private static int maxLength = 3; // Maksymalna długość hasła

    public static void main(String[] args) {
        int startIndex = 400;
        int endIndex = 500;

        // Tworzymy iterator na podstawie zakresu
        PermutationIterator iterator = new PermutationIterator(startIndex, endIndex, maxLength, alphabet);

        // Iterujemy po permutacjach w zadanym zakresie
        while (iterator.hasNext()) {
            System.out.println("Permutacja: " + iterator.next());
        }
    }
}
