package pl.agh.kernel.old;

public class PermutationGeneratorV2 {

    public static String getPermutation(char[] alphabet, int number) {
        int base = alphabet.length;
        StringBuilder result = new StringBuilder();

        // Generate the permutation by treating the number as a base-N number
        while (number > 0) {
            result.append(alphabet[number % base]);
            number /= base;
        }

        // Reverse the result since we build it in reverse order
        return !result.isEmpty() ? result.reverse().toString() : Character.toString(alphabet[0]);
    }

    public static void main(String[] args) {
        // Define the alphabet
        char[] alphabet = {'a', 'b', 'c', 'd'};

        int number = 3;

        String permutation = getPermutation(alphabet, number);
        System.out.println("Permutation at index " + number + ": " + permutation);
    }
}

