package com.otfly.auth;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Objects;

public class SpotifyAuthService 
{
    private final String clientId;
    private final String redirectUri;
    private final String scope;
    private final OAuthHandler oAuthHandler;
    private final String codeChalMethod = "S256";
    private final String code = "code";

    private volatile String state;
    private volatile String codeVerifier;

    public SpotifyAuthService(String id, String uri, String scope, OAuthHandler auth)
    {
        this.clientId = id;
        this.redirectUri = uri;
        this.scope = scope;
        this.oAuthHandler = auth;
    }

    final String constructAuthorizationURL()
    {
        Map<String, String> pairs = new LinkedHashMap<>();
        String baseEndpoint = "https://accounts.spotify.com/authorize";

        generateState();
        generateVerifier();
        String codeChal = oAuthHandler.codeChallenge(codeVerifier);

        pairs.put("client_id", clientId);
        pairs.put("response_type", code);
        pairs.put("redirect_uri", redirectUri);
        pairs.put("code_challenge", codeChal);
        pairs.put("code_challenge_method", codeChalMethod);
        pairs.put("state", state);
        pairs.put("scope", scope);

        String query = buildQueryString(pairs);

        return baseEndpoint + "?" + query;
    }

    private void generateState()
    {
        state = oAuthHandler.generateRandomString(64);
    }

    private void generateVerifier()
    {
        codeVerifier = oAuthHandler.generateRandomString(64);
    }

    private static String buildQueryString(Map<String, String> pairs)
    {
        if(pairs == null)
            throw new IllegalArgumentException("Pairs map cannot be null.");

        StringJoiner joiner = new StringJoiner("&");
        for(Map.Entry<String, String> entry : pairs.entrySet())
        {
            String key = entry.getKey();
            String value = entry.getValue();

            if(key == null || value == null)
                throw new IllegalArgumentException("Query string keys and values cannot be null.");

            String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8).replace("+", "%20");
            String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");

            joiner.add(encodedKey + "=" + encodedValue);
        }

        return joiner.toString();
    }

    private boolean validateState(String cbState)
    {
        String localState = state;
        state = null;

        return Objects.equals(cbState, localState);
    }

    private String consumeCodeVerifier()
    {
        String localCodeVerifier = codeVerifier;
        codeVerifier = null;

        return localCodeVerifier;
    }
}
