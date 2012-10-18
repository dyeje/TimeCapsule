package com.gvsu.socnet.data;

import android.content.Context;

public interface AsyncCallbackListener {
  public void asyncDone(AsyncDownloader.Payload response);
  public Context getApplicationContext();
}
