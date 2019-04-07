package pt.ulisboa.tecnico.cmu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.users.FullAccount;


import pt.ulisboa.tecnico.cmu.utils.AlertUtils;
import pt.ulisboa.tecnico.cmu.utils.DropboxUtils;
import pt.ulisboa.tecnico.cmu.utils.InputValidationUtils;
import pt.ulisboa.tecnico.cmu.tasks.UserAccountTask;

public class SignUpActivity extends AppCompatActivity {

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;
    private View mProgressView;
    private View mSignUpFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setupActionBar();
        // Set up the login form.
        mEmailView = findViewById(R.id.email);

        mPasswordView = findViewById(R.id.password);
        mConfirmPasswordView = findViewById(R.id.confirm_password);
        mConfirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptSignUp();
                    return true;
                }
                return false;
            }
        });

        Button mSignUpButton = findViewById(R.id.sign_up_button);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignUp();
            }
        });

        mSignUpFormView = findViewById(R.id.sign_up_form);
        mProgressView = findViewById(R.id.sign_up_progress);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void attemptSignUp() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mConfirmPasswordView.setError(null);

        // Store values at the time of the sign up attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String confirmPassword = mConfirmPasswordView.getText().toString();

        View focusView;

        focusView = InputValidationUtils.validateConfirmPassword(null, password, confirmPassword,
                mConfirmPasswordView, SignUpActivity.this);
        focusView = InputValidationUtils.validatePassword(focusView, password, mPasswordView,
                SignUpActivity.this);
        focusView = InputValidationUtils.validateEmail(focusView, email, mEmailView,
                SignUpActivity.this);

        if (focusView != null) {
            // There was an error; don't attempt sign up and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            //Start dropbox auth activity
            Auth.startOAuth2Authentication(SignUpActivity.this, getString(R.string.app_key));

            // Show a progress spinner, and kick off a background task to
            // perform the user sign up attempt.
            showProgress(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAccessToken();
    }

    public void getAccessToken() {
        String accessToken = Auth.getOAuth2Token(); //generate Access Token
        if (accessToken != null) {
            String accountId;

            //Store accessToken in SharedPreferences
            SharedPreferences prefs = getSharedPreferences(getString(R.string.cmu_package), Context.MODE_PRIVATE);
            prefs.edit().putString("access-token", accessToken).apply();

            accountId = getUserAccount(accessToken);

            Log.d("User", "Account id received: " + accountId);
        }
    }

    private String getUserAccount(String accessToken) {
        if (accessToken == null) {
            return "";
        }

        FullAccount account = null;
        try {
            account = new UserAccountTask(DropboxUtils.getClient(accessToken),
                    new UserAccountTask.TaskDelegate() {
                        @Override
                        public void onError(Exception e) {
                            Log.d("User", "Error: " + e.getMessage());
                            restartActivity();
                        }
                    }).execute().get();
        } catch (Exception e) {
            Log.d("User", "Error: " + e.getMessage());
            restartActivity();
        }

        if (account == null) {
            return "";
        } else {
            return account.getAccountId();
        }
    }

    private void restartActivity() {
        AlertUtils.alert("Error receiving dropbox account details.", SignUpActivity.this);
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mSignUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mSignUpFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSignUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mSignUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}
