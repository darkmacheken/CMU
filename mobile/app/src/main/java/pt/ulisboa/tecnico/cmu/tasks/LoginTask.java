package pt.ulisboa.tecnico.cmu.tasks;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import dmax.dialog.SpotsDialog;
import java.io.IOException;
import pt.ulisboa.tecnico.cmu.R;
import pt.ulisboa.tecnico.cmu.activities.AlbumMenuActivity;
import pt.ulisboa.tecnico.cmu.exceptions.UserNotFoundException;
import pt.ulisboa.tecnico.cmu.utils.AlertUtils;
import pt.ulisboa.tecnico.cmu.utils.RequestsUtils;

public class LoginTask extends AsyncTask<Boolean, Void, Boolean> {

    private static final String TAG = "LoginTask";
    private final GoogleSignInAccount googleAccount;
    private final Context context;
    // UI components
    private AlertDialog progress;

    public LoginTask(Context context, GoogleSignInAccount googleAccount) {
        this.context = context;
        this.googleAccount = googleAccount;
    }

    @Override
    protected void onPreExecute() {
        progress = new SpotsDialog.Builder().setContext(context)
            .setMessage("Logging in.")
            .setCancelable(false)
            .setTheme(R.style.ProgressBar)
            .build();
        showProgress(true);
    }

    @Override
    protected Boolean doInBackground(Boolean... forceLogin) {
        String token = null;
        if (forceLogin.length == 1 && !forceLogin[0] || googleAccount == null) {
            token = RequestsUtils.getToken(context, googleAccount.getId());

            // login from last time
            if (token != null) {
                return true;
            }
        }

        try {
            token = RequestsUtils.login(context, googleAccount.getId(), googleAccount.getIdToken());
        } catch (UserNotFoundException e) {
            boolean success = RequestsUtils.register(context, googleAccount.getId(), googleAccount.getDisplayName(),
                googleAccount.getEmail(), googleAccount.getServerAuthCode());

            // try login again
            if (success) {
                try {
                    token = RequestsUtils.login(context, googleAccount.getId(), googleAccount.getIdToken());
                } catch (UserNotFoundException e1) {
                    return false;
                } catch (IOException e1) {
                    AlertUtils.alert("Unable to sign in.", context);
                    Log.e(TAG, "Unable to POST request /login.", e1);
                }
            }
        } catch (IOException e) {
            showProgress(false);
            AlertUtils.alert("Unable to sign in.", context);
            Log.e(TAG, "Unable to POST request /login.", e);
        }

        return token != null;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            Intent launchNextActivity;
            launchNextActivity = new Intent(context, AlbumMenuActivity.class);
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