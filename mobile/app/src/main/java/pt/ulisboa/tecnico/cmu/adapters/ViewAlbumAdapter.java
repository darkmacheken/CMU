package pt.ulisboa.tecnico.cmu.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.List;
import pt.ulisboa.tecnico.cmu.R;
import pt.ulisboa.tecnico.cmu.activities.ViewPhotoActivity;

public class ViewAlbumAdapter extends RecyclerView.Adapter<ViewAlbumAdapter.PhotoViewHolder> {

    private static final int IMAGE_HEIGHT = 150;
    private static final int IMAGE_WIDTH = 120;
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
        double scaleFactor = Math.ceil((float) IMAGE_HEIGHT / (float) bmp.getHeight());
        bmp = Bitmap.createScaledBitmap(bmp, (int) (bmp.getWidth() * scaleFactor),(int) (bmp.getHeight() * scaleFactor), false);

        if(bmp.getWidth() < IMAGE_WIDTH) {
            scaleFactor = (int) Math.ceil((float) IMAGE_WIDTH / (float) bmp.getWidth());
            bmp = Bitmap.createScaledBitmap(bmp, (int) (bmp.getWidth() * scaleFactor),(int) (bmp.getHeight() * scaleFactor), false);
        }

        int width = bmp.getWidth();
        int height = bmp.getHeight();
        float scaleWidth = ((float) IMAGE_WIDTH) / width;
        float scaleHeight = ((float) IMAGE_HEIGHT) / height;

        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(Math.min(scaleWidth, scaleHeight), Math.min(scaleWidth, scaleHeight));

        // "RECREATE" THE NEW BITMAP
        Bitmap outputImage = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas can = new Canvas(outputImage);
        can.drawBitmap(bmp, (IMAGE_WIDTH - bmp.getWidth()) / 2, (IMAGE_HEIGHT - bmp.getHeight()) / 2, null);

        photoViewHolder.photo.setImageBitmap(outputImage);
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