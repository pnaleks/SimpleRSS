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

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

abstract public class ImageLoaderModel {
    protected static int height = 400;
    protected static int width = 200;

    /**
     * Sets boundary size of the image
     * @param width maximum allowed image width
     * @param height maximum allowed image height
     */
    public void setImageSize(int width, int height) {
        ImageLoaderModel.height = height;
        ImageLoaderModel.width = width;
    }

    /**
     * Load an image from the url and invoke the callback upon completion
     * @param urlString image url
     * @param callback callback to invoke
     */
    abstract void Load(String urlString, Callback callback);

    public interface Callback {
        /**
         * Called when image ready or error occurs
         * @param bitmap resulting bitmap or null on error
         */
        void onCompleted(@Nullable Bitmap bitmap);
    }
}
