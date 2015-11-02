package com.zuzhi.app.movietime;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by zuzhi on 15/10/21.
 */
public class MovieItems {

    @SerializedName("title") // 返回的JSON的title，如：正在上映的电影-北京
    private String mTitle;

    @SerializedName("count") // 计划返回的条数。另外，total能返回的总条数
    private int mCount;

    @SerializedName("subjects") // 电影条目
    private List<MovieItem> mMovieItems;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public int getCount() {
        return mCount;
    }

    public void setCount(int count) {
        mCount = count;
    }

    public List<MovieItem> getMovieItems() {
        return mMovieItems;
    }

    public void setMovieItems(List<MovieItem> movieItems) {
        mMovieItems = movieItems;
    }
}
