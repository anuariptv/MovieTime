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

    public MovieItems searchMovies(String query) {
        String url = buildUrl(SEARCH_METHOD, query);
        return downloadMovieItems(url);
    }

    public MovieItems in_theaters(String query) {
        String url = buildUrl(IN_THEATERS_METHOD, query);
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
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendPath(method);
        if (method.equals(SEARCH_METHOD)) {
            uriBuilder.appendQueryParameter("q", query);
        } else {
            uriBuilder.appendQueryParameter("city", query);
        }
        return uriBuilder.build().toString();
    }

}
