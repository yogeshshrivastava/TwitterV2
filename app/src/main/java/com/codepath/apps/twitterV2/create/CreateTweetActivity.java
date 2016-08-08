package com.codepath.apps.twitterV2.create;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.twitterV2.R;
import com.codepath.apps.twitterV2.app.TwitterV2Application;
import com.codepath.apps.twitterV2.models.User;
import com.codepath.apps.twitterV2.rest.RestClient;
import com.codepath.apps.twitterV2.utils.CircleTransform;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;


public class CreateTweetActivity extends AppCompatActivity {

    public static final String TAG = CreateTweetActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.etCreateTweet)
    EditText etCreateTweet;

    @BindView(R.id.pbTweetPosting)
    ProgressBar pbTweetPosting;

    @BindView(R.id.ivProfile)
    ImageView ivProfile;

    @BindView(R.id.tvUserName)
    TextView tvUserName;

    @BindView(R.id.tvHandle)
    TextView tvHandle;

    @BindView(R.id.tvCharLeft)
    TextView tvCharLeft;

    private RestClient client;

    private int TOTAL_TWEET_CHAR = 140;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.create_tweet_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pbTweetPosting.setIndeterminate(true);
        init();
    }

    private void init() {
        client = TwitterV2Application.getRestClient();
        client.getProfileInformation(new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Gson gson = new Gson();
                User user = gson.fromJson(response.toString(), User.class);
                showUserInformation(user);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, "onFailure: responseBody", throwable);
            }
        });
    }

    private void showUserInformation(User user) {
        tvUserName.setText(user.getName());
        tvHandle.setText("@" + user.getScreenName());
        Picasso.with(this).load(user.getProfileImageUrl()).transform(new CircleTransform()).into(ivProfile);
        etCreateTweet.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = etCreateTweet.getText().toString();
                tvCharLeft.setText(String.valueOf(TOTAL_TWEET_CHAR - text.length()));
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @OnClick(R.id.fab)
    public void onFabClick() {
        String tweet = etCreateTweet.getText().toString();
        // Post the request to the twitter server.
        pbTweetPosting.setVisibility(View.VISIBLE);
        client.postComposedTweet(new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                pbTweetPosting.setVisibility(View.GONE);
                Toast.makeText(CreateTweetActivity.this, "Successfully posted.", Toast.LENGTH_LONG).show();
                // Mark result as successfully completed.
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pbTweetPosting.setVisibility(View.GONE);
                Toast.makeText(CreateTweetActivity.this, "Error while posting the tweet..", Toast.LENGTH_LONG).show();
            }

        }, tweet);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
