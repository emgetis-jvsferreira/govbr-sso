package br.gov.sso;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App {

    private static final String CLIENT_ID = System.getenv("GOVBR_CLIENTID");
    private static final String CLIENT_SECRET = System.getenv("GOVBR_SECRET");
    private static final String REDIRECT_URI = System.getenv("GOVBR_CLIENTID");
    private static final String AUTH_URL = System.getenv("GOVBR_API_URL");
    private static final String SCOPE = System.getenv("GOVBR_SCOPE");

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(3000), 0);

        server.createContext("/", exchange -> {
                try {
                    String query = exchange.getRequestURI().getQuery();
                    Boolean hasCode = query != null && query.contains("code=");

                    if (hasCode) {
                        String code = query.split("code=")[1].split("&")[0];
                        System.out.println("Código de autorização: " + code);

                        String reqBody = String.format(
                            "grant_type=authorization_code&code=%s&redirect_uri=%s&client_id=%s&client_secret=%s",
                            urlEncode(code),
                            urlEncode("https://".concat(REDIRECT_URI)),
                            urlEncode(CLIENT_ID),
                            urlEncode(CLIENT_SECRET)
                        );

                        HttpClient client = HttpClient.newHttpClient();
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(AUTH_URL.concat("/token")))
                                .header("Content-Type", "application/x-www-form-urlencoded")
                                .POST(HttpRequest.BodyPublishers.ofString(reqBody))
                                .build();

                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                        int status = response.statusCode();
                        String body = response.body();

                        if (status == 200) {
                            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                            String accessToken = json.get("access_token").getAsString();
                            System.out.println(String.format("Access Token: %s", accessToken));

                            String res = "Login realizado com sucesso. Você pode fechar esta aba.";
                            sendResponse(exchange, 200, res);
                        } else {
                            System.err.println(String.format("Erro ao obter token %s", body));
                            sendResponse(exchange, 500, "Erro ao obter token.");
                        }

                        server.stop(1);
                        return 0;
                    }

                    sendResponse(exchange, 404, "Parâmetro \"code\" não encontrado.");
                    return 1;
                } catch (Exception e) {
                    throw new Error("", e);
                }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Servidor iniciado em \"http://localhost:3000\".");

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.schedule(() -> {
            System.out.println("Tempo limite atingido. Encerrando...");
            server.stop(0);
            scheduler.shutdown();
            System.exit(0);
        }, 2, TimeUnit.MINUTES);

        String authUrl = String.format(
            "%s/authorize?response_type=code&client_id=%s&redirect_uri=%s&scope=%s",
            AUTH_URL,
            urlEncode(CLIENT_ID),
            urlEncode("https://".concat(REDIRECT_URI)),
            urlEncode(SCOPE)
        );

        System.out.println("Abrindo link \"".concat(AUTH_URL).concat("\" no navegador..."));
        Desktop.getDesktop().browse(new URI(authUrl));
    }

    private static void sendResponse(com.sun.net.httpserver.HttpExchange exchange, int statusCode, String body) throws Exception {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        } catch (Exception e) {
            throw new Error("sendResponse", e);
        }
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new Error("URL encode", e);
        }
    }
}