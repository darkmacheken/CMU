package pt.ulisboa.tecnico.cmu.adapters;

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

    public AlbumMenuAdapter(List<Album> albumList) {
        this.albumList = albumList;
    }

    @Override
    public AlbumMenuAdapter.AlbumViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.album, viewGroup, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AlbumMenuAdapter.AlbumViewHolder albumViewHolder, int i) {
        albumViewHolder.album.setText(albumList.get(i).getName());
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public void addAlbum(Album album) {
        albumList.add(0, album);
        notifyItemInserted(0);
    }

    class AlbumViewHolder extends RecyclerView.ViewHolder {
        private Button album;

        AlbumViewHolder(View view) {
            super(view);
            album = view.findViewById(R.id.album);
        }
    }
}