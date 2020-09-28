package com.internet.speed.test.analyzer.wifi.key.generator.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.internet.speed.test.analyzer.wifi.key.generator.app.R;
import com.internet.speed.test.analyzer.wifi.key.generator.app.adapters.AppTimeLineAdapter;


import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import bot.box.appusage.contract.PackageContracts;
import bot.box.appusage.contract.TimelineContracts;
import bot.box.appusage.handler.Monitor;
import bot.box.appusage.model.AppData;
import bot.box.appusage.model.TimeLine;
import bot.box.appusage.utils.Duration;
import bot.box.appusage.utils.UsageUtils;

public class DetailActivity extends ActivityBase {
    private static final String PACKAGE_NAME = "_packageName";
    private ProgressBar loadingBar;

    public static void start(Activity activity, String packageName) {
        Intent intent = new Intent(activity, DetailActivity.class);
        intent.putExtra(PACKAGE_NAME, packageName);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarGradient(this, R.color.colorWhite, R.color.colorWhite);
        setContentView(R.layout.activity_details);
        loadingBar = findViewById(R.id.acDetail_loadingBar);
        String packageName = getIntent().getStringExtra(PACKAGE_NAME);
        Monitor.scan().queryFor(new PackageContracts.View() {

            @Override
            public void showProgress() {
                loadingBar.setVisibility(View.VISIBLE);
                //optional
            }

            @Override
            public void hideProgress() {
                loadingBar.setVisibility(View.INVISIBLE);
                //optional
            }

            @Override
            public void getUsageForPackage(AppData appData, int duration) {
                ((TextView) findViewById(R.id.name)).setText(appData.mName);
                ((TextView) findViewById(R.id.total_times_launched)).setText(appData.mCount + " " + getResources().getQuantityString(R.plurals.times_launched, appData.mCount));
                ((TextView) findViewById(R.id.data_used)).setText(UsageUtils.humanReadableByteCount(appData.mWifi + appData.mMobile) + " Consumed");
                ((TextView) findViewById(R.id.last_launched)).setText(String.format(Locale.getDefault(),
                        "%s", "Last Launch " +
                                new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault()).format(new Date(appData.mEventTime))));
                ((TextView) findViewById(R.id.total_usage_time)).
                        setText(UsageUtils.humanReadableMillis(appData.mUsageTime));
                Glide.with(DetailActivity.this)
                        .load(UsageUtils.parsePackageIcon(appData.mPackageName, R.mipmap.ic_launcher)).
                        transition(new DrawableTransitionOptions().crossFade())
                        .into((ImageView) findViewById(R.id.icon));
            }


        }).whichPackage(packageName).fetchFor(Duration.TODAY);

        /**
         * fetching timeline
         */
        AppTimeLineAdapter timeLineAdapter = new AppTimeLineAdapter(getApplicationContext());
        RecyclerView rv = findViewById(R.id.timelineRecyclerView);
//        rv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rv.setAdapter(timeLineAdapter);
        Monitor.scan().generateTimeline(new TimelineContracts.View() {
            @Override
            public void onTimelineGenerated(List<List<TimeLine>> timeline) {
                timeLineAdapter.updateData(timeline);
            }
        }).whichPackage(packageName).fetchForToday();
    }
}
