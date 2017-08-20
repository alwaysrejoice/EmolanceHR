package com.example.microdysis.emolancehr;

/**
 * Created by hui-jou on 6/22/17.
 */

import android.app.Fragment;
import android.os.Bundle;

import com.example.microdysis.emolancehr.service.HardwareConnectorService;
import com.example.microdysis.emolancehr.service.HardwareConnectorServiceConnection;
import com.wahoofitness.connector.HardwareConnectorEnums;
import com.wahoofitness.connector.HardwareConnectorTypes;
import com.wahoofitness.connector.capabilities.Capability;
import com.wahoofitness.connector.conn.connections.SensorConnection;
import com.wahoofitness.connector.conn.connections.params.ConnectionParams;

import java.util.ArrayList;
import java.util.Collection;


/** Base class Fragment with automatic binding to the {@link HardwareConnectorService} */
public abstract class HardwareConnectorFragment extends Fragment implements
        HardwareConnectorService.Listener {

    private final HardwareConnectorServiceConnection mServiceConnection = new HardwareConnectorServiceConnection() {

        @Override
        protected void onHardwareConnectorServiceConnected(HardwareConnectorService service) {

            // Register the listener
            service.addListener(HardwareConnectorFragment.this);

            // Notify the contrece class
            HardwareConnectorFragment.this.onHardwareConnectorServiceConnected(service);
        }
    };

    @Override
    public void onConnectorStateChanged(HardwareConnectorTypes.NetworkType networkType,
                                        HardwareConnectorEnums.HardwareConnectorState hardwareState) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mServiceConnection.bind(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Deregister the listener
        HardwareConnectorService service = mServiceConnection.getHardwareConnectorService();
        if (service != null) {
            service.removeListener(this);
        }

        // Unbind
        mServiceConnection.unbind();
    }

    @Override
    public void onNewCapabilityDetected(SensorConnection sensorConnection,
                                        Capability.CapabilityType capabilityType) {

    }

    @Override
    public void onSensorConnectionStateChanged(SensorConnection sensorConnection,
                                               HardwareConnectorEnums.SensorConnectionState state) {

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

    protected void disconnectSensor(ConnectionParams params) {
        if (mServiceConnection != null) {
            HardwareConnectorService service = mServiceConnection.getHardwareConnectorService();
            if (service != null) {
                service.disconnectSensor(params);
            }
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

    protected Collection<ConnectionParams> getDiscoveredConnectionParams() {
        if (mServiceConnection != null) {
            HardwareConnectorService service = mServiceConnection.getHardwareConnectorService();
            if (service != null) {
                return service.getDiscoveredConnectionParams();
            } else {
                return new ArrayList<ConnectionParams>();
            }
        } else {
            return new ArrayList<ConnectionParams>();
        }
    }

    protected HardwareConnectorEnums.HardwareConnectorState getHardwareConnectorState(HardwareConnectorTypes.NetworkType networkType) {
        if (mServiceConnection != null) {
            HardwareConnectorService service = mServiceConnection.getHardwareConnectorService();
            if (service != null) {
                return service.getHardwareConnectorState(networkType);
            } else {
                return HardwareConnectorEnums.HardwareConnectorState.HARDWARE_NOT_SUPPORTED;
            }
        } else {
            return HardwareConnectorEnums.HardwareConnectorState.HARDWARE_NOT_SUPPORTED;
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

    protected Collection<SensorConnection> getSensorConnections() {
        if (mServiceConnection != null) {
            HardwareConnectorService service = mServiceConnection.getHardwareConnectorService();
            if (service != null) {
                return service.getSensorConnections();
            } else {
                return new ArrayList<SensorConnection>();
            }
        } else {
            return new ArrayList<SensorConnection>();
        }
    }

    protected boolean isDiscovering() {
        if (mServiceConnection != null) {
            HardwareConnectorService service = mServiceConnection.getHardwareConnectorService();
            if (service != null) {
                return service.isDiscovering();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    protected abstract void onHardwareConnectorServiceConnected(HardwareConnectorService service);
}
