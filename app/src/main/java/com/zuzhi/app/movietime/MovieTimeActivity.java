package com.zuzhi.app.movietime;

import android.support.v4.app.Fragment;

public class MovieTimeActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return MovieTimeFragment.newInstance();
    }
}
