package com.zuzhi.app.movietime;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Created by zuzhi on 15/10/21.
 * for method parseItem3()
 */
public class MovieItem {

    @SerializedName("title") // 片名
    private String mTitle;

    @SerializedName("original_title") // 原片名
    private String mOriginalTitle;

    @SerializedName("year") // 上映年份
    private String mYear;

    @SerializedName("images") // 海报
    private JsonObject mImagesJsonObject;

    @SerializedName("rating") // 评分
    private JsonObject mRatingJsonObject;

    @SerializedName("id")
    private String mId;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getOriginalTitle() {
        return mOriginalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        mOriginalTitle = originalTitle;
    }

    public String getYear() {
        return mYear;
    }

    public void setYear(String year) {
        mYear = year;
    }

    public JsonObject getImagesJsonObject() {
        return mImagesJsonObject;
    }

    public void setImagesJsonObject(JsonObject imagesJsonObject) {
        mImagesJsonObject = imagesJsonObject;
    }

    public JsonObject getRatingJsonObject() {
        return mRatingJsonObject;
    }

    public void setRatingJsonObject(JsonObject ratingJsonObject) {
        mRatingJsonObject = ratingJsonObject;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }
}
