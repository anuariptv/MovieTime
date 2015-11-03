package com.zuzhi.app.movietime;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zuzhi on 15/11/2.
 */
public class MovieTimeFragment extends Fragment
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MovieTimeFragment";

    private RecyclerView mPhotoRecyclerView;
    private RecyclerView mRecyclerView;
    private List<MovieItem> mItems = new ArrayList<>();
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;
    private ProgressBar mProgressBar;

    public static Fragment newInstance() {
        return new MovieTimeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setContentView(R.layout.activity_movie_time);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                getActivity(), drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        updateItems();
        //new DoubanFetchrTask().execute(); // 豆瓣API使用HttpClient总是返回500， weired :(

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
                    @Override
                    public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail) {
                        Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                        target.bindDrawable(drawable);
                    }
                }
        );
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper(); // Ensure the thread’s guts are ready before proceeding
        Log.i(TAG, "Background thread started");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_time, container, false);

        mPhotoRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_movie_time_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_movie_time_recycler_view_new);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mProgressBar.setIndeterminate(true);
        mProgressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        mProgressBar.setVisibility(View.VISIBLE);

        setupAdapter();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit(); // Terminate the thread
        Log.i(TAG, "Background thread destroyed");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_movie_time, menu);

        final MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "QueryTextSubmit: " + query);

                searchItem.collapseActionView();

                // Check if no view has focus:
                View view = getActivity().getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                QueryPreferences.setStoredQuery(getActivity(), query);
                updateItems();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "QueryTextChange: " + newText);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(), null);
                updateItems();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateItems() {
        String query = QueryPreferences.getStoredQuery(getActivity());

        if (mPhotoRecyclerView != null) {
            mItems.clear();
            mPhotoRecyclerView.getAdapter().notifyDataSetChanged();
            getActivity().setTitle("搜索中...");
            mProgressBar.setVisibility(View.VISIBLE);
        }

        new FetchItemsTask(query).execute();
    }

    private void setupAdapter() {
        if (isAdded()) {
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
            mRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        // Handle navigation view item clicks here.
        int id = menuItem.getItemId();

        if (id == R.id.nav_in_theaters) {
            // Handle the camera action
            toast("in theaters");
        } else if (id == R.id.nav_coming_soon) {
            toast("coming soon");
        } else if (id == R.id.nav_top_movies) {
            toast("top movies");
        } else if (id == R.id.nav_weekly) {
            toast("weekly");
        } else if (id == R.id.nav_share) {
            toast("share");
        } else if (id == R.id.nav_send) {
            toast("send");
        }

        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void toast(String string) {
        Toast.makeText(getActivity(), string, Toast.LENGTH_SHORT).show();
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private ImageView mItemImageView;
        private TextView mItemTopWhat;
        private TextView mItemTextView;
        private TextView mItemOriginalTitle;
        private TextView mItemYear;
        private TextView mItemRating;

        public PhotoHolder(View itemView) { // Wiring up
            super(itemView);

            mItemTopWhat = (TextView) itemView.findViewById(R.id.fragment_movie_time_top_what);
            mItemImageView = (ImageView) itemView.findViewById(R.id.fragment_movie_time_image_view);
            mItemTextView = (TextView) itemView.findViewById(R.id.fragment_movie_time_text_view);
            mItemOriginalTitle = (TextView) itemView.findViewById(R.id.fragment_movie_time_original_title);
            mItemYear = (TextView) itemView.findViewById(R.id.fragment_movie_time_year);
            mItemRating = (TextView) itemView.findViewById(R.id.fragment_movie_time_rating);
        }

        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }

        public void bindGalleryItem(MovieItem item) { // bind here
            mItemTopWhat.setText(getResources().getString(R.string.top_what, getAdapterPosition() + 1));
            mItemTextView.setText(item.getTitle());
            mItemOriginalTitle.setText(item.getOriginalTitle());
            mItemYear.setText(item.getYear());

            JsonObject ratings = item.getRatingJsonObject();
            JsonElement rating = ratings.get("average");
            mItemRating.setText(rating.toString());
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private List<MovieItem> mGalleryItems;

        public PhotoAdapter(List<MovieItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.movie_item, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            MovieItem galleryItem = mGalleryItems.get(position);
            Drawable placeholder = ContextCompat.getDrawable(getActivity(), R.drawable.poster_holder);
            holder.bindDrawable(placeholder);
            holder.bindGalleryItem(galleryItem);

            // get url from JSONObject
            JsonObject images = galleryItem.getImagesJsonObject();
            JsonElement image = images.get("large");

            // Get rid of the extra '"' in url string
            String url = image.toString().replace("\"", "");

            mThumbnailDownloader.queueThumbnail(holder, url);
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, MovieItems> {
        private String mQuery;

        public FetchItemsTask(String query) {
            mQuery = query;
        }
        @Override
        protected MovieItems doInBackground(Void... params) {

            /*String query = "克里斯托弗诺兰"; // Just for testing
            query = null;
            query = "昆明";*/

            if (mQuery == null) {
                mQuery = "昆明";
                return new DoubanFetchr().in_theaters(mQuery);
            } else {
                return new DoubanFetchr().searchMovies(mQuery);
            }
        }

        @Override
        protected void onPostExecute(MovieItems galleryItems) {
            getActivity().setTitle(galleryItems.getTitle());
            mItems = galleryItems.getMovieItems();
            setupAdapter();
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private class DoubanFetchrTask extends AsyncTask<Void, Void, MovieItems> {
        private static final String TAG = "DoubanFetchr";
        //    private static final String BASE_URL = "https://api.douban.com";
        private static final String BASE_URL = "https://api.douban.com/v2/movie/in_theaters?city=昆明";

        @Override
        protected MovieItems doInBackground(Void... params) {
            MovieItems galleryItems = new MovieItems();

            try {
                // Create an HTTP client
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(BASE_URL);

                // Perform the request and check the status code
                HttpResponse response = client.execute(post); // 豆瓣API使用HttpClient总是返回500， weired :(
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    HttpEntity entity = response.getEntity();
                    InputStream inputStream = entity.getContent();

                    try {
                        //Read the server response and attempt to parse it as JSON
                        Reader reader = new InputStreamReader(inputStream);

                        GsonBuilder gsonBuilder = new GsonBuilder();
                        Gson gson = gsonBuilder.create();
                        galleryItems = gson.fromJson(reader, MovieItems.class);

                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse JSON due to: " + e);
                    } finally {
                        inputStream.close();
                    }
                } else {
                    Log.e(TAG, "Server responded with status code: " + statusLine.getStatusCode());
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to send HTTP POST request due to: " + e);
            }
            return galleryItems;
        }

        @Override
        protected void onPostExecute(MovieItems galleryItems) {
            getActivity().setTitle(galleryItems.getTitle());
            mItems = galleryItems.getMovieItems();
            setupAdapter();
        }
    }
}
