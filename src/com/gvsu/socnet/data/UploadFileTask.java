package com.gvsu.socnet.data;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.gvsu.socnet.user.AddCapsuleActivity;

/**
 * Asynchronous task to upload file to server
 */
class UploadFileTask extends AsyncTask<File, Integer, Boolean> {
	
	private String TAG = "FileUpload";
	
	//default values
	private static String IP ="localhost";
	private static String port = "8888";
	private static String UPLOAD_URL = "http://"+IP+":"+port+"/uploadFromAndroid";
	private static Context mContext = null;
    /** Send the file with this form name */
    private static final String FIELD_FILE = "upload";
    private static final String FIELD_LATITUDE = "latitude";
    private static final String FIELD_LONGITUDE = "longitude";
    
    public static void giveContext(Context context) {
    	mContext = context;
    }

    /**
     * Prepare activity before upload
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mContext == null) {
        	Log.i(TAG, "no context");
        	mContext = AddCapsuleActivity.stealContext();
        	if (mContext == null) {
        		Log.e(TAG, "NO CONTEXT! using default values for upload server");
        	} else {        		
        		Log.i(TAG, "stole context");
        		IP = PreferenceManager.getDefaultSharedPreferences(mContext).getString("upload_server_ip", IP);
        		port = PreferenceManager.getDefaultSharedPreferences(mContext).getString("upload_server_port", port);
        		UPLOAD_URL = "http://"+IP+":"+port+"/uploadFromAndroid";
        		Log.v(TAG, "new upload url:"+UPLOAD_URL);
        	}
        }
//        setProgressBarIndeterminateVisibility(true);
//        mConfirm.setEnabled(false);
//        mCancel.setEnabled(false);
//        showDialog(UPLOAD_PROGRESS_DIALOG);
    }

    /**
     * Clean app state after upload is completed
     */
    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
//        setProgressBarIndeterminateVisibility(false);
//        mConfirm.setEnabled(true);
//        mDialog.dismiss();

        if (result) {
        	Log.d(TAG, "upload success");
//        	Toast.makeText(this), "Upload Success!", Toast.LENGTH_SHORT).show();
//            showDialog(UPLOAD_SUCCESS_DIALOG);
        } else {
        	Log.d(TAG, "upload failed :(");
        	Toast.makeText(mContext, "Please check network configuration", Toast.LENGTH_SHORT).show();
        	mContext = null;
//            showDialog(UPLOAD_ERROR_DIALOG);
        }
    }

    @Override
    protected Boolean doInBackground(File... image) {
        return doFileUpload(image[0], UPLOAD_URL);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        if (values[0] == 0) {
        	Log.i(TAG, "uploaded is starting");
//            mDialog.setTitle(getString(R.string.progress_dialog_title_uploading));
        }

        Log.i(TAG, values[0]+ " % uploaded");
//        mDialog.setProgress(values[0]);
    }
    

    /**
     * Upload given file to given url, using raw socket
     * @see http://stackoverflow.com/questions/4966910/androidhow-to-upload-mp3-file-to-http-server
     *
     * @param file The file to upload
     * @param uploadUrl The uri the file is to be uploaded
     *
     * @return boolean true is the upload succeeded
     */
    private boolean doFileUpload(File file, String uploadUrl) {
    	
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        String separator = twoHyphens + boundary + lineEnd;
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        int sentBytes = 0;
        long fileSize = file.length();

        // Send request
        try {
            // Configure connection
            URL url = new URL(uploadUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            publishProgress(0);

            dos = new DataOutputStream(conn.getOutputStream());

            // Send location params
//            writeFormField(dos, separator, FIELD_LATITUDE, "" + mLocation.getLatitude());
//            writeFormField(dos, separator, FIELD_LONGITUDE, "" + mLocation.getLongitude());

            // Send multipart headers
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"" + FIELD_FILE + "\"; filename=\""
                    + file.getName() + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            // Read file and create buffer
            FileInputStream fileInputStream = new FileInputStream(file);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // Send file data
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                // Write buffer to socket
                dos.write(buffer, 0, bufferSize);

                // Update progress dialog
                sentBytes += bufferSize;
                publishProgress((int)(sentBytes * 100 / fileSize));

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            // send multipart form data necesssary after file data
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            dos.flush();
            dos.close();
            fileInputStream.close();
        } catch (IOException ioe) {
            Log.e(TAG, "Cannot upload file: " + ioe.getMessage(), ioe);
            return false;
        }

        // Read response
        try {
            int responseCode = conn.getResponseCode();
            String message = conn.getHeaderField("message");
            Log.d(TAG, "Server Response: "+message);
            return responseCode == 200;
        } catch (IOException ioex) {
            Log.e(TAG, "Upload file failed: " + ioex.getMessage(), ioex);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Upload file failed: " + e.getMessage(), e);
            return false;
        }
    }

    private void writeFormField(DataOutputStream dos, String separator, String fieldName, String fieldValue) throws IOException
    {
        dos.writeBytes(separator);
        dos.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"\r\n");
        dos.writeBytes("\r\n");
        dos.writeBytes(fieldValue);
        dos.writeBytes("\r\n");
    }
}