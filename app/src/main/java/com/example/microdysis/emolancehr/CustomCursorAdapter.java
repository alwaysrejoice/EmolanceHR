package com.example.microdysis.emolancehr;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.LoaderManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.example.microdysis.emolancehr.data.TaskContract;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.ContentValues.TAG;

/**
 * Created by hui-jou on 6/24/17.
 */

public class CustomCursorAdapter extends RecyclerView.Adapter<CustomCursorAdapter.TaskViewHolder>{

    private Cursor mCursor;
    private Context mContext;
    private static final int TASK_LOADER_ID = 0;



    private DropboxAPI<AndroidAuthSession> mDBApi;
    private LoaderManager mLoaderManager;
    private MainActivity mainActivity;

    public CustomCursorAdapter(Context mContext, DropboxAPI<AndroidAuthSession> DBApi, LoaderManager loaderManager, MainActivity activity) {
        this.mContext = mContext;
        this.mDBApi=DBApi;
        this.mLoaderManager = loaderManager;
        this.mainActivity=activity;

    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.heartrate_list_item, parent, false);


        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        // Indices for the _id, description, and priority columns
        int idIndex = mCursor.getColumnIndex(TaskContract.TaskEntry._ID);
        int dateIndex = mCursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DATE);
        int actionIndex = mCursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_ACTION);
        int dataStatusIndex = mCursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DATA_STATUS);
        int dataIndex = mCursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DATA);

        mCursor.moveToPosition(position); // get to the right location in the cursor

        // Determine the values of the wanted data
        final int id = mCursor.getInt(idIndex);
        final String date = mCursor.getString(dateIndex);
        final String action = mCursor.getString(actionIndex);
        final String datastatus = mCursor.getString(dataStatusIndex);
        final String data = mCursor.getString(dataIndex);

        //Set values
        holder.itemView.setTag(id);
        holder.mDateView.setText(date);
        holder.mStatusView.setText(datastatus);
        holder.mActionView.setText(action);

        holder.mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final String dataToBeUpload = date+"/"+action+"/"+data;
                final String filename= "HR"+System.currentTimeMillis()+".txt";
                final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                        filename);
                //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),

                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(dataToBeUpload.getBytes());
                    fos.close();



                    Log.i(TAG, "HR Data saved at " + file.getAbsolutePath() + " with size: " + file.length());
                    Toast.makeText(mContext, "HR Data uploaded  with filename: " + file.getName(), Toast.LENGTH_SHORT).show();



                } catch (IOException e) {
                    e.printStackTrace();
                }
                Thread childthread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        fileupload(file,filename);
                    }
                });
                childthread.start();
                ContentValues contentValues = new ContentValues();
                contentValues.put(TaskContract.TaskEntry.COLUMN_DATA_STATUS, "uploaded");
                Uri uri= ContentUris.withAppendedId(mCursor.getNotificationUri(),id);
                mContext.getContentResolver().update(uri, contentValues, null, null);

              mLoaderManager.restartLoader(TASK_LOADER_ID, null, mainActivity);








            }
        });
    }
    void fileupload(File file, String filename){
        DropboxAPI.Entry response = null;
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            response = mDBApi.putFile("/"+filename, inputStream,
                    file.length(), null, null);
            file.delete();
            Log.e("DbExampleLog", "The uploaded file's rev is: " + response.rev);

            if(response.rev.isEmpty() == false){
//                Toast.makeText(mContext, "File Uploaded ", Toast.LENGTH_LONG).show();
                Log.e("DbExampleLog", "The uploaded file's rev is: " + response.rev);
            }else
            {
                //Toast.makeText(mContext, "File Uploaded failed", Toast.LENGTH_LONG).show();
                Log.e("DbExampleLog", "The uploaded file failed" + response.rev);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DropboxException e) {
            e.printStackTrace();
        }
    }


    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    public Cursor swapCursor(Cursor c) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == c) {
            return null; // bc nothing has changed
        }
        Cursor temp = mCursor;
        this.mCursor = c; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }



    // Inner class for creating ViewHolders
    class TaskViewHolder extends RecyclerView.ViewHolder {

        // Class variables for the task description and priority TextViews
        TextView mDateView;
        TextView mActionView;
        TextView mStatusView;
        Button mUploadButton;

        /**
         * Constructor for the TaskViewHolders.
         *
         * @param itemView The view inflated in onCreateViewHolder
         */
        public TaskViewHolder(View itemView) {
            super(itemView);

            mDateView = (TextView) itemView.findViewById(R.id.date);
            mActionView = (TextView) itemView.findViewById(R.id.type);
            mStatusView = (TextView) itemView.findViewById(R.id.data_status);
            mUploadButton = (Button) itemView.findViewById(R.id.upload);

        }
    }

}
