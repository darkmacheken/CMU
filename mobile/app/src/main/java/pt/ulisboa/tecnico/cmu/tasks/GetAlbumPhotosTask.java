package pt.ulisboa.tecnico.cmu.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build.VERSION_CODES;
import android.support.annotation.RequiresApi;
import android.util.Log;
import com.google.android.gms.tasks.Tasks;
import com.google.gson.Gson;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import pt.ulisboa.tecnico.cmu.activities.MainActivity;
import pt.ulisboa.tecnico.cmu.adapters.ViewAlbumAdapter;
import pt.ulisboa.tecnico.cmu.dataobjects.Album;
import pt.ulisboa.tecnico.cmu.utils.GoogleDriveUtils;
import pt.ulisboa.tecnico.cmu.utils.RequestsUtils.State;
import pt.ulisboa.tecnico.cmu.utils.SharedPropertiesUtils;

public class GetAlbumPhotosTask extends AsyncTask<Void, Boolean, State> {

    private static final String TAG = "GetAlbumPhotosTask";
    private final Context context;
    private final ViewAlbumAdapter viewAlbumAdapter;
    ;
    private Album album;


    public GetAlbumPhotosTask(Context context, ViewAlbumAdapter viewAlbumAdapter, Album album) {
        this.context = context;
        this.viewAlbumAdapter = viewAlbumAdapter;
        this.album = album;
    }

    @Override
    protected void onPreExecute() {
        GoogleDriveUtils.connectDriveService(context);
    }

    @RequiresApi(api = VERSION_CODES.N)
    @Override
    protected State doInBackground(Void... noParams) {
        File mediaStorageDir = context.getCacheDir();
        File albumFolder = new File(mediaStorageDir, this.album.getId());
        File[] filesArray = albumFolder.listFiles();
        filesArray = Optional.ofNullable(filesArray).orElseGet(() -> new File[]{});

        Map<String, File> files = Arrays.stream(filesArray).collect(Collectors.toMap(File::getName, f -> f));

        List<String> imagesList = new ArrayList<>();

        // Retrieving metadata
        List<String> finalImagesList = imagesList;
        this.album.getUsers()
            .forEach(link -> {
                try {
                    String metaDataFile = Tasks.await(GoogleDriveUtils.readFile(link.getFileId()));
                    String[] images = new Gson().fromJson(metaDataFile, String[].class);
                    finalImagesList.addAll(Arrays.asList(images));
                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG, "Error waiting for tasks.", e);
                }
            });

        if (!imagesList.isEmpty()) {
            SharedPropertiesUtils.saveAlbumMetadata(context, album.getId(), new Gson().toJson(imagesList));
        } else {
            imagesList = Arrays.asList(
                new Gson().fromJson(SharedPropertiesUtils.getAlbumMetadata(context, album.getId()), String[].class));
        }

        imagesList.forEach(image -> {
            if (!files.containsKey(image)) {
                GoogleDriveUtils.downloadFile(image, new File(context.getCacheDir(), this.album.getId()))
                    .addOnCompleteListener((fileResult) -> {
                        if (fileResult.isSuccessful()) {
                            viewAlbumAdapter.addPhoto(fileResult.getResult().getAbsolutePath());
                        }
                    });
            }
        });
        return State.SUCCESS;
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
    }
}