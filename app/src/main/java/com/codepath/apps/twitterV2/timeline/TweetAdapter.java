package com.codepath.apps.twitterV2.timeline;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.twitterV2.R;
import com.codepath.apps.twitterV2.models.Tweet;
import com.codepath.apps.twitterV2.utils.CircleTransform;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author yvastavaus.
 */
public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.ViewHolder> {

    private static final String TAG = TweetAdapter.class.getSimpleName();

    private final List<Tweet> tweetList;
    private final Context context;

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClicked(int pos);
    }

    public TweetAdapter(Context context,  List<Tweet>tweetList) {
        this.tweetList = tweetList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.tweet_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view, mOnItemClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tvTweet.setText(tweetList.get(position).getBody());
        holder.tvvUser.setText(tweetList.get(position).getUser().getScreenName());
        Picasso.with(context).load(tweetList.get(position).getUser().getProfileImageUrl()).transform(new CircleTransform()).into(holder.ivProfile);
        holder.tvSince.setText(getRelativeTimeAgo(tweetList.get(position).getCreatedAt()));
    }

    @Override
    public int getItemCount() {
        return tweetList.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    public String getRelativeTimeAgo(String createdAt) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(createdAt).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL).toString();
        } catch (ParseException e) {
            Log.e(TAG, "getRelativeTimeAgo: error : ", e);
        }

        return relativeDate;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        @BindView(R.id.ivProfile)
        ImageView ivProfile;

        @BindView(R.id.tvTweet)
        TextView tvTweet;

        @BindView(R.id.tvUser)
        TextView tvvUser;

        @BindView(R.id.tvSince)
        TextView tvSince;

        private OnItemClickListener mOnItemClickListener;

        public ViewHolder(View itemView, OnItemClickListener mOnItemClickListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            this.mOnItemClickListener = mOnItemClickListener;
        }

        @Override
        public void onClick(View view) {
            mOnItemClickListener.onItemClicked(getLayoutPosition());
        }
    }
}
