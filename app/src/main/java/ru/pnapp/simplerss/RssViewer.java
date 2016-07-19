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

public interface RssViewer {
    /**
     * This function should be called from {@link RssPresenter} as a response on {@link RssPresenter#getFeed(String)}
     * when data loading has been completed
     */
    void onDataReady();

    /**
     * This function should be called from {@link RssPresenter} as a response on {@link RssPresenter#loadImage(Object, int)} when image loading has been completed
     *
     * @param target object passed with {@link RssPresenter#loadImage(Object, int)}
     * @param position integer passed with {@link RssPresenter#loadImage(Object, int)}
     * @param bitmap image data obtained
     */
    void displayImage(Object target, int position, Bitmap bitmap);

    /**
     * Called if error occurs
     * @param message the error message
     */
    void onError(String message);
}
