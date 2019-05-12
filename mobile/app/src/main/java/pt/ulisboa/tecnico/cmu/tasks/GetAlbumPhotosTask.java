package pt.ulisboa.tecnico.cmu.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.gson.Gson;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import pt.ulisboa.tecnico.cmu.activities.MainActivity;
import pt.ulisboa.tecnico.cmu.adapters.ViewAlbumAdapter;
import pt.ulisboa.tecnico.cmu.dataobjects.Album;
import pt.ulisboa.tecnico.cmu.dataobjects.Link;
import pt.ulisboa.tecnico.cmu.utils.GoogleDriveUtils;
import pt.ulisboa.tecnico.cmu.utils.RequestsUtils.State;
import pt.ulisboa.tecnico.cmu.utils.SharedPropertiesUtils;

public class GetAlbumPhotosTask extends AsyncTask<Void, String, State> {

    private static final String TAG = "GetAlbumPhotosTask";
    private final Context context;
    private final ViewAlbumAdapter viewAlbumAdapter;
    private final GoogleSignInAccount googleAccount;
    private final Toolbar toolbar;
    private final ProgressBar progressBar;
    private final TextView textView;
    private Album album;


    public GetAlbumPhotosTask(Context context, ViewAlbumAdapter viewAlbumAdapter, Album album, Toolbar toolbar,
        ProgressBar progressBar, TextView textView) {
        this.context = context;
        this.viewAlbumAdapter = viewAlbumAdapter;
        this.album = album;
        this.toolbar = toolbar;
        this.progressBar = progressBar;
        this.textView = textView;
        this.googleAccount = GoogleSignIn.getLastSignedInAccount(context);
    }

    @Override
    protected void onPreExecute() {
        GoogleDriveUtils.connectDriveService(context);
        changeTextView("Preparing for getting photos...");
        showInfo();
    }

    @Override
    protected synchronized State doInBackground(Void... noParams) {
        File mediaStorageDir = context.getCacheDir();
        File albumFolder = new File(mediaStorageDir, this.album.getId());
        File[] filesArray = albumFolder.listFiles();
        if (filesArray == null) {
            filesArray = new File[]{};
        }

        Map<String, File> files = new HashMap<>();
        for (File f : filesArray) {
            if (files.put(f.getName(), f) != null) {
                throw new IllegalStateException("Duplicate key");
            }
        }

        List<String> imagesList = new ArrayList<>();

        publishProgress("Retrieving metadata files...");

        // Retrieving metadata
        retrievingMetadata(imagesList);

        // Check the result
        imagesList = getImagesList(imagesList);

        // Download Photos
        downloadPhotos(files, imagesList);
        return State.SUCCESS;
    }


    @Override
    protected void onProgressUpdate(String... values) {
        showInfo();
        changeTextView(values[0]);
    }

    @Override
    protected void onPostExecute(State state) {
        switch (state) {
            case SUCCESS:
                break;
            case NOT_SUCCESS:
                break;
            case UNAUTHORIZED_REQUEST:
                Intent launchNextActivity;
                launchNextActivity = new Intent(context, MainActivity.class);
                launchNextActivity.putExtra("startLogin", true);
                launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                context.startActivity(launchNextActivity);
                break;
            default:
        }
        hideInfo();
    }

    private void downloadPhotos(Map<String, File> files, List<String> imagesList) {
        int totalDownloads = imagesList.size() - files.size();
        AtomicInteger numDownloads = new AtomicInteger();
        List<Task<?>> tasks = new ArrayList<>();
        for (String image : imagesList) {
            if (!files.containsKey(image)) {
                tasks.add(GoogleDriveUtils.downloadFile(image, new File(context.getCacheDir(), this.album.getId()))
                    .addOnCompleteListener(fileResult -> {
                        if (fileResult.isSuccessful()) {
                            numDownloads.getAndIncrement();
                            publishProgress("Downloaded " + numDownloads + " out of " + totalDownloads + "...");
                            viewAlbumAdapter.addPhoto(fileResult.getResult().getAbsolutePath());

                        }
                    }));
            }
        }

        try {
            Tasks.await(Tasks.whenAllComplete(tasks));
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error waiting for download tasks.", e);
        }
    }

    private List<String> getImagesList(List<String> imagesList) {
        if (!imagesList.isEmpty()) {
            SharedPropertiesUtils.saveAlbumMetadata(context, album.getId(), new Gson().toJson(imagesList));
        } else {
            imagesList = Arrays.asList(
                new Gson().fromJson(SharedPropertiesUtils.getAlbumMetadata(context, album.getId()), String[].class));
        }
        return imagesList;
    }

    private void retrievingMetadata(List<String> imagesList) {
        List<String> finalImagesList = imagesList;
        for (Link link : this.album.getUsers()) {
            try {
                String metaDataFile = Tasks.await(GoogleDriveUtils.readFile(link.getFileId()));
                if (link.getUserId().equals(this.googleAccount.getId())) {
                    SharedPropertiesUtils.saveAlbumUserMetadata(context, googleAccount.getId(), album.getId(),
                        metaDataFile);
                }
                String[] images = new Gson().fromJson(metaDataFile, String[].class);
                finalImagesList.addAll(Arrays.asList(images));
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error waiting for tasks.", e);
            }
        }
    }

    private void showInfo() {
        this.progressBar.setVisibility(View.VISIBLE);
        this.textView.setVisibility(View.VISIBLE);
        this.toolbar.setVisibility(View.VISIBLE);
    }

    private void hideInfo() {
        this.progressBar.setVisibility(View.INVISIBLE);
        this.textView.setVisibility(View.INVISIBLE);
        this.toolbar.setVisibility(View.INVISIBLE);
    }

    private void changeTextView(String text) {
        this.textView.setText(text);
    }
}