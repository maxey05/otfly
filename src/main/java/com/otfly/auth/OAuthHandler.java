package com.otfly.auth;

import java.security.SecureRandom;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class OAuthHandler 
{
    final String generateRandomString(int length)
    {
        final String PERMISSABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        final SecureRandom RANDOM = new SecureRandom();
        StringBuilder result = new StringBuilder(length);
        int i = 0;

        while(i < length)
        {
            int currentIndex = RANDOM.nextInt(PERMISSABLE.length());
            result.append(PERMISSABLE.charAt(currentIndex));
            i++;
        }

        return result.toString();
    }

    final boolean validString(String randomized)
    {
        return randomized.length() >=  43 && randomized.length() <= 128;
    }

    final String sha256(String randomized)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(randomized.getBytes(StandardCharsets.UTF_8));

            StringBuilder result = new StringBuilder();
            for(byte b : hash)
            {
                result.append(String.format("%02x", b));
            }

            return result.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }
}
