package com.zuzhi.app.movietime;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zuzhi on 15/10/13.
 */
public class DoubanFetchr {

    private static final String TAG = "DoubanFetchr";

    private static final String SEARCH_METHOD = "search";
    private static final String IN_THEATERS_METHOD = "in_theaters";
    private static final String COMING_SOON_METHOD = "coming_soon";
    private static final String TOP250_METHOD = "top250";

    private static final Uri ENDPOINT = Uri.parse("https://api.douban.com/v2/movie/");

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            InputStream inputStream = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead;
            byte[] buffer = new byte[1024];
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            return outputStream.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public MovieItems methodWithQuery(String method, String query) {
        String url = buildUrl(method, query);
        return downloadMovieItems(url);
    }

    private MovieItems downloadMovieItems(String url) {
        Gson gson = new Gson();

        MovieItems movieItems = new MovieItems();
        try {

            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            movieItems = gson.fromJson(jsonString, MovieItems.class);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        }
        return movieItems;
    }

    private String buildUrl(String method, String query) {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon().appendPath(method);

        switch (method) {
            case SEARCH_METHOD:
                uriBuilder.appendQueryParameter("q", query);
                break;
            case IN_THEATERS_METHOD:
                query = "昆明";
                uriBuilder.appendQueryParameter("city", query);
                break;
            case COMING_SOON_METHOD:
                break;
            case TOP250_METHOD:
                break;
            default:
        }

        return uriBuilder.build().toString();
    }

}
