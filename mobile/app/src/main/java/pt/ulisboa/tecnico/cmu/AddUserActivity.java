package pt.ulisboa.tecnico.cmu;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import pt.ulisboa.tecnico.cmu.adapters.UserButtonListAdapter;
import pt.ulisboa.tecnico.cmu.dataobjects.User;

public class AddUserActivity extends AppCompatActivity {

    private UserButtonListAdapter userButtonListAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        setupActionBar();

        RecyclerView recyclerView = findViewById(R.id.user_list);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(AddUserActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        List<User> userList = new ArrayList<>();
        userButtonListAdapter = new UserButtonListAdapter(userList, this);
        recyclerView.setAdapter(userButtonListAdapter);

        EditText nameOfUserView = findViewById(R.id.name_of_user);
        nameOfUserView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    search();
                    return true;
                }
                return false;
            }
        });

        Button searchUserButton = findViewById(R.id.search_user_button);
        searchUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search();
            }
        });
    }

    private void search() {
        List<User> users = searchUser();
        userButtonListAdapter.setUserList(users);
        ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(0, 0);
    }

    private List<User> searchUser() {
        List<User> users = new ArrayList<>();
        users.add(new User(1, "Dennis"));
        users.add(new User(2, "Eleanor"));
        users.add(new User(3, "Farrow"));
        users.add(new User(4, "Gavin"));
        return users;
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
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return (super.onOptionsItemSelected(item));
    }
}
