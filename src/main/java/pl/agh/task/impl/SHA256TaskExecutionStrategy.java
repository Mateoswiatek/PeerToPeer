package pl.agh.task.impl;

import pl.agh.task.PermutationIterator;
import pl.agh.task.model.Batch;
import pl.agh.task.model.Task;
import pl.agh.task.impl.TaskExecutionStrategy;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256TaskExecutionStrategy implements TaskExecutionStrategy {

    @Override
    public void execute(Task task, Batch batch) {
        try {
            BigInteger min = new BigInteger(batch.getMin());
            BigInteger max = new BigInteger(batch.getMax());
            PermutationIterator iterator = new PermutationIterator(min, max, task.getMaxLength().intValue(), task.getAlphabet().toCharArray());

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            while (iterator.hasNext()) {
                String candidate = iterator.next();
                String hash = hashString(candidate, digest);

                if (hash.equals(task.getPasswordHash())) {
                    task.complete(candidate);
                    return;
                }
            }
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Hashing algorithm not found: " + e.getMessage());
        }
    }

    private String hashString(String input, MessageDigest digest) {
        byte[] encodedhash = digest.digest(input.getBytes());
        StringBuilder hexString = new StringBuilder();

        for (byte b : encodedhash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }
}