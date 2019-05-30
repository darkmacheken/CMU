package pt.ulisboa.tecnico.cmu.tasks;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import dmax.dialog.SpotsDialog;
import pt.ulisboa.tecnico.cmu.R;
import pt.ulisboa.tecnico.cmu.activities.MainActivity;
import pt.ulisboa.tecnico.cmu.dataobjects.User;
import pt.ulisboa.tecnico.cmu.exceptions.UnauthorizedException;
import pt.ulisboa.tecnico.cmu.utils.AlertUtils;
import pt.ulisboa.tecnico.cmu.utils.RequestsUtils;
import pt.ulisboa.tecnico.cmu.utils.RequestsUtils.State;

public class CreateAlbumsTask extends AsyncTask<Void, Void, State> {

    private final Activity context;
    private final String name;
    private final User[] users;
    // UI components
    private AlertDialog progress;

    public CreateAlbumsTask(Activity context, String name, User[] users) {
        this.context = context;
        this.name = name;
        this.users = users;
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
            if (MainActivity.choseWifiDirect) {
                return RequestsUtils.createAlbumWifi(context, name, users) ? State.SUCCESS : State.NOT_SUCCESS;
            } else {
                return RequestsUtils.createAlbum(context, name, users) ? State.SUCCESS : State.NOT_SUCCESS;
            }
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
        } else if (state == State.NOT_SUCCESS) {
            showProgress(false);
            AlertUtils.alert("There was an error creating album.", context);
            context.setResult(RESULT_CANCELED);
        } else {
            context.setResult(RESULT_OK);
        }

        context.finish();
    }

    private void showProgress(boolean show) {
        if (show) {
            progress.show();
        } else {
            progress.dismiss();
        }
    }

}