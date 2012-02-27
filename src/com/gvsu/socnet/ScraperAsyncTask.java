//package com.gvsu.socnet;
///** AsyncTask.java */
//
//
///****************************************************************
// * com.ciscomputingclub.silencer.AsyncTask
// * @author
// * @version 1.0
// ***************************************************************/
//
///*
// * Copyright (C) 2009 
// * Jayesh Salvi <jayesh@altcanvas.com>
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// * 
// * 
// * 
// * 
// * This file has been modified by GV-Computing-Club
// */
//
//import android.os.AsyncTask;
//
//
//
//public class ScraperAsyncTask
//    extends
//    AsyncTask<ScraperAsyncTask.Payload, Object, ScraperAsyncTask.Payload> {
//	public static final String TAG = "debug";
//
//	public static final int GETBANNERDATA = 1001;
//
//	/*
//	 * Runs on GUI thread
//	 */
//	protected void onPreExecute() {
//	}
//
//	/*
//	 * Runs on GUI thread
//	 */
//	public void onPostExecute(ScraperAsyncTask.Payload payload) {
//
//		switch (payload.taskType) {
//
//		case GETBANNERDATA:
//			LoginActivity app = (LoginActivity) payload.data[0];
//
//			if (payload.result != null) {
//
//				// Present the result on success
//				String result = (String) payload.result;
//				// app.loginResult.setTextColor(Color.GREEN);
//				// app.loginResult.setText(result);
//				loginSuccess(app, result);
//
//			} else {
//				// Report the exception on failure
//				String msg = (payload.exception != null) ? payload.exception
//				    .toString() : "";
//				// app.loginResult.setText(msg);
//				loginFailed(app, msg);
//			}
//
//			break;
//		}
//	}
//
//	/*
//	 * Runs on GUI thread
//	 */
//	public void onProgressUpdate(Object... value) {
//		int type = ((Integer) value[0]).intValue();
//
//		switch (type) {
//
//		case GETBANNERDATA:
//			break;
//		}
//
//	}
//
//	/*
//	 * Runs on background thread
//	 */
//	public ScraperAsyncTask.Payload doInBackground(
//	    ScraperAsyncTask.Payload... params) {
//		ScraperAsyncTask.Payload payload = params[0];
//
//		try {
//			throw new AppException("wrong username or password");
//		} catch (AppException ape) {
//			payload.exception = ape;
//			payload.result = null;
//		}
//
//		return payload;
//	}
//
//	public static class Payload {
//		public int taskType;
//		public Object[] data;
//		public Object result;
//		public Exception exception;
//
//		public Payload(int taskType, Object[] data) {
//			this.taskType = taskType;
//			this.data = data;
//		}
//	}
//
//	private void loginFailed(LoginActivity app, CharSequence message) {
//		try {
//			app.username.setEnabled(true);
//			app.username.setFocusable(true);
//			app.username.setFocusableInTouchMode(true);
//			app.password.setEnabled(true);
//			app.password.setFocusable(true);
//			app.password.setFocusableInTouchMode(true);
//			app.loginButton.setEnabled(true);
//			app.loginButton.setFocusable(true);
//			app.semSpinner.setEnabled(true);
//			app.semSpinner.setFocusable(true);
//			app.loginResult.setText(message);
//			app.setProgressBarIndeterminateVisibility(false);
//		} catch (NullPointerException ne) {
//		}
//	}
//
//	/****************************************************************
//	 * @param app
//	 * @param message void
//	 ***************************************************************/
//	private void loginSuccess(LoginActivity app, String message) {
//		try {
//			app.setProgressBarIndeterminateVisibility(false);
//		} catch (NullPointerException ne) {
//		}
//		app.gotoClassScreen();
//	}
//
//	/****************************************************************
//	 * figure out start or end time
//	 * @param time
//	 * @return int
//	 ***************************************************************/
//	private int processTime(String time) {
//		boolean add12 = false;
//		String[] start = time.split(":");
//		String startBase = start[0].trim() + start[1].substring(0, 2);
//		if ((start[1].substring(3).equals("pm"))) {
//			if (start[0].length() == 1) {
//				add12 = true;
//			} else if (start[0].length() > 1
//			    && !start[0].substring(0, 2).equals("12")) {
//				add12 = true;
//			}
//		}
//		int startTime = Integer.parseInt(startBase);
//		if (add12) {
//			startTime += 1200;
//		}
//		return startTime;
//	}
// }
