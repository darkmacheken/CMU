package pt.ulisboa.tecnico.cmu.adapters;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.gms.tasks.Tasks;
import java.util.List;
import java.util.concurrent.Executors;
import pt.ulisboa.tecnico.cmu.R;
import pt.ulisboa.tecnico.cmu.activities.MainActivity;
import pt.ulisboa.tecnico.cmu.dataobjects.User;
import pt.ulisboa.tecnico.cmu.utils.AlertUtils;
import pt.ulisboa.tecnico.cmu.utils.RequestsUtils;

public class UserButtonListAdapter
    extends RecyclerView.Adapter<pt.ulisboa.tecnico.cmu.adapters.UserButtonListAdapter.UserViewHolder> {

    private List<User> userList;
    private Activity activity;

    public UserButtonListAdapter(List<User> userList, Activity activity) {
        this.userList = userList;
        this.activity = activity;
    }

    @Override
    public pt.ulisboa.tecnico.cmu.adapters.UserButtonListAdapter.UserViewHolder onCreateViewHolder(
        @NonNull ViewGroup viewGroup,
        int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_button, viewGroup, false);
        return new pt.ulisboa.tecnico.cmu.adapters.UserButtonListAdapter.UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
        @NonNull pt.ulisboa.tecnico.cmu.adapters.UserButtonListAdapter.UserViewHolder userViewHolder,
        int i) {
        userViewHolder.user.setText(userList.get(i).getName() + " (" + userList.get(i).getEmail() + ")");
        userViewHolder.user.setOnClickListener(new UserButtonListAdapter.UserOnClickListener(userList.get(i)));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        private TextView user;

        UserViewHolder(View view) {
            super(view);
            user = view.findViewById(R.id.user);
        }
    }

    class UserOnClickListener implements View.OnClickListener {

        private User user;

        UserOnClickListener(User user) {
            this.user = user;
        }

        @Override
        public void onClick(View v) {
            Bundle extras = activity.getIntent().getExtras();
            if (extras != null && !TextUtils.isEmpty(extras.getString("viewAlbum", ""))) {
                Tasks.call(Executors.newSingleThreadExecutor(), () -> {
                    if (MainActivity.choseWifiDirect) {
                        RequestsUtils.addUserToAlbumWifi(activity, , user.getId());
                    } else {
                        RequestsUtils.addUserToAlbum(activity, extras.getString("viewAlbum", ""), user.getId());
                    }
                    return null;
                }).addOnFailureListener(e -> AlertUtils.alert("There was an error creating the album.", activity));
            } else {
                Bundle userBundle = new Bundle();
                userBundle.putString("id", user.getId());
                userBundle.putString("name", user.getName());
                userBundle.putString("email", user.getEmail());
                Intent data = new Intent();
                data.putExtra("user", userBundle);
                activity.setResult(RESULT_OK, data);
            }
            activity.finish();
        }
    }
}