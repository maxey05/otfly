package com.otfly.auth;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.util.concurrent.TimeUnit;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.net.URLDecoder;

public class AuthCallbackServer 
{
    public static String waitCallback() throws Exception
    {
        CompletableFuture<String> codeFuture = new CompletableFuture<>();

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8888), 0);

        server.createContext("/callback", exchange -> {
            String query = exchange.getRequestURI().getRawQuery();

            String code = getQueryParam(query, "code");
            String error = getQueryParam(query, "error");
            String state = getQueryParam(query, "state");

            if(error != null)
                codeFuture.completeExceptionally(new Exception(error));
            else
                codeFuture.complete(code);

            String response = error != null
                    ? "Authentication failed."
                    : "Authentication successful.";

            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);

            try(OutputStream output = exchange.getResponseBody())
            {
                output.write(responseBytes);
            }

            server.stop(0);
        });

        server.start();

        return codeFuture.get(60, TimeUnit.SECONDS);
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
