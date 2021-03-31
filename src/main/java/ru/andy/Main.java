package ru.andy;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class Main {

    // URL for mock server
    private static final String URL = "https://jsonplaceholder.typicode.com/albums";
    private static final int NOT_SUCCESSFULL_STATUS_CODE_LOW_RANGE = 300;
    private static HttpURLConnection connection;

    public static void main(String[] args) {
        javaEightVariant();
    }

    private static void javaEightVariant() {
        BufferedReader reader;
        String line;
        StringBuffer responseContent = new StringBuffer();

        try {
            URL url = new URL(URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int status = connection.getResponseCode();

            if (status >= NOT_SUCCESSFULL_STATUS_CODE_LOW_RANGE) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                reader.close();
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                reader.close();
            }

            parseJsonPureJava(responseContent.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void javaElevenVariant() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .GET()
                .build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(Main::parseJson)
                .join();
    }

    private static String parseJson(String respBody) {
        JSONArray albums = new JSONArray(respBody);
        for (int i = 0; i < albums.length(); ++i) {
            JSONObject album = albums.getJSONObject(i);
            int id = album.getInt("id");
            int userId = album.getInt("userId");
            String title = album.getString("title");
            System.out.println(id + " " + userId + " " + title);
        }
        return null;
    }

    private static Map<String, ScriptObjectMirror> parseJsonPureJava(String json) {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("javascript");

        Map<String, ScriptObjectMirror> result = null;
        try {
            result = (Map<String, ScriptObjectMirror>) scriptEngine.eval(json);
            result.forEach((key, value) -> System.out.println(key + " " + value.entrySet() + "/n"));
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return result;
    }
}
