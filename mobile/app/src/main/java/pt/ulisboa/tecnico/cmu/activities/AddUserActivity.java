package pt.ulisboa.tecnico.cmu.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Tasks;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import pt.ulisboa.tecnico.cmu.R;
import pt.ulisboa.tecnico.cmu.adapters.UserButtonListAdapter;
import pt.ulisboa.tecnico.cmu.dataobjects.User;
import pt.ulisboa.tecnico.cmu.utils.RequestsUtils;

public class AddUserActivity extends AppCompatActivity {

    private UserButtonListAdapter userButtonListAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private GoogleSignInAccount googleAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        setupActionBar();

        this.googleAccount = GoogleSignIn.getLastSignedInAccount(this);

        RecyclerView recyclerView = findViewById(R.id.user_list);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(AddUserActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        List<User> userList = new ArrayList<>();
        userButtonListAdapter = new UserButtonListAdapter(userList, this);
        recyclerView.setAdapter(userButtonListAdapter);

        EditText nameOfUserView = findViewById(R.id.name_of_user);
        nameOfUserView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                search(textView.getText().toString());
                return true;
            }
            return false;
        });

        Button searchUserButton = findViewById(R.id.search_user_button);
        searchUserButton.setOnClickListener(view -> search(nameOfUserView.getText().toString()));
    }

    private void search(String q) {
        Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            User[] users = RequestsUtils.getUsers(this, q);

            List<User> usersList = Arrays.asList(users).stream().filter(
                user -> !user.getId().equals(googleAccount.getId())).collect(
                Collectors.toList());

            userButtonListAdapter.setUserList(usersList);
            return null;
        });
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
}
