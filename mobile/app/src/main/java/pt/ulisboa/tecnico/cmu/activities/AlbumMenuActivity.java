package pt.ulisboa.tecnico.cmu.activities;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import java.util.ArrayList;
import java.util.List;
import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.Channel;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.GroupInfoListener;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.PeerListListener;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmu.R;
import pt.ulisboa.tecnico.cmu.adapters.AlbumMenuAdapter;
import pt.ulisboa.tecnico.cmu.tasks.GetAlbumsTask;
import pt.ulisboa.tecnico.cmu.tasks.WifiDirectConnectionManager;
import pt.ulisboa.tecnico.cmu.utils.SharedPropertiesUtils;
import pt.ulisboa.tecnico.cmu.utils.SimWifiP2pBroadcastReceiver;

public class AlbumMenuActivity extends AppCompatActivity implements PeerListListener, GroupInfoListener {

    private static final int ADD_ALBUM_REQUEST = 1;
    private static final String TAG = "AlbumMenuActivity";

    private AlbumMenuAdapter albumMenuAdapter;
    private SimWifiP2pBroadcastReceiver mReceiver;
    private Messenger mService = null;
    private SimWifiP2pManager mManager = null;
    private Channel mChannel = null;
    private boolean mBound = false;
    public static SimWifiP2pSocketServer mSrvSocket = null;
    private IntentFilter intentFilter;
    private boolean alreadyRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_menu);
        setupActionBar();

        RecyclerView recyclerView = findViewById(R.id.album_list);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(AlbumMenuActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        albumMenuAdapter = new AlbumMenuAdapter(new ArrayList<>(), AlbumMenuActivity.this);
        recyclerView.setAdapter(albumMenuAdapter);

        new GetAlbumsTask(this, albumMenuAdapter).execute();

        Context thisContext = this;
        SwipeRefreshLayout pullToRefresh = findViewById(R.id.swipeContainer);
        pullToRefresh.setOnRefreshListener(() -> {
            new GetAlbumsTask(thisContext, albumMenuAdapter).execute();
            pullToRefresh.setRefreshing(false);
        });
        if (MainActivity.choseWifiDirect) {
            setupWifiDirect();
        }
    }

    private void setupWifiDirect() {
        // initialize the WDSim API
        SimWifiP2pSocketManager.Init(getApplicationContext());

        // register broadcast receiver
        intentFilter = new IntentFilter();
        intentFilter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        intentFilter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
        mReceiver = new SimWifiP2pBroadcastReceiver(this);
        registerReceiver(mReceiver, intentFilter);
        Intent intent = new Intent(this, SimWifiP2pService.class);

        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mBound = true;
    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alreadyRegistered) {
            unregisterReceiver(mReceiver);
            alreadyRegistered = false;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        // callbacks for service binding, passed to bindService()

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mManager = new SimWifiP2pManager(mService);
            mChannel = mManager.initialize(getApplication(), getMainLooper(), null);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            mManager = null;
            mChannel = null;
            mBound = false;
        }
    };

    @Override
    public void onPeersAvailable(SimWifiP2pDeviceList peerDeviceList) {
        List<SimWifiP2pDevice> refreshedPeers = new ArrayList<>(peerDeviceList.getDeviceList());

        StringBuilder peersStr = new StringBuilder();

        if (!refreshedPeers.equals(WifiDirectConnectionManager.peers)) {
            WifiDirectConnectionManager.peers.clear();
            WifiDirectConnectionManager.peers.addAll(refreshedPeers);

            // Perform any other updates needed based on the new list of
            // peers connected to the Wi-Fi P2P network.
        }
    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList simWifiP2pDeviceList, SimWifiP2pInfo simWifiP2pInfo) {
        List<SimWifiP2pDevice> refreshedNetworkPeers = new ArrayList<>();

        SimWifiP2pSocketManager.getSockManager().handleActionDeviceInfoChanged(simWifiP2pDeviceList);
        SimWifiP2pSocketManager.getSockManager().handleActionGroupMembershipChanged(simWifiP2pInfo);
        StringBuilder peersStr = new StringBuilder();

        WifiDirectConnectionManager.thisDevice = simWifiP2pDeviceList.getByName(simWifiP2pInfo.getDeviceName());

        for (String deviceName : simWifiP2pInfo.getDevicesInNetwork()) {
            refreshedNetworkPeers.add(simWifiP2pDeviceList.getByName(deviceName));
        }

        // compile list of devices in range
        for (SimWifiP2pDevice device : refreshedNetworkPeers) {
            String devstr = "Group Info: " + device.deviceName + " (" + device.getVirtIp() + ")\n";
            peersStr.append(devstr);
        }
        Log.d(TAG, peersStr.toString());

        if (!refreshedNetworkPeers.equals(WifiDirectConnectionManager.networkPeers)) {
            WifiDirectConnectionManager.networkPeers.clear();
            WifiDirectConnectionManager.networkPeers.addAll(refreshedNetworkPeers);
            WifiDirectConnectionManager.broadcastCatalogs(this);
        }

        if (refreshedNetworkPeers.isEmpty()) {
            Log.d(TAG, "No devices found in group");
            return;
        }
    }

    public void updatePeers() {
        mManager.requestPeers(mChannel, this);
    }

    public void updateNetworkPeers() {
        mManager.requestGroupInfo(mChannel, this);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(false); // disable the button
            actionBar.setDisplayHomeAsUpEnabled(false); // remove the left caret
            actionBar.setDisplayShowHomeEnabled(false); // remove the icon
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        // Make sure the request was successful
        if (requestCode == ADD_ALBUM_REQUEST && resultCode == RESULT_OK) {
            new GetAlbumsTask(this, albumMenuAdapter).execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_album_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_album:
                Intent intent = new Intent(this, AddAlbumActivity.class);
                startActivityForResult(intent, ADD_ALBUM_REQUEST);
                return (true);
            case R.id.logout:
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("Are you sure you want to logout?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                    (dialog, which) -> {
                        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .build();

                        GoogleSignIn.getClient(this, gso).signOut();
                        SharedPropertiesUtils.saveLastLoginId(this, null);

                        Intent intent1 = new Intent(AlbumMenuActivity.this, MainActivity.class);
                        finish();
                        startActivity(intent1);
                    });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                    (dialog, which) -> dialog.dismiss());
                alertDialog.show();
                return (true);
            default:
                return (super.onOptionsItemSelected(item));
        }
    }
}
