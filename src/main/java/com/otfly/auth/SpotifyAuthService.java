package com.otfly.auth;

import com.otfly.auth.OAuthHandler;
import java.net.URLEncoder;
import java.util.StringJoiner;
import java.util.Map;
import java.util.LinkedHashMap;

public class SpotifyAuthService 
{
    private final String clientId;
    private final String redirectUri;
    private final String scope;
    private final OAuthHandler oAuthHandler;
    private final String codeChalMethod = "S256";

    private String state;
    private String codeVerifier;

    public SpotifyAuthService(String id, String uri, String scope, OAuthHandler auth)
    {
        this.clientId = id;
        this.redirectUri = uri;
        this.scope = scope;
        this.oAuthHandler = auth;
    }

    final String constructAuthorizationURL(String codeChal)
    {
        Map<String, String> pairs = new LinkedHashMap<>();

        generateState();
        generateVerifier();
        String code = oAuthHandler.codeChallenge(codeVerifier);

        pairs.put(clientId, code);
        pairs.put(redirectUri, scope);
        pairs.put(codeChal, codeChalMethod);
        pairs.put(state, )
    }

    private void generateState()
    {
        state = oAuthHandler.generateRandomString(64);
    }

    private void generateVerifier()
    {
        codeVerifier = oAuthHandler.generateRandomString(64);
    }

    private String buildQueryString(Map<String, String> pairs)
    {

    }

}
