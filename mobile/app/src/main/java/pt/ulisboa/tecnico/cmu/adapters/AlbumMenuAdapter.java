package pt.ulisboa.tecnico.cmu.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.google.gson.Gson;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import pt.ulisboa.tecnico.cmu.R;
import pt.ulisboa.tecnico.cmu.activities.ViewAlbumActivity;
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
        albumViewHolder.album.setText(albumList.get(i).getName());
        albumViewHolder.album.setOnClickListener(new AlbumOnClickListener(albumList.get(i)));
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public void addAlbum(Album album) {
        albumList.add(0, album);
        notifyItemInserted(0);
    }

    @TargetApi(VERSION_CODES.N)
    public void addAlbums(List<Album> albums) {
        Set<String> albumsSet = this.albumList.stream()
            .map(Album::getId)
            .collect(Collectors.toSet());

        List<Album> filteredAlbums = albums.stream()
            .filter(album -> !albumsSet.contains(album.getId()))
            .collect(Collectors.toList());

        albumList.addAll(filteredAlbums);
        notifyDataSetChanged();
    }

    public List<Album> getAlbumList() {
        return albumList;
    }

    class AlbumViewHolder extends RecyclerView.ViewHolder {

        private Button album;

        AlbumViewHolder(View view) {
            super(view);
            album = view.findViewById(R.id.album);
        }
    }

    class AlbumOnClickListener implements View.OnClickListener {

        private Album album;

        AlbumOnClickListener(Album album) {
            this.album = album;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, ViewAlbumActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            Gson gson = new Gson();
            String albumDataObjectAsAString = gson.toJson(this.album);
            intent.putExtra("album", albumDataObjectAsAString);

            context.startActivity(intent);
        }
    }
}