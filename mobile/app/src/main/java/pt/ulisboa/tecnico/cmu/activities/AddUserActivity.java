package pt.ulisboa.tecnico.cmu.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Tasks;
import dmax.dialog.SpotsDialog;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import pt.ulisboa.tecnico.cmu.R;
import pt.ulisboa.tecnico.cmu.adapters.UserButtonListAdapter;
import pt.ulisboa.tecnico.cmu.dataobjects.User;
import pt.ulisboa.tecnico.cmu.utils.RequestsUtils;

public class AddUserActivity extends AppCompatActivity {

    private UserButtonListAdapter userButtonListAdapter;
    private GoogleSignInAccount googleAccount;
    private AlertDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        setupActionBar();

        progress = new SpotsDialog.Builder().setContext(this)
            .setMessage("Searching user.")
            .setCancelable(false)
            .setTheme(R.style.ProgressBar)
            .build();

        this.googleAccount = GoogleSignIn.getLastSignedInAccount(this);

        RecyclerView recyclerView = findViewById(R.id.user_list);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(AddUserActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        List<User> userList = new ArrayList<>();
        userButtonListAdapter = new UserButtonListAdapter(userList, this);
        recyclerView.setAdapter(userButtonListAdapter);

        EditText nameOfUserView = findViewById(R.id.name_of_user);

        Button searchUserButton = findViewById(R.id.search_user_button);
        searchUserButton.setOnClickListener(view -> {
            search(nameOfUserView.getText().toString());

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        });
    }

    private void search(String q) {
        showProgress(true);
        Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            User[] users = RequestsUtils.getUsers(this, googleAccount.getId(), q);

            List<User> usersList = new ArrayList<>();
            for (User user : users) {
                if (!user.getId().equals(googleAccount.getId())) {
                    usersList.add(user);
                }
            }

            userButtonListAdapter.setUserList(usersList);
            return null;
        }).addOnCompleteListener(result -> showProgress(false));
    }


    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true); // disable the button
            actionBar.setDisplayHomeAsUpEnabled(true); // remove the left caret
            actionBar.setDisplayShowHomeEnabled(true); // remove the icon
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return (super.onOptionsItemSelected(item));
        }
    }

    private void showProgress(boolean show) {
        if (show) {
            progress.show();
        } else {
            progress.dismiss();
        }
    }
}
