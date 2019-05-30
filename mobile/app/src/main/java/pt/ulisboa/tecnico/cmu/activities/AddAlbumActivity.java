package pt.ulisboa.tecnico.cmu.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import java.util.ArrayList;
import pt.ulisboa.tecnico.cmu.R;
import pt.ulisboa.tecnico.cmu.adapters.UserListAdapter;
import pt.ulisboa.tecnico.cmu.dataobjects.User;
import pt.ulisboa.tecnico.cmu.tasks.CreateAlbumsTask;
import pt.ulisboa.tecnico.cmu.utils.AlertUtils;

public class AddAlbumActivity extends AppCompatActivity {

    private static final int ADD_USER_REQUEST = 1;
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

        userListAdapter = new UserListAdapter(new ArrayList<>());
        recyclerView.setAdapter(userListAdapter);

        nameOfAlbumView = findViewById(R.id.name_of_album);
        nameOfAlbumView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptCreate();
                return true;
            }
            return false;
        });

        Button addAlbumButton = findViewById(R.id.add_album_button);
        addAlbumButton.setOnClickListener(view -> attemptCreate());
    }

    private void attemptCreate() {
        String name = nameOfAlbumView.getText().toString();

        if (TextUtils.isEmpty(name)) {
            AlertUtils.alert("The name of the album cannot be empty.", this);
            return;
        }

        new CreateAlbumsTask(this, name, userListAdapter.getUsers()).execute();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true); // disable the button
            actionBar.setDisplayHomeAsUpEnabled(true); // remove the left caret
            actionBar.setDisplayShowHomeEnabled(true); // remove the icon
        }
    }

    public void startAddUserActivity(View view) {
        Intent intent = new Intent(this, AddUserActivity.class);
        startActivityForResult(intent, ADD_USER_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        // Make sure the request was successful
        if (requestCode == ADD_USER_REQUEST && resultCode == RESULT_OK) {
            Bundle userBundle = data.getBundleExtra("user");
            userListAdapter.addUser(
                new User(userBundle.getString("id"), userBundle.getString("name"), userBundle.getString("email")));
            ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(0, 0);
        }
    }
}
