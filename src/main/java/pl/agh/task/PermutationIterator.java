package pl.agh.task;

import lombok.AllArgsConstructor;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.NoSuchElementException;

@AllArgsConstructor
public class PermutationIterator implements Iterator<String> {

    private BigInteger currentIndex;
    private final BigInteger endIndex;
    private final int maxLength;
    private final char[] alphabet;

    @Override
    public boolean hasNext() {
        return currentIndex.compareTo(endIndex) <= 0;
    }

    @Override
    public String next() {
        if (!hasNext()) {
            throw new NoSuchElementException("Brak więcej permutacji w zakresie");
        }

        String password = getPermutationAtIndex(currentIndex, maxLength, alphabet);
        currentIndex = currentIndex.add(BigInteger.ONE); // Przechodzimy do następnego indeksu
        return password;
    }

    private String getPermutationAtIndex(BigInteger index, int length, char[] alphabet) {
        BigInteger baseBigInt = BigInteger.valueOf(alphabet.length);
        char[] result = new char[length];

        for (int i = 0; i < length; i++) {
            BigInteger charIndex = index.mod(baseBigInt); // Uzyskanie indeksu znaku z alfabetu
            result[length - 1 - i] = alphabet[charIndex.intValue()];

            // Zmniejszamy indeks, dzieląc przez podstawę
            index = index.divide(baseBigInt);
        }

        return new String(result);
    }
}