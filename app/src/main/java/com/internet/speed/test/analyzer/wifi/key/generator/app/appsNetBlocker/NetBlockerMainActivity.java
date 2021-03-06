package com.internet.speed.test.analyzer.wifi.key.generator.app.appsNetBlocker;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.VpnService;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.internet.speed.test.analyzer.wifi.key.generator.app.R;
import com.internet.speed.test.analyzer.wifi.key.generator.app.activities.ActivityBase;

import java.util.List;
import java.util.Locale;

public class NetBlockerMainActivity extends ActivityBase implements PopupMenu.OnMenuItemClickListener {
    private static final String TAG = "Firewall.Main";


    private ProgressBar loadingBar;
    SearchView searchView;
    private ImageView btnMenu;

    private boolean running = false;
    private AppsNetBlockerAdapter adapter = null;
    private MenuItem searchItem = null;

    private static final int REQUEST_VPN = 1;

    public ToggleButton startStopVpn;



    public ImageView headerItemCenterRight;
    public TextView headerItemTextViewFirst;
    public TextView headerItemTextViewSecond;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarGradient(this, R.color.colorWhite, R.color.colorWhite);
        setContentView(R.layout.activity_apps_net_blocker);
        setUpHeader();
        initViews();

    }

    private void initViews() {
        loadingBar = findViewById(R.id.acNetBlock_loadingBar);
        searchView = (SearchView) findViewById(R.id.acNetBlock_searchView);
        btnMenu = findViewById(R.id.acNetBlock_btnMenu);
        startStopVpn = findViewById(R.id.btnStartStopVnp);
        if (isVpnServiceRunning()) {
            startStopVpn.setChecked(true);
        } else {
            startStopVpn.setChecked(false);
        }
        add_All_Application_RCV();
        setUpSearchViewListeners();
        startStopVpn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    startVpnService();

                } else {
                    stopVpnService();
                }
            }
        });
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showPopUpMenu(view);
            }
        });

        running = true;

        // Listen for connectivity updates
        IntentFilter ifConnectivity = new IntentFilter();
        ifConnectivity.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityChangedReceiver, ifConnectivity);

        // Listen for added/removed applications
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");
        registerReceiver(packageChangedReceiver, intentFilter);
    }

    private void setUpSearchViewListeners() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (adapter != null)
                    adapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null)
                    adapter.getFilter().filter(newText);
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if (adapter != null)
                    adapter.getFilter().filter(null);
                return true;
            }
        });


    }

    void setUpHeader() {

        headerItemCenterRight = findViewById(R.id.header_item_centerRight_imageView);

        headerItemTextViewFirst = findViewById(R.id.header_item_textView_First);
        headerItemTextViewSecond = findViewById(R.id.header_item_textView_Second);
        headerItemTextViewSecond.setVisibility(View.INVISIBLE);
        headerItemCenterRight.setImageResource(R.drawable.ic_header_item_net_block);
        headerItemTextViewFirst.setText(R.string.net_blocker);


    }

    public boolean isVpnServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (AppsNetBlockerService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void startVpnService() {
        Log.i(TAG, "Switch on");
        Intent prepare = VpnService.prepare(NetBlockerMainActivity.this);
        if (prepare == null) {
            Log.e(TAG, "Prepare done");
            onActivityResult(REQUEST_VPN, RESULT_OK, null);
        } else {
            Log.i(TAG, "Start intent=" + prepare);
            try {
                startActivityForResult(prepare, REQUEST_VPN);
            } catch (Throwable ex) {
                Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                onActivityResult(REQUEST_VPN, RESULT_CANCELED, null);
                Toast.makeText(NetBlockerMainActivity.this, ex.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void stopVpnService() {
        AppsNetBlockerService.stopVpnService(NetBlockerMainActivity.this);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Destroy");
        running = false;
        //PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        unregisterReceiver(connectivityChangedReceiver);
        unregisterReceiver(packageChangedReceiver);
        super.onDestroy();
    }

    private BroadcastReceiver connectivityChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received " + intent);
            helperClass.logExtras(TAG, intent);
            invalidateOptionsMenu();
        }
    };

    private BroadcastReceiver packageChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received " + intent);
            helperClass.logExtras(TAG, intent);
            add_All_Application_RCV();
        }
    };

    @SuppressLint("StaticFieldLeak")
    private void add_All_Application_RCV() {
        // Get recycler view
        final RecyclerView rvApplication = findViewById(R.id.rvApplication);
        rvApplication.setHasFixedSize(true);
        rvApplication.setLayoutManager(new LinearLayoutManager(this));

        // Get/set application list
        new AsyncTask<Object, Object, List<Apps_Model_Class>>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loadingBar.setVisibility(View.VISIBLE);
                rvApplication.setVisibility(View.INVISIBLE);
            }

            @Override
            protected List<Apps_Model_Class> doInBackground(Object... arg) {
                return Apps_Model_Class.getListOfApps(NetBlockerMainActivity.this);
            }

            @Override
            protected void onPostExecute(List<Apps_Model_Class> result) {
                if (running) {
                    if (searchItem != null)
                        MenuItemCompat.collapseActionView(searchItem);
                    adapter = new AppsNetBlockerAdapter(result, NetBlockerMainActivity.this);
                    rvApplication.setAdapter(adapter);
                    loadingBar.setVisibility(View.INVISIBLE);
                    rvApplication.setVisibility(View.VISIBLE);
                }
            }
        }.execute();
    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.net_blocker_menu, menu);

        // Search
       *//* searchItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);*//*
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (adapter != null)
                    adapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null)
                    adapter.getFilter().filter(newText);
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if (adapter != null)
                    adapter.getFilter().filter(null);
                return true;
            }
        });

        return true;
    }*/

    private void showPopUpMenu(View v) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        PopupMenu popup = new PopupMenu(NetBlockerMainActivity.this, v);
        popup.setOnMenuItemClickListener(NetBlockerMainActivity.this);

        popup.inflate(R.menu.net_blocker_menu);
        Menu menu = popup.getMenu();

        MenuItem network = menu.findItem(R.id.menu_network);
        network.setIcon(helperClass.isWifiActive(this) ? R.drawable.ic_net_block_wifi_on : R.drawable.ic_net_block_signal_on);
        MenuItem wifi = menu.findItem(R.id.menu_whitelist_wifi);
        wifi.setChecked(prefs.getBoolean("whitelist_wifi", false));

        MenuItem other = menu.findItem(R.id.menu_whitelist_other);
        other.setChecked(prefs.getBoolean("whitelist_other", false));

        popup.show();
    }

  /*  @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        MenuItem network = menu.findItem(R.id.menu_network);
        network.setIcon(helperClass.isWifiActive(this) ? R.drawable.ic_network_wifi_white_24dp : R.drawable.ic_network_cell_white_24dp);

        MenuItem wifi = menu.findItem(R.id.menu_whitelist_wifi);
        wifi.setChecked(prefs.getBoolean("whitelist_wifi", false));

        MenuItem other = menu.findItem(R.id.menu_whitelist_other);
        other.setChecked(prefs.getBoolean("whitelist_other", false));


        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_network:
                Intent settings;
                if (helperClass.isWifiActive(this)) {
                    settings = new Intent(Settings.ACTION_WIFI_SETTINGS);
                } else {
                    settings = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                }
                if (settings.resolveActivity(getPackageManager()) != null)
                    startActivity(settings);
                return true;

//            case R.id.menu_refresh:
//                add_All_Application_RCV();
//                return true;

            case R.id.menu_whitelist_wifi:
                prefs.edit().putBoolean("whitelist_wifi", !prefs.getBoolean("whitelist_wifi", false)).apply();
                add_All_Application_RCV();
                AppsNetBlockerService.refreshVpnService("wifi", this);
                return true;

            case R.id.menu_whitelist_other:
                prefs.edit().putBoolean("whitelist_other", !prefs.getBoolean("whitelist_other", false)).apply();
                add_All_Application_RCV();
                AppsNetBlockerService.refreshVpnService("other", this);
                return true;

            case R.id.menu_vpn_settings:
                // Open VPN settings
                Intent vpn = new Intent("android.net.vpn.SETTINGS");
                if (vpn.resolveActivity(getPackageManager()) != null)
                    startActivity(vpn);
                else
                    Log.w(TAG, vpn + " not available");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }*/

    private void reset(String network) {
        SharedPreferences other = getSharedPreferences(network, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = other.edit();
        for (String key : other.getAll().keySet())
            edit.remove(key);
        edit.apply();
        add_All_Application_RCV();
        AppsNetBlockerService.refreshVpnService(network, NetBlockerMainActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_VPN) {
            // Update enabled state
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putBoolean("enabled", resultCode == RESULT_OK).apply();

            // Start service
            if (resultCode == RESULT_OK)
                AppsNetBlockerService.startVpnService(this);
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        switch (item.getItemId()) {
            case R.id.menu_network:
                Intent settings;
                if (helperClass.isWifiActive(this)) {
                    settings = new Intent(Settings.ACTION_WIFI_SETTINGS);
                } else {
                    settings = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                }
                if (settings.resolveActivity(getPackageManager()) != null)
                    startActivity(settings);
                return true;

//            case R.id.menu_refresh:
//                add_All_Application_RCV();
//                return true;

            case R.id.menu_whitelist_wifi:
                prefs.edit().putBoolean("whitelist_wifi", !prefs.getBoolean("whitelist_wifi", false)).apply();
                add_All_Application_RCV();
                AppsNetBlockerService.refreshVpnService("wifi", this);
                return true;

            case R.id.menu_whitelist_other:
                prefs.edit().putBoolean("whitelist_other", !prefs.getBoolean("whitelist_other", false)).apply();
                add_All_Application_RCV();
                AppsNetBlockerService.refreshVpnService("other", this);
                return true;

            case R.id.menu_vpn_settings:
                // Open VPN settings
                Intent vpn = new Intent("android.net.vpn.SETTINGS");
                if (vpn.resolveActivity(getPackageManager()) != null)
                    startActivity(vpn);
                else
                    Log.w(TAG, vpn + " not available");
                return true;

            default:
                return true;
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        MenuItem network = menu.findItem(R.id.menu_network);
        network.setIcon(helperClass.isWifiActive(this) ? R.drawable.ic_net_block_wifi_on : R.drawable.ic_net_block_signal_on);

        MenuItem wifi = menu.findItem(R.id.menu_whitelist_wifi);
        wifi.setChecked(prefs.getBoolean("whitelist_wifi", false));

        MenuItem other = menu.findItem(R.id.menu_whitelist_other);
        other.setChecked(prefs.getBoolean("whitelist_other", false));


        Log.d(TAG, "onPrepareOptionsMenu: Called");
        return super.onPrepareOptionsMenu(menu);
    }
}
