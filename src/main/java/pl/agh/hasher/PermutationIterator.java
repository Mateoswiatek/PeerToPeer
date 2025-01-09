package pl.agh.hasher;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class PermutationIterator implements Iterator<String> {

    private int currentIndex;
    private final int endIndex;
    private final int maxLength;
    private final char[] alphabet;

    public PermutationIterator(int startIndex, int endIndex, int maxLength, char[] alphabet) {
        this.currentIndex = startIndex;
        this.endIndex = endIndex;
        this.maxLength = maxLength;
        this.alphabet = alphabet;
    }

    // Sprawdza, czy są jeszcze permutacje do przetworzenia
    @Override
    public boolean hasNext() {
        return currentIndex <= endIndex;
    }

    // Zwraca następną permutację
    @Override
    public String next() {
        if (!hasNext()) {
            throw new NoSuchElementException("Brak więcej permutacji w zakresie");
        }

        String password = getPermutationAtIndex(currentIndex, maxLength, alphabet);
        currentIndex++;
        return password;
    }

    // Funkcja do uzyskania permutacji na podstawie numeru
    private String getPermutationAtIndex(int index, int length, char[] alphabet) {
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
