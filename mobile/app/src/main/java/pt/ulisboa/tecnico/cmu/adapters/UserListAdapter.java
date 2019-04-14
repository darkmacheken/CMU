package pt.ulisboa.tecnico.cmu.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;
import pt.ulisboa.tecnico.cmu.R;
import pt.ulisboa.tecnico.cmu.dataobjects.User;

public class UserListAdapter
    extends RecyclerView.Adapter<pt.ulisboa.tecnico.cmu.adapters.UserListAdapter.UserViewHolder> {

    private List<User> userList;

    public UserListAdapter(List<User> userList) {
        this.userList = userList;
    }

    @Override
    public pt.ulisboa.tecnico.cmu.adapters.UserListAdapter.UserViewHolder onCreateViewHolder(ViewGroup viewGroup,
        int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user, viewGroup, false);
        return new pt.ulisboa.tecnico.cmu.adapters.UserListAdapter.UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(pt.ulisboa.tecnico.cmu.adapters.UserListAdapter.UserViewHolder userViewHolder, int i) {
        userViewHolder.user.setText(userList.get(i).getUsername());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void addUser(User user) {
        userList.add(0, user);
        notifyItemInserted(0);
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        private TextView user;

        UserViewHolder(View view) {
            super(view);
            user = view.findViewById(R.id.user);
        }
    }
}