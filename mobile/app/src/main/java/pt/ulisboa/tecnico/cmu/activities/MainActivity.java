package pt.ulisboa.tecnico.cmu.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
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
import com.google.api.services.drive.Drive.Builder;
import com.google.api.services.drive.DriveScopes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import pt.ulisboa.tecnico.cmu.R;
import pt.ulisboa.tecnico.cmu.tasks.LoginTask;
import pt.ulisboa.tecnico.cmu.tasks.WifiDirectConnectionManager;
import pt.ulisboa.tecnico.cmu.utils.GoogleDriveUtils;
import pt.ulisboa.tecnico.cmu.utils.SharedPropertiesUtils;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String[] permissions = {
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_INTERNAL_STORAGE",
        "android.permission.READ_INTERNAL_STORAGE",
        "android.permission.INTERNET"
    };
    private static final int NOT_FORCE_LOGIN = 0;
    private static final int FORCE_LOGIN = 1;

    public static boolean choseWifiDirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hide action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.getBoolean("startLogin")) {
            requestGdriveSignIn(FORCE_LOGIN);
        }

        if (!arePermissionsEnabled()) {
            requestMultiplePermissions();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        final boolean forceLogin = (requestCode == FORCE_LOGIN);

        final Context thisContext = this;
        GoogleSignIn.getSignedInAccountFromIntent(resultData)
            .addOnSuccessListener(googleAccount -> {
                Log.d(TAG, "Signed in as " + googleAccount.getEmail());

                SharedPropertiesUtils.saveLastLoginId(thisContext, googleAccount.getId());
                new LoginTask(this, googleAccount).execute(forceLogin);

                if (!MainActivity.choseWifiDirect) {
                    // Use the authenticated account to sign in to the Drive service.
                    GoogleAccountCredential credential =
                        GoogleAccountCredential.usingOAuth2(
                            this,
                            Arrays
                                .asList(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE_READONLY));
                    credential.setSelectedAccount(googleAccount.getAccount());

                    Drive googleDriveService =
                        new Builder(
                            AndroidHttp.newCompatibleTransport(),
                            new GsonFactory(),
                            credential)
                            .setApplicationName("P2Photo")
                            .build();

                    // The GoogleDriveUtils encapsulates all REST API and SAF functionality.
                    // Its instantiation is required before handling any onClick actions.
                    GoogleDriveUtils.setGoogleDriveService(googleDriveService);
                }
            })
            .addOnFailureListener(exception -> {
                Log.e(TAG, "Unable to sign in.", exception);
                new LoginTask(this, null).execute(false);
            });

    }

    /**
     * Starts a G drive sign-in activity .
     */
    private void requestGdriveSignIn(int forceLogin) {
        Log.d(TAG, "Requesting sign-in");
        GoogleSignInOptions signInOptions;
        if (MainActivity.choseWifiDirect) {
            signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getResources().getString(R.string.server_id))
                    .requestServerAuthCode(getResources().getString(R.string.server_id))
                    .requestEmail()
                    .build();
        } else {
            signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getResources().getString(R.string.server_id))
                    .requestServerAuthCode(getResources().getString(R.string.server_id))
                    .requestEmail()
                    .requestScopes(new Scope(DriveScopes.DRIVE_FILE), new Scope(DriveScopes.DRIVE_APPDATA),
                        new Scope(DriveScopes.DRIVE_READONLY))
                    .build();
        }
        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);

        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(client.getSignInIntent(), forceLogin);
    }

    /**
     * Called when the user taps the Cloud Storage button
     */
    public void startCloudStorageLoginActivity(View view) {
        MainActivity.choseWifiDirect = false;
        requestGdriveSignIn(NOT_FORCE_LOGIN);
    }

    /**
     * Called when the user taps the Cloud Storage button
     */
    public void startWifiDirectLoginActivity(View view) {
        MainActivity.choseWifiDirect = true;
        WifiDirectConnectionManager.init();
        requestGdriveSignIn(NOT_FORCE_LOGIN);
    }


    private boolean arePermissionsEnabled() {
        for (String permission : permissions) {
            if (VERSION.SDK_INT >= VERSION_CODES.M &&
                checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }

        }
        return true;
    }

    private void requestMultiplePermissions() {
        List<String> remainingPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (VERSION.SDK_INT >= VERSION_CODES.M &&
                checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                remainingPermissions.add(permission);

            }
        }
        if (VERSION.SDK_INT >= VERSION_CODES.M) {
            requestPermissions(remainingPermissions.toArray(new String[remainingPermissions.size()]), 101);
        }
    }

}
