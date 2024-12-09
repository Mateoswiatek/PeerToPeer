package pl.agh;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashSolver {
    public static String findPassword(String targetHash, String charset, int maxLength) {
        for (int length = 1; length <= maxLength; length++) {
            String result = bruteForce("", targetHash, charset, length);
            if (result != null) return result;
        }
        return null;
    }

    private static String bruteForce(String prefix, String targetHash, String charset, int maxLength) {
        if (prefix.length() == maxLength) {
            if (hash(prefix).equals(targetHash)) {
                return prefix;
            }
            return null;
        }

        for (char c : charset.toCharArray()) {
            String result = bruteForce(prefix + c, targetHash, charset, maxLength);
            if (result != null) return result;
        }
        return null;
    }

    private static String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        String targetHash = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd873b6f8c...";
        String password = findPassword(targetHash, "abcdefghijklmnopqrstuvwxyz", 5);
        System.out.println("Found password: " + password);
    }
}
