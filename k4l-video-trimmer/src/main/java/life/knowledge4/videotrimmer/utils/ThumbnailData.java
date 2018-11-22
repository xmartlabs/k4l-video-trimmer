package life.knowledge4.videotrimmer.utils;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ThumbnailData {
  private Bitmap thumbnail;
  private Bitmap thumbnailFullSize;
  private Integer time;

  public Bitmap getThumbnail() {
    return thumbnail;
  }

  public void setThumbnail(@Nullable Bitmap thumbnail) {
    this.thumbnail = thumbnail;
  }

  public Bitmap getThumbnailFullSize() {
    return thumbnailFullSize;
  }

  public void setThumbnailFullSize(@NonNull Bitmap thumbnailFullSize) {
    this.thumbnailFullSize = thumbnailFullSize;
  }

  public Integer getTime() {
    return time;
  }

  public void setTime(@NonNull Integer time) {
    this.time = time;
  }

  public ThumbnailData(@Nullable Bitmap thumbnail, @NonNull Bitmap thumbnailFullSize, @NonNull Integer time) {
    this.thumbnail = thumbnail;
    this.thumbnailFullSize = thumbnailFullSize;
    this.time = time;
  }
}
