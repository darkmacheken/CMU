package pt.ulisboa.tecnico.cmu.adapters;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import pt.ulisboa.tecnico.cmu.R;
import pt.ulisboa.tecnico.cmu.dataobjects.User;

import static android.app.Activity.RESULT_OK;

public class UserButtonListAdapter extends RecyclerView.Adapter<pt.ulisboa.tecnico.cmu.adapters.UserButtonListAdapter.UserViewHolder> {
    private List<User> userList;
    private Activity activity;

    public UserButtonListAdapter(List<User> userList, Activity activity) {
        this.userList = userList;
        this.activity = activity;
    }

    @Override
    public pt.ulisboa.tecnico.cmu.adapters.UserButtonListAdapter.UserViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_button, viewGroup, false);
        return new pt.ulisboa.tecnico.cmu.adapters.UserButtonListAdapter.UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(pt.ulisboa.tecnico.cmu.adapters.UserButtonListAdapter.UserViewHolder userViewHolder, int i) {
        userViewHolder.user.setText(userList.get(i).getUsername());
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
            Bundle userBundle = new Bundle();
            userBundle.putInt("id", user.getId());
            userBundle.putString("username", user.getUsername());
            Intent data = new Intent();
            data.putExtra("user", userBundle);
            activity.setResult(RESULT_OK, data);
            activity.finish();
        }
    }
}