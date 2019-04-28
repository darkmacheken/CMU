package pt.ulisboa.tecnico.cmu.activities;

import android.Manifest.permission;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import pt.ulisboa.tecnico.cmu.R;
import pt.ulisboa.tecnico.cmu.adapters.AlbumMenuAdapter;
import pt.ulisboa.tecnico.cmu.tasks.GetAlbumsTask;
import pt.ulisboa.tecnico.cmu.utils.GoogleDriveUtils;

@TargetApi(VERSION_CODES.JELLY_BEAN)
public class AlbumMenuActivity extends AppCompatActivity {

    private static final String TAG = "AlbumMenuActivity";
    private static final int ADD_ALBUM_REQUEST = 1;

    private static final String[] permissions = {permission.WRITE_EXTERNAL_STORAGE, permission.READ_EXTERNAL_STORAGE};
    private AlbumMenuAdapter albumMenuAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @TargetApi(VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_menu);
        setupActionBar();

        RecyclerView recyclerView = findViewById(R.id.album_list);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(AlbumMenuActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        albumMenuAdapter = new AlbumMenuAdapter(new ArrayList<>(), AlbumMenuActivity.this);
        recyclerView.setAdapter(albumMenuAdapter);

        if (!arePermissionsEnabled()) {
            requestMultiplePermissions();
        }

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "P2Photo");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create albums directory");
            }
        }

        GoogleDriveUtils.queryFiles();

        new GetAlbumsTask(this, albumMenuAdapter).execute();
        Context thisContext = this;
        SwipeRefreshLayout pullToRefresh = findViewById(R.id.swipeContainer);
        pullToRefresh.setOnRefreshListener(() -> {
            new GetAlbumsTask(thisContext, albumMenuAdapter).execute();
            pullToRefresh.setRefreshing(false);
        });

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

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean arePermissionsEnabled() {
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestMultiplePermissions() {
        List<String> remainingPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                remainingPermissions.add(permission);
            }
        }
        requestPermissions(remainingPermissions.toArray(new String[remainingPermissions.size()]), 101);
    }
}
