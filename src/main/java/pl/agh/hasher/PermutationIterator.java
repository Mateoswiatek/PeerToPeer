package pl.agh.hasher;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class PermutationIterator implements Iterator<String> {

    private BigInteger currentIndex;
    private final BigInteger endIndex;
    private final int maxLength;
    private final char[] alphabet;

    public PermutationIterator(BigInteger startIndex, BigInteger endIndex, int maxLength, char[] alphabet) {
        this.currentIndex = startIndex;
        this.endIndex = endIndex;
        this.maxLength = maxLength;
        this.alphabet = alphabet;
    }

    // Sprawdza, czy są jeszcze permutacje do przetworzenia
    @Override
    public boolean hasNext() {
        return currentIndex.compareTo(endIndex) <= 0;
    }

    // Zwraca następną permutację
    @Override
    public String next() {
        if (!hasNext()) {
            throw new NoSuchElementException("Brak więcej permutacji w zakresie");
        }

        String password = getPermutationAtIndex(currentIndex, maxLength, alphabet);
        currentIndex = currentIndex.add(BigInteger.ONE); // Przechodzimy do następnego indeksu
        return password;
    }

    // Funkcja do uzyskania permutacji na podstawie numeru
    private String getPermutationAtIndex(BigInteger index, int length, char[] alphabet) {
        int base = alphabet.length;
        char[] result = new char[length];

        for (int i = 0; i < length; i++) {
            BigInteger charIndex = index.mod(BigInteger.valueOf(base)); // Zmieniamy na BigInteger
            result[length - 1 - i] = alphabet[charIndex.intValue()]; // Przekształcamy na int
            index = index.divide(BigInteger.valueOf(base)); // Dzielimy przez podstawę
        }

        return new String(result);
    }
}