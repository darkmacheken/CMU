package pt.ulisboa.tecnico.cmu;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmu.adapters.AlbumMenuAdapter;
import pt.ulisboa.tecnico.cmu.dataobjects.Album;

public class AlbumMenuActivity extends AppCompatActivity {

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

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.album_list);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        List<Album> albumList = getAlbums();
        AlbumMenuAdapter albumMenuAdapter = new AlbumMenuAdapter(albumList, getApplicationContext());
        recyclerView.setAdapter(albumMenuAdapter);
    }

    private List<Album> getAlbums() {
        List<Album> albums = new ArrayList<Album>();
        albums.add(new Album(1, "Hardware"));
        albums.add(new Album(2, "Destiny Game"));
        albums.add(new Album(3, "Trains"));
        albums.add(new Album(4, "Xenomorphs"));
        return albums;
    }
}
