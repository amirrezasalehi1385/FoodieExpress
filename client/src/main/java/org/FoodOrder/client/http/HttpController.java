package org.FoodOrder.client.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpController {
    public static HttpResponse sendRequest(String url, HttpMethod method, String body, HttpHeaders headers) throws IOException {
        HttpURLConnection connection = null;
        try {
            URL apiUrl = new URL(url);
            connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod(method.toString());
            if (headers != null) {
                for (String key : headers.keySet()) {
                    connection.setRequestProperty(key, headers.get(key));
                }
            }
            if ((method == HttpMethod.POST || method == HttpMethod.PUT) && body != null && !body.isEmpty()) {
                connection.setDoOutput(true);
                try (OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(body.getBytes(StandardCharsets.UTF_8));
                }
            }
            int responseCode = connection.getResponseCode();
            BufferedReader reader;
            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return new HttpResponse(responseCode, response.toString());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}