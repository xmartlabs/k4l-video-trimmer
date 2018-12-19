/*
 * MIT License
 *
 * Copyright (c) 2016 Knowledge, education for life.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package life.knowledge4.videotrimmer.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.Pair;
import android.view.View;

import java.util.stream.Stream;

import life.knowledge4.videotrimmer.R;
import life.knowledge4.videotrimmer.utils.BackgroundExecutor;
import life.knowledge4.videotrimmer.utils.ThumbnailData;
import life.knowledge4.videotrimmer.utils.UiThreadExecutor;

public class TimeLineView extends View {

  private Uri mVideoUri;
  private int mHeightView;
  private int mWidthView;
  private LongSparseArray<ThumbnailData> mBitmapList = null;

  public TimeLineView(@NonNull Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public TimeLineView(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    mHeightView = getContext().getResources().getDimensionPixelOffset(R.dimen.frames_video_height);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    final int minW = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
    int w = resolveSizeAndState(minW, widthMeasureSpec, 1);

    final int minH = getPaddingBottom() + getPaddingTop() + mHeightView;
    int h = resolveSizeAndState(minH, heightMeasureSpec, 1);

    setMeasuredDimension(w, h);
  }

  @Override
  protected void onSizeChanged(final int w, int h, final int oldW, int oldH) {
    super.onSizeChanged(w, h, oldW, oldH);

    if (w != oldW) {
      mWidthView = w;
      try {
        getBitmap(w);
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      }
    }
  }

  private void getBitmap(final int viewWidth) {
    BackgroundExecutor.execute(new BackgroundExecutor.Task("", 0L, "") {
                                 @Override
                                 public void execute() {
                                   try {
                                     LongSparseArray<ThumbnailData> thumbnailList = new LongSparseArray<>();

                                     MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                                     mediaMetadataRetriever.setDataSource(getContext(), mVideoUri);

                                     // Retrieve media data
                                     String lengthMetadata = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                     if (lengthMetadata != null) {
                                       Integer videoLengthInMillis = Integer.parseInt(lengthMetadata);
                                       long videoLengthInMs = videoLengthInMillis * 1000;

                                       // Set thumbnail properties (Thumbs are squares)
                                       final int thumbWidth = mHeightView;
                                       final int thumbHeight = mHeightView;

                                       int numThumbs = (int) Math.ceil(((float) viewWidth) / thumbWidth);

                                       final long interval = videoLengthInMs / numThumbs;

                                       for (int i = 0; i < numThumbs; ++i) {
                                         Bitmap bitmapFullSize = mediaMetadataRetriever.getFrameAtTime(i * interval, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                                         // TODO: bitmap might be null here, hence throwing NullPointerException. You were right
                                         Bitmap bitmap = null;
                                         try {
                                           bitmap = Bitmap.createScaledBitmap(bitmapFullSize, thumbWidth, thumbHeight, false);
                                         } catch (Exception e) {
                                           e.printStackTrace();
                                         }
                                         ThumbnailData thumbnailData = new ThumbnailData(bitmap, bitmapFullSize, i * videoLengthInMillis / numThumbs);

                                         thumbnailList.put(i, thumbnailData);
                                       }

                                       mediaMetadataRetriever.release();
                                       returnBitmaps(thumbnailList);
                                     }
                                   } catch (final Throwable e) {
                                     Log.e("Timeline View Crash", e.getMessage(), e);
                                   }
                                 }
                               }
    );
  }

  private void returnBitmaps(final LongSparseArray<ThumbnailData> thumbnailList) {
    UiThreadExecutor.runTask("", new Runnable() {
          @Override
          public void run() {
            mBitmapList = thumbnailList;
            invalidate();
          }
        }
        , 0L);
  }

  @Override
  protected void onDraw(@NonNull Canvas canvas) {
    super.onDraw(canvas);

    if (mBitmapList != null) {
      canvas.save();
      int x = 0;

      for (int i = 0; i < mBitmapList.size(); i++) {
        Bitmap bitmap = mBitmapList.get(i).getThumbnail();

        if (bitmap != null) {
          canvas.drawBitmap(bitmap, x, 0, null);
          x = x + bitmap.getWidth();
        }
      }
    }
  }

  // Given a time in millis thus function returns the bitmap associated with the interval that time is in
  @Nullable
  public Bitmap getBitmapFromMillis(@NonNull Integer time) {
    if (mBitmapList != null && mBitmapList.size() > 0) {
      Bitmap thumbnail = mBitmapList.get(0).getThumbnailFullSize();
      for (int i = 1; i < mBitmapList.size(); i++) {
        if (time - mBitmapList.get(i).getTime() >= 0) {
          thumbnail = mBitmapList.get(i).getThumbnailFullSize();
        } else {
          break;
        }
      }
      return thumbnail;
    }
    return null;
  }

  public void setVideo(@NonNull Uri data) {
    mVideoUri = data;
  }
}
