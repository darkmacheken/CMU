package pt.ulisboa.tecnico.cmu;

import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmu.adapters.ViewAlbumAdapter;
import pt.ulisboa.tecnico.cmu.utils.AlertUtils;

public class ViewAlbumActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_album);
        setTitle(getIntent().getBundleExtra("album").getString("name"));
        setupActionBar();

        RecyclerView recyclerView = findViewById(R.id.photo_list);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(ViewAlbumActivity.this, 3);
        recyclerView.setLayoutManager(layoutManager);
        ViewAlbumAdapter viewAlbumAdapter = new ViewAlbumAdapter(getPhotos(), ViewAlbumActivity.this);
        recyclerView.setAdapter(viewAlbumAdapter);
    }

    private List<String> getPhotos() {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                .toString() + "/Camera";
        File f = new File(path);
        File files[] = f.listFiles();
        List<String> photos = new ArrayList<>();
        for (File file : files) {
            photos.add(file.getAbsolutePath());
        }
        return photos;
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true); // disable the button
            actionBar.setDisplayHomeAsUpEnabled(true); // remove the left caret
            actionBar.setDisplayShowHomeEnabled(true); // remove the icon
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_album, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_photo:
                AlertUtils.alert("Photo added to album", ViewAlbumActivity.this);
                return (true);
            case R.id.add_user:
                AlertUtils.alert("User added to album", ViewAlbumActivity.this);
                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }
}