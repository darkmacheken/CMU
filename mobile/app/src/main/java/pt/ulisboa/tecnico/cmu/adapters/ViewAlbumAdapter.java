package pt.ulisboa.tecnico.cmu.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.List;
import pt.ulisboa.tecnico.cmu.R;
import pt.ulisboa.tecnico.cmu.activities.ViewPhotoActivity;

public class ViewAlbumAdapter extends RecyclerView.Adapter<ViewAlbumAdapter.PhotoViewHolder> {

    private List<String> photoList;
    private Context context;

    public ViewAlbumAdapter(List<String> photoList, Context context) {
        this.photoList = photoList;
        this.context = context;
    }

    @Override
    public ViewAlbumAdapter.PhotoViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.photo,
            viewGroup, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewAlbumAdapter.PhotoViewHolder photoViewHolder, int i) {
        photoViewHolder.photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        Bitmap bmp = BitmapFactory.decodeFile(photoList.get(i), options);
        photoViewHolder.photo.setImageBitmap(bmp);
        photoViewHolder.photo.setOnClickListener(new PhotoOnClickListener(photoList.get(i)));
    }

    public void addPhoto(String photo) {
        photoList.add(0, photo);
        notifyItemInserted(0);
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {

        private ImageView photo;

        PhotoViewHolder(View view) {
            super(view);
            photo = view.findViewById(R.id.photo);
        }
    }

    class PhotoOnClickListener implements View.OnClickListener {

        private String photo;

        PhotoOnClickListener(String photo) {
            this.photo = photo;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, ViewPhotoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("photo", this.photo);
            context.startActivity(intent);
        }
    }
}