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

        // Obliczamy aktualną długość permutacji na podstawie indeksu
        int currentLength = calculateCurrentLength(currentIndex, alphabet.length, maxLength);
        String password = getPermutationAtIndex(currentIndex, currentLength, alphabet);

        // Przechodzimy do następnego indeksu
        currentIndex = currentIndex.add(BigInteger.ONE);
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

    private int calculateCurrentLength(BigInteger index, int alphabetSize, int maxLength) {
        BigInteger currentPower = BigInteger.ONE;
        BigInteger totalPermutations = BigInteger.ZERO;

        for (int length = 1; length <= maxLength; length++) {
            currentPower = currentPower.multiply(BigInteger.valueOf(alphabetSize));
            totalPermutations = totalPermutations.add(currentPower);

            if (index.compareTo(totalPermutations) < 0) {
                return length;
            }
        }

        throw new IllegalStateException("Index exceeds the total number of permutations");
    }
}