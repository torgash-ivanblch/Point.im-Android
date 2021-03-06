package org.itishka.pointim.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.itishka.pointim.R;
import org.itishka.pointim.api.ConnectionManager;
import org.itishka.pointim.api.data.ImgurImage;
import org.itishka.pointim.api.data.ImgurUploadResult;
import org.itishka.pointim.utils.ImgurUploadTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Tishka17 on 31.12.2014.
 */
public class ImageUploadingPanel extends FrameLayout {

    private ViewGroup mLayout;
    private ArrayList<Image> mImages = new ArrayList<>(1);
    private Callback<Void> deleteCallback = new Callback<Void>() {
        @Override
        public void success(Void aVoid, Response response) {
            //do nothng
        }

        @Override
        public void failure(RetrofitError error) {
            //do nothng
        }
    };

    public ImageUploadingPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageUploadingPanel(Context context) {
        super(context);
        init();
    }

    public ImageUploadingPanel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.image_uploading_panel, this);
        mLayout = (ViewGroup) findViewById(R.id.contentView);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancel();
    }

    public void addImage(Uri uri) {
        final Image img = new Image();
        final View newView = inflate(getContext(), R.layout.image_uploading_panel_item, null);
        img.imageView = (ImageView) newView.findViewById(R.id.imageView);
        img.viewFinished = (ImageView) newView.findViewById(R.id.viewFinished);
        img.viewFinished.setVisibility(GONE);
        img.viewError = (ImageView) newView.findViewById(R.id.viewError);
        img.viewError.setVisibility(GONE);
        img.progress = (ProgressWheel) newView.findViewById(R.id.progress_wheel);
        img.cancel = (ImageButton) newView.findViewById(R.id.action_cancel);
        img.cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (img.task != null && !img.task.isCancelled()) {
                    img.task.cancel(true);
                }
                ImageUploadingPanel.this.mLayout.removeView(newView);
                mImages.remove(img);
            }
        });
        img.imageView.setColorFilter(Color.argb(220, 255, 255, 255), PorterDuff.Mode.LIGHTEN);
        Picasso.with(getContext())
                .load(uri)
                .transform(new CropSquareTransformation())
                .fit()
                .centerCrop()
                .into(img.imageView);
        mLayout.addView(newView);
        img.originalPath = uri;
        mImages.add(img);

        img.task = new ImgUploadTask(img, getContext());
        img.task.execute();
        newView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!img.uploaded && img.task != null && img.task.getStatus() == AsyncTask.Status.FINISHED) {
                    img.task = new ImgUploadTask(img, getContext());
                    img.task.execute();
                } else if (img.uploaded && !TextUtils.isEmpty(img.uploadInfo.deletehash)) {
                    ConnectionManager.getInstance().imgurService.deleteImage(img.uploadInfo.deletehash, deleteCallback);
                }
            }
        });
    }

    public List<String> getLinks() {
        List<String> links = new ArrayList<>(mImages.size());
        for (Image i : mImages) {
            links.add(i.uploadInfo.link);
        }
        return links;
    }

    public void cancel() {
        for (Image i : mImages) {
            if (i.task != null && !i.task.isCancelled()) {
                i.task.cancel(true);
            } else if (i.uploaded && !TextUtils.isEmpty(i.uploadInfo.deletehash)) {
                //FIXME delete uploaded images on view cancel
                //ConnectionManager.getInstance().imgurService.deleteImage(i.uploadInfo.deletehash, deleteCallback);
            }
        }
    }

    public boolean isUploadFinished() {
        for (Image i : mImages) {
            if (!i.uploaded) return false;
        }
        return true;
    }

    public void reset() {
        cancel();
        mImages.clear();
        mLayout.removeAllViews();
    }

    private static final class ImgUploadTask extends ImgurUploadTask {
        WeakReference<Image> img;

        ImgUploadTask(Image img, Context context) {
            super(context, img.originalPath);
            this.img = new WeakReference<>(img);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            img.get().progress.setProgress(0);
            img.get().progress.setVisibility(VISIBLE);
            img.get().viewError.setVisibility(GONE);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            img.get().progress.setProgress(values[0] * 0.9f);
            //progress 0..100 -> fkmaf 255..55
            img.get().imageView.setColorFilter(Color.argb(220 - values[0] * 2, 255, 255, 255), PorterDuff.Mode.LIGHTEN);
        }

        @Override
        protected void onPostExecute(ImgurUploadResult result) {
            super.onPostExecute(result);
            if (result != null && result.success) {
                img.get().viewFinished.setVisibility(VISIBLE);
                img.get().progress.setVisibility(GONE);
                img.get().uploaded = true;
                img.get().imageView.setColorFilter(null);
                img.get().uploadInfo = result.data;
            } else {
                img.get().viewError.setVisibility(VISIBLE);
                img.get().progress.setVisibility(GONE);
                img.get().imageView.setColorFilter(Color.argb(220, 200, 100, 100), PorterDuff.Mode.LIGHTEN);
                //Toast.makeText(getActivity(), "Error uploading photo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class Image {
        Uri originalPath;
        ImageView imageView;
        ImageButton cancel;
        ImageView viewFinished;
        ImageView viewError;
        ProgressWheel progress;

        boolean uploaded = false;
        ImgurUploadTask task = null;
        ImgurImage uploadInfo;
    }

    private class CropSquareTransformation implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;
            Bitmap result = Bitmap.createBitmap(source, x, y, size, size);
            if (result != source) {
                source.recycle();
            }
            return result;
        }

        @Override
        public String key() {
            return "square()";
        }
    }
}
