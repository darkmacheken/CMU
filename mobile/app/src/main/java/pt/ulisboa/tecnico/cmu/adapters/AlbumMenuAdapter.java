package pt.ulisboa.tecnico.cmu.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

import pt.ulisboa.tecnico.cmu.R;
import pt.ulisboa.tecnico.cmu.dataobjects.Album;

public class AlbumMenuAdapter extends RecyclerView.Adapter<AlbumMenuAdapter.AlbumViewHolder> {
    private List<Album> albumList;
    private Context context;

    public AlbumMenuAdapter(List<Album> albumList, Context context) {
        this.albumList = albumList;
        this.context = context;
    }

    @Override
    public AlbumMenuAdapter.AlbumViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.album, viewGroup, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AlbumMenuAdapter.AlbumViewHolder albumViewHolder, int i) {
        //albumViewHolder.name.setText(albumList.get(i).getName());
        albumViewHolder.album.setText(albumList.get(i).getName());
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public class AlbumViewHolder extends RecyclerView.ViewHolder {
        //private TextView name;
        private Button album;

        public AlbumViewHolder(View view) {
            super(view);
            //name = view.findViewById(R.id.name);
            album = view.findViewById(R.id.album);
        }
    }
}