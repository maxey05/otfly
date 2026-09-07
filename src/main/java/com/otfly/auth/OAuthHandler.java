package com.otfly.auth;

import java.security.SecureRandom;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public final class OAuthHandler 
{
    final String generateRandomString(int length)
    {
        final String PERMISSABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        final SecureRandom RANDOM = new SecureRandom();
        StringBuilder result = new StringBuilder(length);
        int i = 0;

        if(length < 43 || length > 128)
        {
            throw new IllegalArgumentException("Verifier length must be between 43 and 128.");
        }

        while(i < length)
        {
            int currentIndex = RANDOM.nextInt(PERMISSABLE.length());
            result.append(PERMISSABLE.charAt(currentIndex));
            i++;
        }

        return result.toString();
    }

    final boolean validVerifier(String verifier)
    {
        return verifier.length() >=  43 && verifier.length() <= 128;
    }

    final byte[] sha256(String randomized)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(randomized.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }

    final String base64Encoder(byte[] hashed)
    {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
    }

    final String codeChallenge(String verifier)
    {

        if(!validVerifier(verifier))
        {
            throw new IllegalArgumentException("Invalid length.");
        }

        byte[] hashed = sha256(verifier);
        return base64Encoder(hashed);
    }
}
