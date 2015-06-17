/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tw.binary.dipper.util;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import tw.binary.dipper.gcm.GcmMessagePostTask;

/**
 * Helper class for sending and receiving HTTP request and response.
 */
public final class HttpUtils {

    private HttpUtils() {
    }

    public static byte[] get(String url) throws IOException {
        Log.i("HttpUtils", "HTTP GET " + url);
        return request("GET", url, null, null);
    }

    public static byte[] post(String url, byte[] body) throws IOException {
        Log.i("HttpUtils", "HTTP POST " + url);
        return request("POST", url, body);
    }

    public static byte[] post2gcm(String gcmApiKey, byte[] body) throws IOException {
        String url = "https://android.googleapis.com/gcm/send";
        Log.i("GCM HttpsUtils", "HTTPs POST " + url);
        return request("POST", url, body, gcmApiKey);
    }

    public static void postMessage(Context context, HashMap<String, String> params) throws IOException {
        /*String endpoint = context.getString(R.string.server_endpoint);

        try {
            URL url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }*/
        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Map.Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=').append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        String body = bodyBuilder.toString();
        byte[] bytes = body.getBytes();
        //Post message
        GcmMessagePostTask gcmMessagePostTask = new GcmMessagePostTask(context);
        gcmMessagePostTask.execute(bytes);
    }

    private static byte[] request(String method, String url, byte[] body, String apiKey) throws IOException {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setDoInput(true);  //intent to read data
            if (method.equals("POST")) {
                conn.setDoOutput(true); //if they include a request body
                conn.setFixedLengthStreamingMode(body.length);
                conn.setRequestProperty("Content-Type", "application/json");    //傳送Json資料
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "key=" + apiKey);
                OutputStream os = conn.getOutputStream();
                Log.i("HttpUtils", "Posting '" + IOUtils.toString(body, "UTF-8"));
                os.write(body);
                os.close();
            }

            BufferedInputStream in;
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                in = new BufferedInputStream(conn.getInputStream());
            } else {
                in = new BufferedInputStream(conn.getErrorStream());
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            in.close();
            return out.toByteArray();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static byte[] request(String method, String url, byte[] body) throws IOException {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setDoInput(true);  //intent to read data
            if (method.equals("POST")) {
                conn.setDoOutput(true); //if they include a request body
                conn.setFixedLengthStreamingMode(body.length);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8"); //傳送Form資料
                conn.setRequestMethod("POST");
                OutputStream os = conn.getOutputStream();
                Log.i("HttpUtils", "Posting '" + IOUtils.toString(body, "UTF-8"));
                os.write(body);
                os.close();
            }

            BufferedInputStream in;
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                in = new BufferedInputStream(conn.getInputStream());
            } else {
                in = new BufferedInputStream(conn.getErrorStream());
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            in.close();
            return out.toByteArray();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static void postMessage2(Context context, ContentValues params) throws IOException {
        /*String endpoint = context.getString(R.string.server_endpoint);

        try {
            URL url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }*/

        StringBuilder bodyBuilder = new StringBuilder();

        Iterator iterator = (Iterator) params.valueSet().iterator();

        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Map.Entry<String, Object> param = (Map.Entry<String, Object>) iterator.next();
            bodyBuilder.append(param.getKey().toString()).append('=').append(param.getValue().toString());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        String body = bodyBuilder.toString();
        byte[] bytes = body.getBytes();
        //Post message
        GcmMessagePostTask gcmMessagePostTask = new GcmMessagePostTask(context);
        gcmMessagePostTask.execute(bytes);
    }

}
