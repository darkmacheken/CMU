package pt.ulisboa.tecnico.cmu.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.gson.Gson;
import dmax.dialog.SpotsDialog;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import pt.ulisboa.tecnico.cmu.R;
import pt.ulisboa.tecnico.cmu.adapters.ViewAlbumAdapter;
import pt.ulisboa.tecnico.cmu.dataobjects.Album;
import pt.ulisboa.tecnico.cmu.dataobjects.Link;
import pt.ulisboa.tecnico.cmu.tasks.GetAlbumPhotosTask;
import pt.ulisboa.tecnico.cmu.utils.AlertUtils;
import pt.ulisboa.tecnico.cmu.utils.GoogleDriveUtils;
import pt.ulisboa.tecnico.cmu.utils.SharedPropertiesUtils;

public class ViewAlbumActivity extends AppCompatActivity {

    private static final String TAG = "ViewAlbumActivity";
    private static final int GALLERY = 1;
    // UI components
    private AlertDialog progress;
    private ViewAlbumAdapter viewAlbumAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Album album;
    private Link userLink;

    @RequiresApi(api = VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_album);

        // Get album
        Gson gson = new Gson();
        String albumDataObjectAsAString = getIntent().getStringExtra("album");
        this.album = gson.fromJson(albumDataObjectAsAString, Album.class);

        // get user's Link
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        String userId = account.getId();
        Optional<Link> linkOp = album.getUsers().stream().filter(link -> userId.equals(link.getUserId())).findFirst();
        this.userLink = linkOp.orElseGet(Link::new);

        // progress bar
        progress = new SpotsDialog.Builder().setContext(this)
            .setMessage("Uploading image.")
            .setCancelable(false)
            .setTheme(R.style.ProgressBar)
            .build();

        setTitle(this.album.getName());
        setupActionBar();

        RecyclerView recyclerView = findViewById(R.id.photo_list);
        recyclerView.setHasFixedSize(true);

        layoutManager = new GridLayoutManager(ViewAlbumActivity.this, 3);
        recyclerView.setLayoutManager(layoutManager);
        viewAlbumAdapter = new ViewAlbumAdapter(getPhotos(), ViewAlbumActivity.this);
        recyclerView.setAdapter(viewAlbumAdapter);

        new GetAlbumPhotosTask(this, viewAlbumAdapter, album).execute();

        Context thisContext = this;
        SwipeRefreshLayout pullToRefresh = findViewById(R.id.swipeContainer);
        pullToRefresh.setOnRefreshListener(() -> {
            new GetAlbumPhotosTask(thisContext, viewAlbumAdapter, album).execute();
            pullToRefresh.setRefreshing(false);
        });
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

    private List<String> getPhotos() {
        File albumFolder = new File(this.getCacheDir(), this.album.getId());

        if (!albumFolder.exists()) {
            if (!albumFolder.mkdirs()) {
                Log.d(TAG, "failed to create " + this.album.getId() + " directory");
            }
        }

        File[] files = albumFolder.listFiles();
        List<String> photos = new ArrayList<>();

        for (File file : files) {
            photos.add(file.getAbsolutePath());
        }
        return photos;
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

            showProgress(true);
            GoogleDriveUtils.createFile(new File(getRealPathFromURI(contentURI)), userLink.getFolderId())
                .addOnCompleteListener(result -> {
                    if (result.isSuccessful()) {
                        String[] imagesArray = new Gson().fromJson(
                            SharedPropertiesUtils.getAlbumMetadata(this, album.getId()), String[].class);

                        List<String> imagesList = new ArrayList<>();
                        if (imagesArray != null && imagesArray.length != 0) {
                            imagesList = new ArrayList<>(Arrays.asList(imagesArray));
                        }

                        imagesList.add(result.getResult());

                        String metadata = new Gson().toJson(imagesList);

                        GoogleDriveUtils.updateFile(userLink.getFileId(), metadata).addOnCompleteListener(result2 -> {
                            SharedPropertiesUtils.saveAlbumMetadata(this, album.getId(), metadata);
                        }).addOnFailureListener(e -> Log.e(TAG, "Unable update metadata file.", e));

                        File pathAlbum = new File(getCacheDir(), album.getId());
                        File photo = new File(pathAlbum, result.getResult());
                        GoogleDriveUtils.copy(new File(getRealPathFromURI(contentURI)), photo);
                        viewAlbumAdapter.addPhoto(photo.getAbsolutePath());
                        ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(0, 0);
                    }

                    showProgress(false);
                }).addOnFailureListener(e -> AlertUtils.alert("Unable to upload the photo.", this));

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

    private void showProgress(boolean show) {
        if (show) {
            progress.show();
        } else {
            progress.dismiss();
        }
    }
}