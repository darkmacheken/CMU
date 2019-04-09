package pt.ulisboa.tecnico.cmu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmu.adapters.UserListAdapter;
import pt.ulisboa.tecnico.cmu.dataobjects.User;

public class AddAlbumActivity extends AppCompatActivity {

    private UserListAdapter userListAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private EditText nameOfAlbumView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_album);
        setupActionBar();

        RecyclerView recyclerView = findViewById(R.id.user_list);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(AddAlbumActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        List<User> userList = getUsers();
        userListAdapter = new UserListAdapter(userList);
        recyclerView.setAdapter(userListAdapter);

        nameOfAlbumView = findViewById(R.id.name_of_album);
        nameOfAlbumView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptCreate();
                    return true;
                }
                return false;
            }
        });

        Button addAlbumButton = findViewById(R.id.add_album_button);
        addAlbumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptCreate();
            }
        });
    }

    private void attemptCreate() {
        Bundle albumBundle = new Bundle();
        albumBundle.putInt("id", 0);
        albumBundle.putString("name", nameOfAlbumView.getText().toString());
        Intent data = new Intent();
        data.putExtra("album", albumBundle);
        setResult(RESULT_OK, data);
        finish();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true); // disable the button
            actionBar.setDisplayHomeAsUpEnabled(true); // remove the left caret
            actionBar.setDisplayShowHomeEnabled(true); // remove the icon
        }
    }

    private List<User> getUsers() {
        List<User> users = new ArrayList<>();
        users.add(new User(1, "(me)"));
        users.add(new User(2, "Alice"));
        users.add(new User(3, "Bob"));
        users.add(new User(4, "Charlie"));
        return users;
    }

    public void startAddUserActivity(View view) {
        userListAdapter.addUser(new User(0, "New User"));
        ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(0, 0);
    }
}