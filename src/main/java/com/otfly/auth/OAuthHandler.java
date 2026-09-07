package com.otfly.auth;

import java.security.SecureRandom;
import java.security.MessageDigest;

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
        return randomized.length() > 43 || randomized.length() < 128;
    }
}
