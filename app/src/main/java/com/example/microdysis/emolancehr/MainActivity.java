package com.example.microdysis.emolancehr;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.example.microdysis.emolancehr.data.TaskContract;
import com.example.microdysis.emolancehr.service.HardwareConnectorService;
import com.example.microdysis.emolancehr.service.HardwareConnectorServiceConnection;
import com.wahoofitness.common.log.Logger;
import com.wahoofitness.connector.HardwareConnectorEnums;
import com.wahoofitness.connector.HardwareConnectorTypes;
import com.wahoofitness.connector.capabilities.Capability;
import com.wahoofitness.connector.capabilities.Heartrate;
import com.wahoofitness.connector.conn.connections.SensorConnection;
import com.wahoofitness.connector.conn.connections.params.ConnectionParams;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity implements HardwareConnectorService.Listener, LoaderManager.LoaderCallbacks<Cursor> {

    // Constants for logging and referring to a unique loader
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int TASK_LOADER_ID = 0;

    private CustomCursorAdapter mAdapter;
    RecyclerView mRecyclerView;

    // Replace APP_KEY from your APP_KEY
    final static private String APP_KEY = "1edt7qh3au3olef";
    // Relace APP_SECRET from your APP_SECRET
    final static private String APP_SECRET = "t6oztp71xvgl39g";

    //
    private DropboxAPI<AndroidAuthSession> mDBApi;


    CustomCursorAdapter customCursorAdapter;


    private TextView mHeartRateTextView;
    private TextView mStatusTextView;
    //private DiscoveryAdapter mDiscoveryAdapter;
    private Button mRecordButton;
    private Button mStopButton;
    private Spinner mSelectTypeSpinner;
    private ImageView mHeartImageView;
    private Heartrate.Listener mHeartrateListener = new Heartrate.Listener() {

        @Override
        public void onHeartrateData(Heartrate.Data data) {
            StringBuilder sum = new StringBuilder();
            hrData=data;
            if (hrData != null) {
                sum.append((int) hrData.getHeartrate().asEventsPerMinute()).append(" ");
                //sum.append((int) hrData.getAccumulationPeriod().asEventsPerMinute()).append(" ");
            }
            mHeartRateTextView.setText(sum.toString().trim());

            //if(start_record)heartRateData.append(String.format("%.2f",hrData.getAccumulationPeriod().asSeconds())).append(" ");
            if(start_record)heartRateData.append(String.format("%.4f",1/hrData.getHeartrate().asEventsPerSecond())).append("s, ");

        }

        @Override
        public void onHeartrateDataReset() {

        }
    };
    Heartrate mhr;

    boolean start_record = false;
    private static final Logger L = new Logger("MainActivity");

    private ConnectionParams mDiscoveryConnectionParams;

    private final HardwareConnectorServiceConnection mServiceConnection = new HardwareConnectorServiceConnection() {

        @Override
        protected void onHardwareConnectorServiceConnected(HardwareConnectorService service) {
            service.addListener(MainActivity.this);

        }
    };


    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    HeartRateBroadcastReceiver mHRReceiver;
    public static final int REQUEST_ENABLE_BT = 2;

    StringBuilder  heartRateData;
    private Heartrate.Data hrData;
    Uri taskuri;
    LoaderManager lm = getSupportLoaderManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHRReceiver = new HeartRateBroadcastReceiver();

        // callback method
        initialize_session();


        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewTasks);

        // Set the layout for the RecyclerView to be a linear layout, which measures and
        // positions items within a RecyclerView into a linear list
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new CustomCursorAdapter(this, mDBApi,lm, MainActivity.this);
        mRecyclerView.setAdapter(mAdapter);

        mHeartRateTextView = (TextView) findViewById(R.id.heart_rate);
        mStatusTextView = (TextView) findViewById(R.id.status);
        mRecordButton = (Button) findViewById(R.id.record);
        mStopButton = (Button) findViewById(R.id.stop);
        mSelectTypeSpinner = (Spinner) findViewById(R.id.select_type);


        mHeartImageView = (ImageView) findViewById(R.id.heart_image);

        mSelectTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        //Bind to the HardwareConnectorService
        mServiceConnection.bind(this);

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Device does not support bluetooth!", Toast.LENGTH_SHORT).show();
        }else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else
        {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE),REQUEST_ENABLE_BT);
            //enableDiscovery(true);
            //mStatusTextView.setText("Discovering");
        }





        mRecordButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if(start_record==true) return;
                heartRateData=new StringBuilder();
                start_record=true;
                SimpleDateFormat df = new SimpleDateFormat("d/MM/yyyy HH:mm");
                String date = df.format(Calendar.getInstance().getTime());
                ContentValues contentValues = new ContentValues();
                // Put the task description and selected mPriority into the ContentValues
                contentValues.put(TaskContract.TaskEntry.COLUMN_DATE, date);
                contentValues.put(TaskContract.TaskEntry.COLUMN_DATA_STATUS, "Recording");
                contentValues.put(TaskContract.TaskEntry.COLUMN_ACTION, mSelectTypeSpinner.getSelectedItem().toString());
                contentValues.put(TaskContract.TaskEntry.COLUMN_DATA,"");
                // Insert the content values via a ContentResolver
                taskuri = getContentResolver().insert(TaskContract.TaskEntry.CONTENT_URI, contentValues);
                if(taskuri != null) {
                    Toast.makeText(getBaseContext(), taskuri.toString(), Toast.LENGTH_LONG).show();
                }

                getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, MainActivity.this);
               // mDateTextView.setText(date);
                //mTypeTextView.setText(mSelectTypeSpinner.getSelectedItem().toString());

            }
        });
        mStopButton.setOnClickListener(new View.OnClickListener(){
            public void  onClick(View v){


                if(start_record==false) return;
                start_record=false;
                ContentValues contentValues = new ContentValues();
                contentValues.put(TaskContract.TaskEntry.COLUMN_DATA_STATUS, "saved");
                //heartRateData.append("test, 0.77/sec, 0.8/sec");
                contentValues.put(TaskContract.TaskEntry.COLUMN_DATA, heartRateData.toString());
                getContentResolver().update(taskuri, contentValues, null, null);

                getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, MainActivity.this);

                //mHeartRateDataTextView.setText(heartRateData.toString());
            }
        });
       //String connectionParamsStr = getIntent().getStringExtra("mDiscoveryConnectionParams");
        //mConnectionParams = ConnectionParams.deserialize(connectionParamsStr);
        //this.setTitle(mConnectionParams.getName());

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

                int id = (int) viewHolder.itemView.getTag();

                // Build appropriate uri with String row id appended
                String stringId = Integer.toString(id);
                Uri uri = TaskContract.TaskEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(stringId).build();


                getContentResolver().delete(uri, null, null);


                getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, MainActivity.this);

            }


        }).attachToRecyclerView(mRecyclerView);







        getSupportLoaderManager().initLoader(TASK_LOADER_ID, null, this);



    }

    protected void initialize_session(){

        // store app key and secret key
        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys);

        // Pass app key pair to the new DropboxAPI object.
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);

        // MyActivity below should be your activity class name
        // start authentication.
        mDBApi.getSession().startOAuth2Authentication(MainActivity.this);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_ENABLE_BT){
            if(resultCode==RESULT_OK){
                enableDiscovery(true);
                mStatusTextView.setText("Discovering");
            }else{
                enableDiscovery(true);
                mStatusTextView.setText("Discovering");
            }
        }




    }


    @Override
    public void onDeviceDiscovered(ConnectionParams params) {
        // DISCOVERED DEVICES
        mDiscoveryConnectionParams=params;
        connectSensor(mDiscoveryConnectionParams);



    }



    @Override
    public void onDiscoveredDeviceLost(ConnectionParams params) {

    }

    @Override
    public void onDiscoveredDeviceRssiChanged(ConnectionParams params) {

    }

    @Override
    public void onNewCapabilityDetected(SensorConnection sensorConnection, Capability.CapabilityType capabilityType) {

        StringBuilder sum = new StringBuilder();
        if (sensorConnection != null && sensorConnection.isConnected()) {
            for (Capability.CapabilityType ct : Capability.CapabilityType.values()) {
                Capability cap = sensorConnection.getCurrentCapability(ct);
                if (cap != null) {
                    switch (ct) {
                        case Heartrate:
                            /*hrData = ((Heartrate) cap).getHeartrateData();
                            if (hrData != null) {
                                sum.append((int) hrData.getHeartrate().asEventsPerMinute()).append(" ");
                            }*/
                            mhr =(Heartrate) cap;
                            mhr.addListener(mHeartrateListener);
                            break;
                        default:
                            break;
                    }
                }
            }
            //mHeartRateTextView.setText(sum.toString().trim());
            //if(start_record)heartRateData.append(hrData.getHeartrate().asEventsPerSecond()).append("/sec, ");
        }


    }


    @Override
    public void onSensorConnectionStateChanged(SensorConnection sensorConnection, HardwareConnectorEnums.SensorConnectionState state) {
        if (sensorConnection != null) {

            switch (sensorConnection.getConnectionState()) {
                case CONNECTED:
                    mStatusTextView.setText("Connected");
                    enableDiscovery(false);
                    break;
                case CONNECTING:
                    mStatusTextView.setText("Connecting");

                    break;
                case DISCONNECTED:
                    mStatusTextView.setText("Disconnected");

                    break;
                case DISCONNECTING:
                    mStatusTextView.setText("Disconnecting");

                    break;
                default:
                    mStatusTextView.setText("ERROR");
                    break;
            }
        } else {
            mStatusTextView.setText("Discovering");

        }

    }

    @Override
    public void onConnectorStateChanged(HardwareConnectorTypes.NetworkType networkType, HardwareConnectorEnums.HardwareConnectorState hardwareState) {

    }


    @Override
    protected void onResume() {
        super.onResume();

        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                mDBApi.getSession().finishAuthentication();
               // customCursorAdapter.SetData(mDBApi);
                String accessToken = mDBApi.getSession().getOAuth2AccessToken();
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unregisterReceiver(mHRReceiver);

        if (mhr != null) {
            mhr.removeListener(mHeartrateListener);
        }
        mServiceConnection.unbind();
    }
    private class HeartRateBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            final SensorConnection sensorConnection = getSensorConnection(mDiscoveryConnectionParams);



        }
    }

    private SensorConnection getSensorConnection() {
        if (mDiscoveryConnectionParams == null) {
            throw new AssertionError("mConnectionParams is null");
        }

        if (mServiceConnection != null) {
            HardwareConnectorService service = mServiceConnection.getHardwareConnectorService();
            if (service != null) {
                return service.getSensorConnection(mDiscoveryConnectionParams);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    protected boolean enableDiscovery(boolean enable) {
        if (mServiceConnection != null) {
            HardwareConnectorService service = mServiceConnection.getHardwareConnectorService();
            if (service != null) {
                return service.enableDiscovery(enable);

            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    protected SensorConnection getSensorConnection(ConnectionParams params) {
        if (mServiceConnection != null) {
            HardwareConnectorService service = mServiceConnection.getHardwareConnectorService();
            if (service != null) {
                return service.getSensorConnection(params);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    protected SensorConnection connectSensor(ConnectionParams params) {
        if (mServiceConnection != null) {
            HardwareConnectorService service = mServiceConnection.getHardwareConnectorService();
            if (service != null) {
                return service.connectSensor(params);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /** Launches the {@link MainActivity} for the specifed {@link SensorConnection} */
    public static void launchActivity(Activity activity, SensorConnection sensorConnection) {
        Intent intent = new Intent(activity, MainActivity.class);

        // We serialize the SensorConnection's ConnectionParams, and pass the string into the
        // Activity
        intent.putExtra("params", sensorConnection.getConnectionParams().serialize());
        activity.startActivity(intent);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {

            // Initialize a Cursor, this will hold all the task data
            Cursor mTaskData = null;

            // onStartLoading() is called when a loader first starts loading data
            @Override
            protected void onStartLoading() {
                if (mTaskData != null) {
                    // Delivers any previously loaded data immediately
                    deliverResult(mTaskData);
                } else {
                    // Force a new load
                    forceLoad();
                }
            }

            // loadInBackground() performs asynchronous loading of data
            @Override
            public Cursor loadInBackground() {
                // Will implement to load data

                // Query and load all task data in the background; sort by priority
                // [Hint] use a try/catch block to catch any errors in loading data

                try {
                    return getContentResolver().query(TaskContract.TaskEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            TaskContract.TaskEntry._ID+" DESC");

                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            // deliverResult sends the result of the load, a Cursor, to the registered listener
            public void deliverResult(Cursor data) {
                mTaskData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }






}
