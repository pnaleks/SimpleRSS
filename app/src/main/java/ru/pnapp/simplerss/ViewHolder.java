/*
 *  Copyright 2016 P.N.Alekseev <pnaleks@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.pnapp.simplerss;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

class ViewHolder  extends RecyclerView.ViewHolder implements Callback {
    private static Drawable imageDefault;
    private static Drawable imageBroken;
    private static int imageWidth;
    private static int imageHeight;

    private TextView textView;
    private ImageView imageView;
    private ContentLoadingProgressBar progressBar;

    static void init(Context context) {
        if (imageDefault == null) {
            imageDefault = ContextCompat.getDrawable(context, R.drawable.ic_photo_camera_white_48dp);
            imageBroken = ContextCompat.getDrawable(context, R.drawable.ic_broken_image_white_48dp);
            imageWidth = (int)(0.4F * context.getResources().getDisplayMetrics().widthPixels);
            imageHeight = imageWidth / 2;
        }
    }

    ViewHolder(View itemView) {
        super(itemView);
        textView = (TextView) itemView.findViewById(R.id.text);
        imageView = (ImageView) itemView.findViewById(R.id.image);
        progressBar = (ContentLoadingProgressBar) itemView.findViewById(R.id.progress);
        progressBar.show();
    }

    void setText(CharSequence text) { if( textView != null ) textView.setText(text); }

    void setImage(String urlString) {
        if (imageView != null) {
            if (urlString == null) {
                imageView.setImageDrawable(imageDefault);
            } else {
                progressBar.show();
                Picasso.with(imageView.getContext()).load(urlString)
                        .resize(imageWidth, imageHeight)
                        .centerInside()
                        .placeholder(imageDefault)
                        .error(imageBroken)
                        .into(imageView, this);
                return;
            }
        }
        if (progressBar != null) progressBar.hide();
    }

    @Override
    public void onSuccess() { if (progressBar != null) progressBar.hide(); }
    @Override
    public void onError() { if (progressBar != null) progressBar.hide(); }
}
