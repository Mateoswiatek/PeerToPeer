package pl.agh.hasher;

import java.math.BigInteger;

public class PermutationGenerator {
    private final char[] alphabet;
    private final int maxLength;

    public PermutationGenerator(String alphabet, int maxLength) {
        this.alphabet = alphabet.toCharArray();
        this.maxLength = maxLength;
    }

    public BigInteger calculateTotalPermutations() {
        BigInteger total = BigInteger.ZERO;
        BigInteger currentPower = BigInteger.ONE;
        BigInteger alphabetSize = BigInteger.valueOf(alphabet.length);

        for (int i = 1; i <= maxLength; i++) {
            currentPower = currentPower.multiply(alphabetSize);
            total = total.add(currentPower);
        }

        return total;
    }

    public String getPermutation(BigInteger index) {
        StringBuilder result = new StringBuilder();
        BigInteger alphabetSize = BigInteger.valueOf(alphabet.length);

        for (int length = 1; length <= maxLength; length++) {
            BigInteger permutationsForLength = alphabetSize.pow(length);

            if (index.compareTo(permutationsForLength) < 0) {
                // Znaleźliśmy odpowiednią długość hasła
                for (int i = 0; i < length; i++) {
                    BigInteger[] divAndRem = index.divideAndRemainder(alphabetSize);
                    int charIndex = divAndRem[1].intValue();
                    result.insert(0, alphabet[charIndex]);
                    index = divAndRem[0];
                }
                return result.toString();
            } else {
                index = index.subtract(permutationsForLength);
            }
        }

        throw new IllegalArgumentException("Index poza zakresem");
    }



//    public Iterable<String> getPermutationsInRange(BigInteger start, BigInteger end) {
//        return () -> new Iterator<>() {
//            private BigInteger currentIndex = start;
//
//            @Override
//            public boolean hasNext() {
//                return currentIndex.compareTo(end) < 0;
//            }
//
//            @Override
//            public String next() {
//                if (!hasNext()) {
//                    throw new IllegalStateException("No more elements");
//                }
//                String permutation = getPermutation(currentIndex);
//                currentIndex = currentIndex.add(BigInteger.ONE);
//                return permutation;
//            }
//        };
//    }
//
//    public static void main(String[] args) {
//        String alphabet = "abc";
//        int maxLength = 5;
//
//        PermutationGenerator generator = new PermutationGenerator(alphabet, maxLength);
//
//        // Liczba wszystkich permutacji
//        BigInteger totalPermutations = generator.calculateTotalPermutations();
//        System.out.println("Liczba wszystkich permutacji: " + totalPermutations);
//
//        // Przykładowy zakres
//        BigInteger start = BigInteger.ZERO;
//        BigInteger end = BigInteger.valueOf(500);
//
//        System.out.println("Permutacje w zakresie od " + start + " do " + end + ":");
//        for (String permutation : generator.getPermutationsInRange(start, end)) {
//            System.out.println(permutation);
//        }
//    }
}