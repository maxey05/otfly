package com.otfly.auth;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class AuthCallbackServer implements AutoCloseable
{
    private final HttpServer server;
    private final CompletableFuture<CallbackResult> callbackFuture = new CompletableFuture<>();

    public AuthCallbackServer() throws IOException
    {
        try
        {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8888), 0);
        }
        catch (BindException e)
        {
            throw new IOException("Failed to start AuthCallbackServer: Port 8888 is already in use.", e);
        }

        server.createContext("/callback", this::handleCallback);
        server.start();
    }

    public CompletableFuture<CallbackResult> callbackFuture()
    {
        return callbackFuture;
    }

    private void handleCallback(com.sun.net.httpserver.HttpExchange exchange) throws IOException
    {
        String query = exchange.getRequestURI().getRawQuery();

        String code = getQueryParam(query, "code");
        String error = getQueryParam(query, "error");
        String state = getQueryParam(query, "state");

        if(code == null && error == null)
            callbackFuture.completeExceptionally(new Exception("callback received neither code or error"));
        else if(error != null)
            callbackFuture.completeExceptionally(new Exception(error));
        else
            callbackFuture.complete(new CallbackResult(code, state));

        String response = (code == null && error == null)
                ? "Authentication failed: Missing parameters."
                : error != null
                ? "Authentication failed."
                : "Authentication successful.";

        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);

        try(OutputStream output = exchange.getResponseBody())
        {
            output.write(responseBytes);
        }
    }

    @Override
    public void close()
    {
        server.stop(0);
    }

    private static String getQueryParam(String query, String param)
    {
        if(query == null)
            return null;

        for(String pair : query.split("&"))
        {
            String[] parts = pair.split("=", 2);

            if(parts.length == 2 && parts[0].equals(param))
            {
                return URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            }
        }

        return null;
    }
}
