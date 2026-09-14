package com.otfly.auth;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.io.IOException;
import java.awt.Desktop;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

    final boolean authorize()
    {
        String stringUrl = constructAuthorizationURL();
        URI uri = URI.create(stringUrl);

        try(AuthCallbackServer callBack = new AuthCallbackServer())
        {
            Desktop.getDesktop().browse(uri);
            CallbackResult callBackResult = callBack.callbackFuture().get(60, TimeUnit.SECONDS);
            
            if(!validateState(callBackResult.state()))
                return false;
            else
            {
                String authCode = callBackResult.code();
                String authVerifier = consumeCodeVerifier();
            }

            return true;
        }
        catch(IOException e)
        {
            if(e.getMessage() != null && e.getMessage().contains("Port 8888 is already in use."))
                System.err.println("Authentication failed: Port 8888 is already in use. Close the application using that port and try again.");
            else
                e.printStackTrace();

            return false;
        }
        catch(TimeoutException e)
        {
            System.err.println("Authentication timed out waiting for callback. Please try again.");
            return false;
        }
        catch(ExecutionException e)
        {
            String causeMessage = e.getCause() != null ? e.getCause().getMessage() : null;

            if(causeMessage != null && causeMessage.contains("access_denied"))
                System.err.println("Access denied, otfly needs permission to work.");
            else if(causeMessage != null && causeMessage.contains("callback received neither code or error"))
                System.err.println("Authentication callback did not return a code or error. Please try again.");
            else
                e.printStackTrace();

            return false;
        }
        catch(InterruptedException e)
        {
            System.err.println("Authentication interrupted while waiting for callback: " + e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
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
