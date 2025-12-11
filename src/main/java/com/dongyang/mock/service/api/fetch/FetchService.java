package com.dongyang.mock.service.api.fetch;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.lang.reflect.Type;
import java.util.Map;

public abstract class FetchService {
    protected final long DELAY_TIME = 1000; final Gson gson = new Gson();

    public abstract void runUpdate() throws Exception;

    protected Map<String, Object> callApi(String token, String urlString, String tr, String requestBody, String method) throws Exception {
        return callApi(token, urlString, tr, requestBody, method, "N", "");
    }

    protected Map<String, Object> callApi(String token, String urlString, String tr, String requestBody, String method, String contYn, String nextKey) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("authorization", token);
        conn.setRequestProperty("cont-yn", contYn == null ? "N" : contYn);
        conn.setRequestProperty("next-key", contYn == null ? "" : nextKey);
        conn.setRequestProperty("api-id", tr);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        BufferedReader br;

        if (responseCode >= 200 && responseCode <= 300) {
            // 성공 응답 (2xx)
            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        } else {
            // 실패 응답 (4xx, 5xx)
            br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        br.close();

        String jsonResponse = response.toString();
        System.out.println("API 응답 (" + tr + "): " + jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)) + "...");

        Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
        return gson.fromJson(jsonResponse, mapType);
    }
}
