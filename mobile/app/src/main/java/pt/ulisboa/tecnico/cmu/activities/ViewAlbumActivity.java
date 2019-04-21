package pt.ulisboa.tecnico.cmu.activities;

import static pt.ulisboa.tecnico.cmu.activities.AlbumMenuActivity.mediaStorageDir;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import pt.ulisboa.tecnico.cmu.R;
import pt.ulisboa.tecnico.cmu.adapters.ViewAlbumAdapter;

public class ViewAlbumActivity extends AppCompatActivity {

    private static final int GALLERY = 1;
    private ViewAlbumAdapter viewAlbumAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private String albumName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_album);
        this.albumName = getIntent().getBundleExtra("album").getString("name");
        setTitle(this.albumName);
        setupActionBar();

        RecyclerView recyclerView = findViewById(R.id.photo_list);
        recyclerView.setHasFixedSize(true);

        layoutManager = new GridLayoutManager(ViewAlbumActivity.this, 3);
        recyclerView.setLayoutManager(layoutManager);
        viewAlbumAdapter = new ViewAlbumAdapter(getPhotos(), ViewAlbumActivity.this);
        recyclerView.setAdapter(viewAlbumAdapter);
    }

    private List<String> getPhotos() {

        File albumFolder = new File(mediaStorageDir, this.albumName);

        if (!albumFolder.exists()) {
            if (!albumFolder.mkdirs()) {
                Log.d("App", "failed to create " + this.albumName + " directory");
            }
        }

        File[] files = albumFolder.listFiles();
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
                choosePhotoFromGallery();
                return (true);
            case R.id.add_user:
                Intent intent = new Intent(this, AddUserActivity.class);
                startActivity(intent);
                return (true);
            default:
                return (super.onOptionsItemSelected(item));
        }
    }

    private void choosePhotoFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED && requestCode == GALLERY && data != null) {
            Uri contentURI = data.getData();
            viewAlbumAdapter.addPhoto(getRealPathFromURI(contentURI));
            ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(0, 0);
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}