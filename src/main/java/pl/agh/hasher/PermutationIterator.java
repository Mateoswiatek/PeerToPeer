package pl.agh.hasher;

public class PermutationIterator {
    private static char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray(); // Alfabet
    private static int maxLength = 3; // Maksymalna długość hasła

    public static void main(String[] args) {
        int startIndex = 400;
        int endIndex = 500;

        for (int i = startIndex; i <= endIndex; i++) {
            String password = getPermutationAtIndex(i, maxLength, alphabet);
            System.out.println("Permutacja " + i + ": " + password);
        }
    }

    // Funkcja do uzyskania permutacji na podstawie numeru
    public static String getPermutationAtIndex(int index, int length, char[] alphabet) {
        int base = alphabet.length;
        char[] result = new char[length];

        for (int i = 0; i < length; i++) {
            int charIndex = index % base;
            result[length - 1 - i] = alphabet[charIndex];
            index /= base;
        }

        return new String(result);
    }
}
