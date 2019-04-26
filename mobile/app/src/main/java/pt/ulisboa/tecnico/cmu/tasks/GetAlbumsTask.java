package pt.ulisboa.tecnico.cmu.tasks;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import dmax.dialog.SpotsDialog;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import pt.ulisboa.tecnico.cmu.R;
import pt.ulisboa.tecnico.cmu.activities.MainActivity;
import pt.ulisboa.tecnico.cmu.adapters.AlbumMenuAdapter;
import pt.ulisboa.tecnico.cmu.dataobjects.Album;
import pt.ulisboa.tecnico.cmu.exceptions.UnauthorizedException;
import pt.ulisboa.tecnico.cmu.utils.RequestsUtils;

public class GetAlbumsTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = "GetAlbumsTask";
    private final Context context;

    private final AlbumMenuAdapter adapter;

    // UI components
    private AlertDialog progress;
    private List<Album> albums;


    public GetAlbumsTask(Context context, AlbumMenuAdapter adapter) {
        this.context = context;
        this.adapter = adapter;
    }

    @Override
    protected void onPreExecute() {
        progress = new SpotsDialog.Builder().setContext(context)
            .setMessage("Retrieving albums.")
            .setCancelable(false)
            .setTheme(R.style.ProgressBar)
            .build();
        showProgress(true);
    }

    @Override
    protected Boolean doInBackground(Void... noParams) {
        try {
            String albumsJson = RequestsUtils.getAlbums(context);

            ObjectMapper mapper = new ObjectMapper();
            Album[] albumsFromJson = mapper.readValue(albumsJson, Album[].class);

            this.albums = Arrays.asList(albumsFromJson);
            return true;
        } catch (UnauthorizedException e) {
            return false;
        } catch (IOException e) {
            Log.e(TAG, "Unable parse JSON of albums.", e);
            return true;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            adapter.addAlbums(this.albums);
        } else {
            Intent launchNextActivity;
            launchNextActivity = new Intent(context, MainActivity.class);
            launchNextActivity.putExtra("startLogin", true);
            launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            context.startActivity(launchNextActivity);
        }
        showProgress(false);
    }

    private void showProgress(boolean show) {
        if (show) {
            progress.show();
        } else {
            progress.dismiss();
        }
    }
}