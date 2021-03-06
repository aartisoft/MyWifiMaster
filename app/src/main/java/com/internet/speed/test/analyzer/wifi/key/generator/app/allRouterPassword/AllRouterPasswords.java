package com.internet.speed.test.analyzer.wifi.key.generator.app.allRouterPassword;

import android.os.Bundle;
import android.text.TextUtils;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.internet.speed.test.analyzer.wifi.key.generator.app.R;
import com.internet.speed.test.analyzer.wifi.key.generator.app.activities.ActivityBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class AllRouterPasswords extends ActivityBase {


    ArrayList<All_Router_Model_Class> arrayList;
    RouterAdapterClass adapter;
    RecyclerView recyclerView;
    SearchView searchView;


    public ImageView headerItemCenterRight;
    public TextView headerItemTextViewFirst;
    public TextView headerItemTextViewSecond;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarGradient(this, R.color.colorWhite, R.color.colorWhite);
        setContentView(R.layout.activity_all_router_password);
        if (haveNetworkConnection()) {
            requestBanner((FrameLayout) findViewById(R.id.bannerContainer));
        }
        setUpHeader();
        initViews();
        setUpSearchViewChangeListeners();


    }

    private void initViews() {
        arrayList = new ArrayList<>();
        try {

            JSONObject obj = new JSONObject(readJSONFromAsset());
            JSONArray m_jArry = obj.getJSONArray("modem");
            ArrayList<HashMap<String, String>> formList = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> m_li;

            for (int i = 0; i < m_jArry.length(); i++) {
                JSONObject jo_inside = m_jArry.getJSONObject(i);

                String brand = jo_inside.getString("brand");
                String model = jo_inside.getString("model");
                String protocol = jo_inside.getString("protocol");
                String username = jo_inside.getString("username");
                String password = jo_inside.getString("password");

                All_Router_Model_Class object = new All_Router_Model_Class(brand, password, username, protocol, model);

                arrayList.add(object);


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        searchView = findViewById(R.id.acAllRouter_searchView);
        recyclerView = findViewById(R.id.allRouterRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RouterAdapterClass(arrayList, this);
        recyclerView.setAdapter(adapter);

    }

    void setUpHeader() {

        headerItemCenterRight = findViewById(R.id.header_item_centerRight_imageView);
        headerItemTextViewFirst = findViewById(R.id.header_item_textView_First);
        headerItemTextViewSecond = findViewById(R.id.header_item_textView_Second);

        headerItemCenterRight.setImageResource(R.drawable.ic_header_item_all_router_pass);
        headerItemTextViewFirst.setText("Default Router");
        headerItemTextViewSecond.setText("Password");


    }

    public String readJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("modem");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void setUpSearchViewChangeListeners() {
        searchView.setQueryHint(getString(R.string.enter_brand_name));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    adapter.updateData(arrayList);
                }
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        SearchView.OnCloseListener closeListener = new SearchView.OnCloseListener() {

            @Override
            public boolean onClose() {
                adapter.updateData(arrayList);
                return false;
            }
        };
        searchView.setOnCloseListener(closeListener);
    }
}
