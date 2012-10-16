package com.gvsu.socnet.data;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: calebgomer
 * Date: 10/14/12
 * Time: 2:16 PM
 * To change this template use File | Settings | File Templates.
 */
public interface AsyncCallbackListener {
  public void asyncDone(AsyncDownloader.Payload response);
}
