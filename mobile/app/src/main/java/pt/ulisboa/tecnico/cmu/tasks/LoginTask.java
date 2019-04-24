package pt.ulisboa.tecnico.cmu.tasks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import pt.ulisboa.tecnico.cmu.activities.AlbumMenuActivity;
import pt.ulisboa.tecnico.cmu.exceptions.UserNotFoundException;
import pt.ulisboa.tecnico.cmu.utils.HttpUtils;

public class LoginTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = "LoginTask";

    private final GoogleSignInAccount googleAccount;

    private final Context context;

    public LoginTask(Context context, GoogleSignInAccount googleAccount) {
        this.context = context;
        this.googleAccount = googleAccount;
    }

    protected Boolean doInBackground(Void... urls) {
        String token = null;
        try {
            token = HttpUtils.login(context, googleAccount.getId(), googleAccount.getIdToken());
        } catch (UserNotFoundException e) {
            boolean success = HttpUtils.register(context, googleAccount.getId(), googleAccount.getDisplayName(),
                googleAccount.getEmail());

            // try login again
            if (success) {
                try {
                    token = HttpUtils.login(context, googleAccount.getId(), googleAccount.getIdToken());
                } catch (UserNotFoundException e1) {
                    return false;
                }
            }
        }

        if (token != null) {
            SharedPreferences sp = context.getSharedPreferences("Login", Context.MODE_PRIVATE);
            SharedPreferences.Editor ed = sp.edit();
            ed.putString("token", token);
            ed.apply();
            return true;
        }

        return false;
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
    }
}