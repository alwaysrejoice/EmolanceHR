package com.example.microdysis.emolancehr.capabilityFragments;

/**
 * Created by hui-jou on 6/22/17.
 */

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.microdysis.emolancehr.service.HardwareConnectorService;
import com.wahoofitness.common.datatypes.TimeInstant;
import com.wahoofitness.connector.capabilities.Capability;
import com.wahoofitness.connector.capabilities.Heartrate;


public class CapHeartrateFragment extends CapabilityFragment {

    private final Heartrate.Listener mHeartrateListener = new Heartrate.Listener() {

        @Override
        public void onHeartrateData(Heartrate.Data data) {
            mLastCallbackData = data;
            refreshView();

        }

        @Override
        public void onHeartrateDataReset() {
            registerCallbackResult("onHeartrateDataReset", TimeInstant.now());
            refreshView();
        }
    };
    private Heartrate.Data mLastCallbackData;
    private TextView mTextView;

    @Override
    public void initView(final Context context, LinearLayout ll) {
        mTextView = createSimpleTextView(context);
        refreshView();
        ll.addView(mTextView);

        ll.addView(createSimpleButton(context, "resetHeartrateData", new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getHeartrateCap().resetHeartrateData();
            }
        }));

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Heartrate cap = getHeartrateCap();
        if (cap != null) {
            cap.removeListener(mHeartrateListener);
        }
    }

    @Override
    public void onHardwareConnectorServiceConnected(HardwareConnectorService service) {

        getHeartrateCap().addListener(mHeartrateListener);
        refreshView();
    }

    private Heartrate getHeartrateCap() {
        return (Heartrate) getCapability(Capability.CapabilityType.Heartrate);
    }

    @Override
    protected void refreshView() {
        Heartrate cap = getHeartrateCap();
        if (cap != null) {
            Heartrate.Data data = cap.getHeartrateData();

            mTextView.setText("");
            mTextView.append("GETTER DATA\n");
            mTextView.append(summarizeGetters(data));
            mTextView.append("\n\n");
            mTextView.append("CALLBACK DATA\n");
            mTextView.append(summarizeGetters(mLastCallbackData));
            mTextView.append("\n\n");
            mTextView.append("CALLBACKS\n");
            mTextView.append(getCallbackSummary());

        } else {
            mTextView.setText("Please wait... no cap...");
        }

    }
}

