package com.gvsu.socnet.data;

public interface AsyncCallbackListener {
  public void asyncDone(AsyncDownloader.Payload response);
}
