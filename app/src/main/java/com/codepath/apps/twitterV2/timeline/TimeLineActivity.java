package com.codepath.apps.twitterV2.timeline;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.codepath.apps.twitterV2.R;
import com.codepath.apps.twitterV2.app.TwitterV2Application;
import com.codepath.apps.twitterV2.create.CreateTweetActivity;
import com.codepath.apps.twitterV2.models.Tweet;
import com.codepath.apps.twitterV2.rest.RestClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;

public class TimeLineActivity extends AppCompatActivity implements TweetAdapter.OnItemClickListener {

    private static final String TAG = TimeLineActivity.class.getSimpleName();
    private static final String EXTRA_TWEET_LIST = TAG + ".EXTRA_TWEET_LIST";
    private static final int REQUEST_CODE_CREATE_TWEET = 1001;

    private static final int DEFAULT_SINCE = 1;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.rvTimeline)
    RecyclerView rvTimeLine;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    private RestClient client;
    private TweetAdapter mTweetAdapter;
    private EndlessRecyclerViewScrollListener scrollListener;

    private List<Tweet> mTweetList;
    private int currentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_line);
        ButterKnife.bind(this);
        if(savedInstanceState != null) {
            mTweetList = savedInstanceState.getParcelableArrayList(EXTRA_TWEET_LIST);
        }
        init();

        if(mTweetList == null || mTweetList.size() == 0) {
            populateHomeTimeLine(DEFAULT_SINCE);
        }

    }

    private void init() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        client = TwitterV2Application.getRestClient();
        if(mTweetList == null) {
            mTweetList = new ArrayList<>();
        }
        mTweetAdapter = new TweetAdapter(this, mTweetList);
        mTweetAdapter.setOnItemClickListener(this);
        rvTimeLine.setAdapter(mTweetAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvTimeLine.setLayoutManager(layoutManager);
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // Set the current page.
                currentPage = page;
                loadMoreDataFromApi();
            }
        };
        rvTimeLine.addOnScrollListener(scrollListener);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateHomeTimeLine(DEFAULT_SINCE);
            }
        });
    }

    private void loadMoreDataFromApi() {
        if(mTweetList != null && mTweetList.size() > 1) {
            populateHomeTimeLine(mTweetList.get(mTweetList.size() - 1).getUuid());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(EXTRA_TWEET_LIST, (ArrayList<Tweet>)mTweetList);
    }

    private void populateHomeTimeLine(long maxId) {
        // This means this is the first request
        if(maxId == DEFAULT_SINCE) {
            client.getHomeTimeLine(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<Tweet>>(){}.getType();
                    // In this test code i just shove the JSON here as string.
                    List<Tweet> list = gson.fromJson(response.toString(), listType);
                    // Remove older tweets and ask for fresh list.
                    clearTweets();
                    populateTimeLine(list);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject jsonObject) {
                    resetPageValueToPrevious();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }, DEFAULT_SINCE);
        } else {
            // This is subsequent request
            client.getHomeTimeLineMaxId(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<Tweet>>(){}.getType();
                    // In this test code i just shove the JSON here as string.
                    List<Tweet> list = gson.fromJson(response.toString(), listType);
                    populateTimeLine(list);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject jsonObject) {
                    resetPageValueToPrevious();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }, maxId);
        }


    }

    /**
     * If request fails then function to revert to the previous page.
     */
    private void resetPageValueToPrevious() {
        if(currentPage > 0) {
            currentPage = currentPage - 1;
            scrollListener.setCurrentPage(currentPage);
        }
    }

    private void clearTweets() {
        mTweetList.clear();
    }

    private void populateTimeLine(List<Tweet> list) {
        mTweetList.addAll(list);
        mTweetAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }


    @OnClick(R.id.fab)
    public void onFabClicked(View view) {
        Intent intent = new Intent(this, CreateTweetActivity.class);
        ActivityCompat.startActivityForResult(this, intent, REQUEST_CODE_CREATE_TWEET, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK && requestCode == REQUEST_CODE_CREATE_TWEET) {
            // Refesh the latest tweet from the server.
            populateHomeTimeLine(DEFAULT_SINCE);
        }
    }

    @Override
    public void onItemClicked(int pos) {
        //TODO: Will implement in the next assignment.
    }
}
