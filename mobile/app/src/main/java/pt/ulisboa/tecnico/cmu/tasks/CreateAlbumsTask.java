package pt.ulisboa.tecnico.cmu.tasks;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import dmax.dialog.SpotsDialog;
import pt.ulisboa.tecnico.cmu.R;
import pt.ulisboa.tecnico.cmu.activities.MainActivity;
import pt.ulisboa.tecnico.cmu.exceptions.UnauthorizedException;
import pt.ulisboa.tecnico.cmu.tasks.CreateAlbumsTask.State;
import pt.ulisboa.tecnico.cmu.utils.RequestsUtils;

public class CreateAlbumsTask extends AsyncTask<Void, Void, State> {

    private static final String TAG = "CreateAlbumsTask";
    private final Context context;
    private final String name;
    // UI components
    private AlertDialog progress;

    public CreateAlbumsTask(Context context, String name) {
        this.context = context;
        this.name = name;
    }

    @Override
    protected void onPreExecute() {
        progress = new SpotsDialog.Builder().setContext(context)
            .setMessage("Creating album.")
            .setCancelable(false)
            .setTheme(R.style.ProgressBar)
            .build();
        showProgress(true);
    }

    @Override
    protected State doInBackground(Void... noParams) {
        try {
            return RequestsUtils.createAlbum(context, name) ? State.SUCCESS : State.NOT_SUCCESS;
        } catch (UnauthorizedException e) {
            return State.UNAUTHORIZED_REQUEST;
        }
    }

    @Override
    protected void onPostExecute(State state) {
        if (state == State.UNAUTHORIZED_REQUEST) {
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

    public enum State {UNAUTHORIZED_REQUEST, SUCCESS, NOT_SUCCESS}
}