package ru.pnapp.simplerss;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import java.net.URL;

import dagger.Module;
import dagger.Provides;

@Module
public class SimpleImageLoader extends ImageLoaderModel {

    @Provides
    ImageLoaderModel provideImageLoaderModel() {
        return new SimpleImageLoader();
    }

    @Override
    void Load(final String urlString, final Callback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = null;
                try {
                    URL url = new URL(urlString);
                    final BitmapFactory.Options options = new BitmapFactory.Options();

                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(url.openStream(), null, options);

                    options.inJustDecodeBounds = false;
                    options.inSampleSize = Math.min( options.outWidth/width , options.outHeight/height  );
                    bitmap = BitmapFactory.decodeStream(url.openStream(), null, options);

                    float w = bitmap.getWidth();
                    float h = bitmap.getHeight();

                    if (w > width) {
                        h = width * h / w;
                        bitmap = Bitmap.createScaledBitmap(bitmap, width, (int) h, true);
                    }

                    if (h > height) {
                        float nh = height;
                        float ny = (h - nh) / 2F;
                        bitmap = Bitmap.createBitmap(bitmap, 0, (int) ny, (int) w, (int) nh);
                    }

                } catch (Exception ignore) {}

                final Bitmap tmpBitmap = bitmap;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onCompleted(tmpBitmap);
                    }
                });
            }
        };
        new Thread( runnable ).start();
    }

}
