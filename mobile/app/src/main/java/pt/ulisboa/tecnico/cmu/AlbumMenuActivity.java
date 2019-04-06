package pt.ulisboa.tecnico.cmu;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmu.adapters.AlbumMenuAdapter;
import pt.ulisboa.tecnico.cmu.dataobjects.Album;

public class AlbumMenuActivity extends AppCompatActivity {

    private AlbumMenuAdapter albumMenuAdapter;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_menu);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(false); // disable the button
            actionBar.setDisplayHomeAsUpEnabled(false); // remove the left caret
            actionBar.setDisplayShowHomeEnabled(false); // remove the icon
        }

        RecyclerView recyclerView = findViewById(R.id.album_list);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        List<Album> albumList = getAlbums();
        albumMenuAdapter = new AlbumMenuAdapter(albumList);
        recyclerView.setAdapter(albumMenuAdapter);
    }

    private List<Album> getAlbums() {
        List<Album> albums = new ArrayList<>();
        albums.add(new Album(1, "Hardware"));
        albums.add(new Album(2, "Destiny Game"));
        albums.add(new Album(3, "Trains"));
        albums.add(new Album(4, "Xenomorphs"));
        return albums;
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
                albumMenuAdapter.addAlbum(new Album(1, "New Album"));
                ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(0, 0);
                return (true);
            case R.id.logout:
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("Are you sure you want to logout?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                finish();
                                startActivity(intent);
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }
}
