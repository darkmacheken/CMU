package pt.ulisboa.tecnico.cmu.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import java.io.IOException;
import java.util.Arrays;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pt.ulisboa.tecnico.cmu.R;
import pt.ulisboa.tecnico.cmu.tasks.LoginTask;
import pt.ulisboa.tecnico.cmu.utils.AlertUtils;
import pt.ulisboa.tecnico.cmu.utils.GoogleDriveUtils;
import pt.ulisboa.tecnico.cmu.utils.HttpUtils;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    // UI references.
    private View mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hide action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        new LoginTask(this).execute();

        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        GoogleSignIn.getSignedInAccountFromIntent(resultData)
            .addOnSuccessListener(googleAccount -> {
                Log.d(TAG, "Signed in as " + googleAccount.getEmail());

                // Use the authenticated account to sign in to the Drive service.
                GoogleAccountCredential credential =
                    GoogleAccountCredential.usingOAuth2(
                        this, Arrays.asList(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA));
                credential.setSelectedAccount(googleAccount.getAccount());
                Drive googleDriveService =
                    new Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName("P2Photo")
                        .build();

                // The GoogleDriveUtils encapsulates all REST API and SAF functionality.
                // Its instantiation is required before handling any onClick actions.
                GoogleDriveUtils.setGoogleDriveService(googleDriveService);
            })
            .addOnFailureListener(exception -> {
                Log.e(TAG, "Unable to sign in.", exception);
                AlertUtils.alert("Unable to sign in.", this);
                showProgress(false);
            });

        super.onActivityResult(requestCode, resultCode, resultData);
    }

    /**
     * Starts a G drive sign-in activity .
     */
    private void requestGdriveSignIn() {
        Log.d(TAG, "Requesting sign-in");

        GoogleSignInOptions signInOptions =
            new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getResources().getString(R.string.server_id))
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE), new Scope(DriveScopes.DRIVE_APPDATA))
                .build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);

        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(client.getSignInIntent(), 0);
    }

    /**
     * Called when the user taps the Cloud Storage button
     */
    public void startLoginActivity(View view) {
        requestGdriveSignIn();
        showProgress(true);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
