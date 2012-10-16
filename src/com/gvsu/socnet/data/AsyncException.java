package com.gvsu.socnet.data;

public class AsyncException extends Exception {

  public String msg = null;

  public AsyncException(String msg) {
    super(msg);
    this.msg = msg;
  }

  @Override
  public String toString() {
    return msg;
  }
}
