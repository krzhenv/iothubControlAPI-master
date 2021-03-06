package com.tencent.iot.hub.device.android.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.tencent.iot.hub.device.android.core.log.TXMqttLogCallBack;
import com.tencent.iot.hub.device.android.core.log.TXMqttLogConstants;
import com.tencent.iot.hub.device.android.core.dynreg.TXMqttDynreg;
import com.tencent.iot.hub.device.android.core.dynreg.TXMqttDynregCallback;
import com.tencent.iot.hub.device.android.app.mqtt.MQTTRequest;
import com.tencent.iot.hub.device.android.app.mqtt.MQTTSample;
import com.tencent.iot.hub.device.android.core.util.AsymcSslUtils;
import com.tencent.iot.hub.device.android.core.util.TXLog;
import com.tencent.iot.hub.device.java.core.common.Status;
import com.tencent.iot.hub.device.java.core.mqtt.ConnectionState;
import com.tencent.iot.hub.device.java.core.mqtt.TXMqttActionCallBack;
import com.tencent.iot.hub.device.java.core.mqtt.TXWebSocketActionCallback;
import com.tencent.iot.hub.device.java.core.mqtt.TXWebSocketManager;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.SocketFactory;

public class IoTMqttFragment extends Fragment {

    private static final String TAG = "TXMQTT";

    private IoTMainActivity mParent;

    private MQTTSample mMQTTSample;

    private Button mConnectWebSocketBtn;

    private Button mCloseConnectWebSocketBtn;

    private Button mConnectStatusWebSocketBtn;

    private Button mConnectBtn;

    private Button mCloseConnectBtn;

    private Button mSubScribeBtn;

    private Button mUnSubscribeBtn;

    private Button mPublishBtn;
    private Button mGearPBtn;
    private Button mGearRBtn;
    private Button mGearNBtn;
    private Button mGearDBtn;
    private Button mTurnLeftBtn;
    private Button mTurnRightBtn;
    private Button mForwardBtn;
    private Button mBrakeBtn;
    private Button mBackwardBtn;
    private Button mVideoOnBtn;
    private Button mVideoOffBtn;
    private Button mHandbreakOnBtn;
    private Button mHandbreakOffBtn;

    private Button mSubScribeBroadcastBtn;

    private Button mSubScribeRRPCBtn;

    private Button mCheckFirmwareBtn;

    private Button mDynRegBtn;

    private TextView mLogInfoText;

    private Spinner mSpinner;

    private Button mSubdevOnlineBtn;
    private Button mSubdevOfflineBtn;

    private Button mSubdevBindedBtn;
    private Button mSubdevUnbindedBtn;
    private Button mSubdevRelationCheckBtn;

    private Button mDeviceLogBtn;
    private Button mUploadLogBtn;

    private Button mGetRemoteConfigBtn;
    private Button mConcernRemoteConfigBtn;
    private Button mReportSubDevVersionBtn;

    private Video mVideoOn,mVideoOff;
    private Handbrake mHandbrakeOn,mHandbrakeOff;
    private Gears mGearP,mGearR,mGearN,mGearD;
    private Control mForward,mBackward,mTurnLeft,mTurnRight,mBrake;

    // Default testing parameters
    private String mBrokerURL = "ssl://fawtsp-mqtt-public-dev.faw.cn:8883";  //传入null，即使用腾讯云物联网通信默认地址 "${ProductId}.iotcloud.tencentdevices.com:8883"  https://cloud.tencent.com/document/product/634/32546
    private String mProductID = BuildConfig.PRODUCT_ID;
    private String mDevName = BuildConfig.DEVICE_NAME;
    private String mDevPSK  = BuildConfig.DEVICE_PSK; //若使用证书验证，设为null
    private String mSubProductID = BuildConfig.SUB_PRODUCT_ID; // If you wont test gateway, let this to be null
    private String mSubDevName = BuildConfig.SUB_DEV_NAME;
    private String mSubDevPsk = BuildConfig.SUB_DEVICE_PSK;
    private String mTestTopic = BuildConfig.TEST_TOPIC;    // productID/DeviceName/TopicName
    private String mDevCertName = "YOUR_DEVICE_NAME_cert.crt";
    private String mDevKeyName  = "YOUR_DEVICE_NAME_private.key";
    private String mProductKey = BuildConfig.PRODUCT_KEY;        // Used for dynamic register
    private String mDevCert = "";           // Cert String
    private String mDevPriv = "";           // Priv String

    private volatile boolean mIsConnected;

    /**日志保存的路径*/
    private final static String mLogPath = Environment.getExternalStorageDirectory().getPath() + "/tencent/";
    private EditText mItemText;


    private final static String BROKER_URL = "broker_url";
    private final static String PRODUCT_ID = "product_id";
    private final static String DEVICE_NAME = "dev_name";
    private final static String DEVICE_PSK = "dev_psk";
    private final static String SUB_PRODUCID = "sub_prodid";
    private final static String SUB_DEVNAME = "sub_devname";
    private final static String TEST_TOPIC  = "test_topic";

    private final static String DEVICE_CERT = "dev_cert";
    private final static String DEVICE_PRIV  = "dev_priv";
    private final static String PRODUCT_KEY  = "product_key";
    private final static String SUB_DEVICE_PSK = "sub_dev_psk";


    private AtomicInteger temperature = new AtomicInteger(0);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_iot_mqtt, container, false);
        mParent = (IoTMainActivity) this.getActivity();

        initView(view);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           @Override
           public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               String[] items = getResources().getStringArray(R.array.setup_items);
               String paraStr = mItemText.getText().toString();
               if (position == 0) {
                   return;
               }
               if (paraStr.equals("")) {
                   return;
               }

               Log.d("TXMQTT", "Set " + items[position] + " to " + paraStr);
               Toast toast = Toast.makeText(mParent, "Set " + items[position] + " to " + paraStr, Toast.LENGTH_LONG);
               toast.show();
               SharedPreferences sharedPreferences =  mParent.getSharedPreferences("config",Context.MODE_PRIVATE);
               SharedPreferences.Editor editor = sharedPreferences.edit();
               switch(position) {
                   case 1:
                       mBrokerURL = paraStr;
                       editor.putString(BROKER_URL, mBrokerURL);
                       break;
                   case 2:
                       mProductID = paraStr;
                       editor.putString(PRODUCT_ID, mProductID);
                   case 3:
                       mDevName = paraStr;
                       editor.putString(DEVICE_NAME, mDevName);
                       break;
                   case 4:
                       mDevPSK = paraStr;
                       editor.putString(DEVICE_PSK, mDevPSK);
                       break;
                   case 5:
                       mSubProductID = paraStr;
                       editor.putString(SUB_PRODUCID, mSubProductID);
                       break;
                   case 6:
                       mSubDevName = paraStr;
                       editor.putString(SUB_DEVNAME, mSubDevName);
                       break;
                   case 7:
                       mTestTopic = paraStr;
                       editor.putString(TEST_TOPIC, mTestTopic);
                       break;
                   case 8:
                       mProductKey = paraStr;
                       editor.putString(PRODUCT_KEY, mProductKey);
                       break;
                   case 9:
                       mSubDevPsk = paraStr;
                       editor.putString(SUB_DEVICE_PSK, mSubDevPsk);
                       break;
                   default:
                       break;
               }
               editor.commit();
           }
           @Override
           public void onNothingSelected(AdapterView<?> parent) {
           }
        });
        mConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mIsConnected) {
                    SharedPreferences settings = mParent.getSharedPreferences("config", Context.MODE_PRIVATE);
                    mBrokerURL = settings.getString(BROKER_URL, mBrokerURL);
                    mProductID = settings.getString(PRODUCT_ID, mProductID);
                    mDevName = settings.getString(DEVICE_NAME, mDevName);
                    mDevPSK = settings.getString(DEVICE_PSK, mDevPSK);
                    mSubProductID = settings.getString(SUB_PRODUCID, mSubProductID);
                    mSubDevName = settings.getString(SUB_DEVNAME, mSubDevName);
                    mSubDevPsk = settings.getString(SUB_DEVICE_PSK, mSubDevPsk);

                    mTestTopic = settings.getString(TEST_TOPIC, mTestTopic);

                    mDevCert = settings.getString(DEVICE_CERT, mDevCert);
                    mDevPriv = settings.getString(DEVICE_PRIV, mDevPriv);

                    mMQTTSample = new MQTTSample(mParent, new SelfMqttActionCallBack(), mBrokerURL, mProductID, mDevName, mDevPSK,
                            mDevCert, mDevPriv, mSubProductID, mSubDevName, mTestTopic, null, null, true, new SelfMqttLogCallBack());
                    mMQTTSample.setSubDevPsk(mSubDevPsk);
                    mMQTTSample.connect();
                } else {
                    mParent.printLogInfo(TAG, "Mqtt has been connected, do not connect it again.", mLogInfoText, TXLog.LEVEL_INFO);
                }
            }
        });

        mCloseConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMQTTSample == null)
                    return;
                mMQTTSample.disconnect();
            }
        });

        mDynRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Test Dynamic");
                TXMqttDynreg dynreg = new TXMqttDynreg(mProductID, mProductKey, mDevName, new SelfMqttDynregCallback());
                if (dynreg.doDynamicRegister()) {
                    Log.d(TAG, "Dynamic Register OK!");
                } else {
                    Log.e(TAG, "Dynamic Register failed!");
                }
            }
        });
        mSubScribeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 在腾讯云控制台增加自定义主题（权限为订阅和发布）：custom_data，用于接收IoT服务端转发的自定义数据。
                // 本例中，发布的自定义数据，IoT服务端会在发给当前设备。
                if (mMQTTSample == null)
                    return;
                mMQTTSample.subscribeTopic();
            }
        });
        mSubdevOnlineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMQTTSample == null)
                    return;
                mMQTTSample.setSubdevOnline();
            }
        });
        mSubdevOfflineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMQTTSample == null)
                    return;
                mMQTTSample.setSubDevOffline();
            }
        });
        mSubdevBindedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMQTTSample == null)
                    return;
                mMQTTSample.setSubDevBinded();
            }
        });
        mSubdevUnbindedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMQTTSample == null)
                    return;
                mMQTTSample.setSubDevUnbinded();
            }
        });
        mSubdevRelationCheckBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMQTTSample == null)
                    return;
                mMQTTSample.checkSubdevRelation();
            }
        });
        mUnSubscribeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMQTTSample == null)
                    return;
                mMQTTSample.unSubscribeTopic();
            }
        });
        mPublishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMQTTSample == null)
                    return;
                mHandbrakeOn = new Handbrake();
                mHandbrakeOn.setTimestamp(System.currentTimeMillis());
                mHandbrakeOn.setStatus(0);
                mHandbrakeOn.setType(14);
                mHandbrakeOn.setTaskid("6D");

                // 需先在腾讯云控制台，增加自定义主题: data，用于更新自定义数据
                mMQTTSample.publishTopic("data", JSON.toJSONString(mHandbrakeOn));
                Log.d(TAG, "onClick: "+JSON.toJSONString(mHandbrakeOn));
            }
        });
        mHandbreakOnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMQTTSample == null)
                    return;
                mHandbrakeOn = new Handbrake();
                mHandbrakeOn.setTimestamp(System.currentTimeMillis());
                mHandbrakeOn.setStatus(1);
                mHandbrakeOn.setType(14);
                mHandbrakeOn.setTaskid("6D");

                // 需先在腾讯云控制台，增加自定义主题: data，用于更新自定义数据
                mMQTTSample.publishTopic("data", JSON.toJSONString(mHandbrakeOn));
                Log.d(TAG, "onClick: "+JSON.toJSONString(mHandbrakeOn));
            }
        });
        mHandbreakOffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMQTTSample == null)
                    return;
                mHandbrakeOff = new Handbrake();
                mHandbrakeOff.setTimestamp(System.currentTimeMillis());
                mHandbrakeOff.setStatus(1);
                mHandbrakeOff.setType(14);
                mHandbrakeOff.setTaskid("6D");

                // 需先在腾讯云控制台，增加自定义主题: data，用于更新自定义数据
                mMQTTSample.publishTopic("data", JSON.toJSONString(mHandbrakeOff));
                Log.d(TAG, "onClick: "+JSON.toJSONString(mHandbrakeOff));
            }
        });
        mForwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMQTTSample == null)
                    return;
                mForward = new Control();
                mForward.setTimestamp(System.currentTimeMillis());
                mForward.setAcceleration(0.1);
                mForward.setSpeed(5.0);
                mForward.setType(11);
                mForward.setWheel_angle(0.0);

                // 需先在腾讯云控制台，增加自定义主题: data，用于更新自定义数据
                mMQTTSample.publishTopic("data", JSON.toJSONString(mForward));
                Log.d(TAG, "onClick: "+JSON.toJSONString(mForward));
            }
        });
        mBackwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMQTTSample == null)
                    return;
                mBackward = new Control();
                mBackward.setTimestamp(System.currentTimeMillis());
                mBackward.setAcceleration(0.1);
                mBackward.setSpeed(-5.0);
                mBackward.setType(11);
                mBackward.setWheel_angle(0.0);

                // 需先在腾讯云控制台，增加自定义主题: data，用于更新自定义数据
                mMQTTSample.publishTopic("data", JSON.toJSONString(mBackward));
                Log.d(TAG, "onClick: "+JSON.toJSONString(mBackward));
            }
        });
        mBrakeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMQTTSample == null)
                    return;
                mBrake = new Control();
                mBrake.setTimestamp(System.currentTimeMillis());
                mBrake.setAcceleration(-0.2);
                mBrake.setSpeed(0.0);
                mBrake.setType(11);
                mBrake.setWheel_angle(0.0);
                // 需先在腾讯云控制台，增加自定义主题: data，用于更新自定义数据
                mMQTTSample.publishTopic("data", JSON.toJSONString(mBrake));
                Log.d(TAG, "onClick: "+JSON.toJSONString(mBrake));
            }
        });
        mTurnLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMQTTSample == null)
                    return;
                mTurnLeft = new Control();
                mTurnLeft.setTimestamp(System.currentTimeMillis());
                mTurnLeft.setAcceleration(0.0);
                mTurnLeft.setSpeed(0.0);
                mTurnLeft.setType(11);
                mTurnLeft.setWheel_angle(-90.0);
                // 需先在腾讯云控制台，增加自定义主题: data，用于更新自定义数据
                mMQTTSample.publishTopic("data", JSON.toJSONString(mTurnLeft));
                Log.d(TAG, "onClick: "+JSON.toJSONString(mTurnLeft));
            }
        });
        mTurnRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMQTTSample == null)
                    return;
                mTurnRight = new Control();
                mTurnRight.setTimestamp(System.currentTimeMillis());
                mTurnRight.setAcceleration(0.0);
                mTurnRight.setSpeed(0.0);
                mTurnRight.setType(11);
                mTurnRight.setWheel_angle(90.0);
                // 需先在腾讯云控制台，增加自定义主题: data，用于更新自定义数据
                mMQTTSample.publishTopic("data", JSON.toJSONString(mTurnRight));
                Log.d(TAG, "onClick: "+JSON.toJSONString(mTurnRight));
            }
        });
        mGearPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMQTTSample == null)
                    return;
                mGearP = new Gears();
                mGearP.setTimestamp(System.currentTimeMillis());
                Log.d(TAG, "onClick: "+System.currentTimeMillis());
                mGearP.setGear(1);
                mGearP.setType(13);
                mGearP.setTaskid("6D挂P档");

                // 需先在腾讯云控制台，增加自定义主题: data，用于更新自定义数据
                mMQTTSample.publishTopic("data", JSON.toJSONString(mGearP));
                Log.d(TAG, "onClick: "+JSON.toJSONString(mGearP));
            }
        });

       mGearRBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMQTTSample == null)
                    return;
                mGearR = new Gears();
                mGearR.setTimestamp(System.currentTimeMillis());
                Log.d(TAG, "onClick: "+System.currentTimeMillis());
                mGearR.setGear(2);
                mGearR.setType(13);
                mGearR.setTaskid("6D挂R档");

                // 需先在腾讯云控制台，增加自定义主题: data，用于更新自定义数据
                mMQTTSample.publishTopic("data", JSON.toJSONString(mGearR));
                Log.d(TAG, "onClick: "+JSON.toJSONString(mGearR));
            }
        });
         mGearNBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMQTTSample == null)
                    return;
                mGearN = new Gears();
                mGearN.setTimestamp(System.currentTimeMillis());
                mGearN.setGear(3);
                mGearN.setType(13);
                mGearN.setTaskid("6D挂N档");

                // 需先在腾讯云控制台，增加自定义主题: data，用于更新自定义数据
                mMQTTSample.publishTopic("data", JSON.toJSONString(mGearN));
                Log.d(TAG, "onClick: "+JSON.toJSONString(mGearN));
            }
        });
        mGearDBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMQTTSample == null)
                    return;
                mGearD = new Gears();
                mGearD.setTimestamp(System.currentTimeMillis());
                mGearD.setGear(4);
                mGearD.setType(13);
                mGearD.setTaskid("6D挂D档");

                // 需先在腾讯云控制台，增加自定义主题: data，用于更新自定义数据
                mMQTTSample.publishTopic("data", JSON.toJSONString(mGearD));
                Log.d(TAG, "onClick: "+JSON.toJSONString(mGearD));
            }
        });
        mVideoOnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMQTTSample == null)
                    return;
                mVideoOn = new Video();
                mVideoOn.setTimestamp(System.currentTimeMillis());
                mVideoOn.setVideo_type(1);
                mVideoOn.setOperation(1);
                mVideoOn.setType(12);
                // 需先在腾讯云控制台，增加自定义主题: data，用于更新自定义数据
                mMQTTSample.publishTopic("data", JSON.toJSONString(mVideoOn));
                Log.d(TAG, "onClick: "+JSON.toJSONString(mVideoOn));
            }
        });
        mVideoOffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMQTTSample == null)
                    return;
                mVideoOff = new Video();
                mVideoOff.setTimestamp(System.currentTimeMillis());
                mVideoOff.setVideo_type(1);
                mVideoOff.setOperation(0);
                mVideoOff.setType(12);
                // 需先在腾讯云控制台，增加自定义主题: data，用于更新自定义数据
                mMQTTSample.publishTopic("data", JSON.toJSONString(mVideoOff));
                Log.d(TAG, "onClick: "+JSON.toJSONString(mVideoOff));
            }
        });
        mSubScribeBroadcastBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMQTTSample == null)
                    return;
                mMQTTSample.subscribeBroadCastTopic();
            }
        });
        mCheckFirmwareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMQTTSample == null)
                    return;
                mMQTTSample.checkFirmware();
            }
        });
        mDeviceLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMQTTSample == null)
                    return;
                mMQTTSample.mLog(TXMqttLogConstants.LEVEL_ERROR,TAG,"Error level log for test!!!");
                mMQTTSample.mLog(TXMqttLogConstants.LEVEL_WARN,TAG,"Warning level log for test!!!");
                mMQTTSample.mLog(TXMqttLogConstants.LEVEL_INFO,TAG,"Info level log for test!!!");
                mMQTTSample.mLog(TXMqttLogConstants.LEVEL_DEBUG,TAG,"Debug level log for test!!!");
            }
        });
        mUploadLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMQTTSample == null)
                    return;
                mMQTTSample.uploadLog();
            }
        });

        mSubScribeRRPCBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMQTTSample == null)
                    return;
                mMQTTSample.subscribeRRPCTopic();
            }
        });

        mConnectWebSocketBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings = mParent.getSharedPreferences("config", Context.MODE_PRIVATE);
                mDevCert = settings.getString(DEVICE_CERT, mDevCert);
                mDevPriv = settings.getString(DEVICE_PRIV, mDevPriv);

                SocketFactory socketFactory = null;
                if (mDevPriv != null && mDevCert != null && mDevPriv.length() != 0 && mDevCert.length() != 0) {
                    TXLog.i(TAG, "Using cert stream " + mDevPriv + "  " + mDevCert);
                    socketFactory = AsymcSslUtils.getSocketFactoryByStream(new ByteArrayInputStream(mDevCert.getBytes()), new ByteArrayInputStream(mDevPriv.getBytes()));
                } else if (mDevPSK != null && mDevPSK.length() != 0){
                    TXLog.i(TAG, "Using PSK");
                    socketFactory = AsymcSslUtils.getSocketFactory();
                } else {
                    TXLog.i(TAG, "Using cert assets file");
                    socketFactory = AsymcSslUtils.getSocketFactoryByAssetsFile(getContext(), mDevCertName, mDevKeyName);
                }

                TXWebSocketManager.getInstance().getClient(mProductID, mDevName).setSecretKey(mDevPSK, socketFactory);
                try {
                    TXWebSocketManager.getInstance().getClient(mProductID, mDevName).setTXWebSocketActionCallback(new TXWebSocketActionCallback() {

                        @Override
                        public void onConnected() {
                        }

                        @Override
                        public void onMessageArrived(String topic, MqttMessage message) {
                        }

                        @Override
                        public void onConnectionLost(Throwable cause) {
                        }

                        @Override
                        public void onDisconnected() {
                        }
                    });
                    TXWebSocketManager.getInstance().getClient(mProductID, mDevName).connect();
                } catch (MqttException e) {
                    e.printStackTrace();
                    Log.e(TAG, "MqttException " + e.toString());
                }
            }
        });
        mCloseConnectWebSocketBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    TXWebSocketManager.getInstance().getClient(mProductID, mDevName).disconnect();
                    TXWebSocketManager.getInstance().releaseClient(mProductID, mDevName);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });
        mConnectStatusWebSocketBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectionState show = TXWebSocketManager.getInstance().getClient(mProductID, mDevName).getConnectionState();
                Log.e(TAG, "current state " + show);
            }
        });

        mGetRemoteConfigBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMQTTSample.getRemoteConfig();
            }
        });

        mConcernRemoteConfigBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMQTTSample.concernRemoteConfig();
            }
        });

        mReportSubDevVersionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMQTTSample.initOTA();  // 初始化 ota 服务，开始监听平台的推送
                mMQTTSample.reportSubDevVersion("0.0");
            }
        });
        return view;
    }

    public void initView(View view) {
        mConnectBtn = view.findViewById(R.id.connect);
        mCloseConnectBtn = view.findViewById(R.id.close_connect);
        mSubScribeBtn = view.findViewById(R.id.subscribe_topic);
        mUnSubscribeBtn = view.findViewById(R.id.unSubscribe_topic);
        mPublishBtn = view.findViewById(R.id.publish_topic);
        mGearPBtn=view.findViewById(R.id.gearP);
        mGearRBtn=view.findViewById(R.id.gearR);
        mGearNBtn=view.findViewById(R.id.gearN);
        mGearDBtn=view.findViewById(R.id.gearD);
        mTurnLeftBtn=view.findViewById(R.id.turn_left);
        mTurnRightBtn=view.findViewById(R.id.turn_right);
        mForwardBtn=view.findViewById(R.id.forward);
        mBrakeBtn=view.findViewById(R.id.brake);
        mBackwardBtn=view.findViewById(R.id.backward);
        mHandbreakOnBtn=view.findViewById(R.id.handbrakeOn);
        mHandbreakOffBtn=view.findViewById(R.id.handbrakeOff);
        mHandbreakOnBtn=view.findViewById(R.id.handbrakeOn);
        mVideoOnBtn=view.findViewById(R.id.videoOn);
        mVideoOffBtn=view.findViewById(R.id.videoOff);





        mSubScribeBroadcastBtn = view.findViewById(R.id.subscribe_broadcast_topic);
        mCheckFirmwareBtn = view.findViewById(R.id.check_firmware);
        mDynRegBtn = view.findViewById(R.id.dynreg);
        mLogInfoText = view.findViewById(R.id.log_info);
        mSpinner = view.findViewById(R.id.spinner4);
        mItemText = view.findViewById(R.id.editText2);
        mSubdevOnlineBtn = view.findViewById(R.id.subdev_online);
        mSubdevOfflineBtn = view.findViewById(R.id.subdev_offline);
        mDeviceLogBtn = view.findViewById(R.id.mlog);
        mUploadLogBtn = view.findViewById(R.id.uploadlog);
        mSubScribeRRPCBtn = view.findViewById(R.id.subscribe_rrpc_topic);
        mSubdevBindedBtn = view.findViewById(R.id.subdev_binded);
        mSubdevUnbindedBtn = view.findViewById(R.id.subdev_unbinded);
        mSubdevRelationCheckBtn = view.findViewById(R.id.check_subdev_relation);
        mConnectWebSocketBtn = view.findViewById(R.id.websocket_connect);
        mCloseConnectWebSocketBtn = view.findViewById(R.id.websocket_disconnect);
        mConnectStatusWebSocketBtn = view.findViewById(R.id.websocket_status);
        mGetRemoteConfigBtn = view.findViewById(R.id.get_remote_config);
        mConcernRemoteConfigBtn = view.findViewById(R.id.concern_remote_config);
        mReportSubDevVersionBtn = view.findViewById(R.id.report_sub_dev_version);

    }

    public void closeConnection() {
        if (mMQTTSample == null)
            return;
        mMQTTSample.disconnect();
    }

    /**
     * 实现TXMqttActionCallBack回调接口
     */
    private class SelfMqttActionCallBack extends TXMqttActionCallBack {

        @Override
        public void onConnectCompleted(Status status, boolean reconnect, Object userContext, String msg) {
            String userContextInfo = "";
            if (userContext instanceof MQTTRequest) {
                userContextInfo = userContext.toString();
            }
            String logInfo = String.format("onConnectCompleted, status[%s], reconnect[%b], userContext[%s], msg[%s]",
                    status.name(), reconnect, userContextInfo, msg);
            mParent.printLogInfo(TAG, logInfo, mLogInfoText, TXLog.LEVEL_INFO);
            mIsConnected = true;
        }

        @Override
        public void onConnectionLost(Throwable cause) {
            String logInfo = String.format("onConnectionLost, cause[%s]", cause.toString());
            mParent.printLogInfo(TAG, logInfo, mLogInfoText, TXLog.LEVEL_INFO);
        }

        @Override
        public void onDisconnectCompleted(Status status, Object userContext, String msg) {
            String userContextInfo = "";
            if (userContext instanceof MQTTRequest) {
                userContextInfo = userContext.toString();
            }
            String logInfo = String.format("onDisconnectCompleted, status[%s], userContext[%s], msg[%s]", status.name(), userContextInfo, msg);
            mParent.printLogInfo(TAG, logInfo, mLogInfoText, TXLog.LEVEL_INFO);
            mIsConnected = false;
        }

        @Override
        public void onPublishCompleted(Status status, IMqttToken token, Object userContext, String errMsg) {
            String userContextInfo = "";
            if (userContext instanceof MQTTRequest) {
                userContextInfo = userContext.toString();
            }
            String logInfo = String.format("onPublishCompleted, status[%s], topics[%s],  userContext[%s], errMsg[%s]",
                    status.name(), Arrays.toString(token.getTopics()), userContextInfo, errMsg);
            mParent.printLogInfo(TAG, logInfo, mLogInfoText);
        }

        @Override
        public void onSubscribeCompleted(Status status, IMqttToken asyncActionToken, Object userContext, String errMsg) {
            String userContextInfo = "";
            if (userContext instanceof MQTTRequest) {
                userContextInfo = userContext.toString();
            }
            String logInfo = String.format("onSubscribeCompleted, status[%s], topics[%s], userContext[%s], errMsg[%s]",
                    status.name(), Arrays.toString(asyncActionToken.getTopics()), userContextInfo, errMsg);
            if (Status.ERROR == status) {
                mParent.printLogInfo(TAG, logInfo, mLogInfoText, TXLog.LEVEL_ERROR);
            } else {
                mParent.printLogInfo(TAG, logInfo, mLogInfoText);
            }
        }

        @Override
        public void onUnSubscribeCompleted(Status status, IMqttToken asyncActionToken, Object userContext, String errMsg) {
            String userContextInfo = "";
            if (userContext instanceof MQTTRequest) {
                userContextInfo = userContext.toString();
            }
            String logInfo = String.format("onUnSubscribeCompleted, status[%s], topics[%s], userContext[%s], errMsg[%s]",
                    status.name(), Arrays.toString(asyncActionToken.getTopics()), userContextInfo, errMsg);
            mParent.printLogInfo(TAG, logInfo, mLogInfoText);
        }

        @Override
        public void onMessageReceived(final String topic, final MqttMessage message) {
            String logInfo = String.format("receive command, topic[%s], message[%s]", topic, message.toString());
            mParent.printLogInfo(TAG, logInfo, mLogInfoText);
        }
    }

    /**
     * 实现TXMqttLogCallBack回调接口
     */
    private class SelfMqttLogCallBack extends TXMqttLogCallBack {

        @Override
        public String setSecretKey() {
            String secertKey;
            if (mDevPSK != null && mDevPSK.length() != 0) {  //密钥认证
                secertKey = mDevPSK;
                secertKey = secertKey.length() > 24 ? secertKey.substring(0,24) : secertKey;
                return secertKey;
            } else {
                BufferedReader cert;

                if (mDevCert != null && mDevCert.length() != 0) { //动态注册,从DevCert中读取
                     cert = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(mDevCert.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));

                } else { //证书认证，从证书文件中读取
                    AssetManager assetManager = mParent.getAssets();
                    if (assetManager == null) {
                        return null;
                    }
                    try {
                        cert=new BufferedReader(new InputStreamReader(assetManager.open(mDevCertName)));
                    } catch (IOException e) {
                        mParent.printLogInfo(TAG, "getSecertKey failed, cannot open CRT Files.",mLogInfoText);
                        return null;
                    }
                }
                //获取密钥
                try {
                    if (cert.readLine().contains("-----BEGIN")) {
                        secertKey = cert.readLine();
                        secertKey = secertKey.length() > 24 ? secertKey.substring(0,24) : secertKey;
                    } else {
                        secertKey = null;
                        mParent.printLogInfo(TAG,"Invaild CRT Files.", mLogInfoText);
                    }
                    cert.close();
                } catch (IOException e) {
                    TXLog.e(TAG, "getSecertKey failed.", e);
                    mParent.printLogInfo(TAG,"getSecertKey failed.", mLogInfoText);
                    return null;
                }
            }

            return secertKey;
        }

        @Override
        public void printDebug(String message){
            mParent.printLogInfo(TAG, message, mLogInfoText);
            //TXLog.d(TAG,message);
        }

        @Override
        public boolean saveLogOffline(String log){
            //判断SD卡是否可用
            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                mParent.printLogInfo(TAG, "saveLogOffline not ready", mLogInfoText);
                return false;
            }

            String logFilePath = mLogPath + mProductID + mDevName + ".log";

            TXLog.i(TAG, "Save log to %s", logFilePath);

            try {
                BufferedWriter wLog = new BufferedWriter(new FileWriter(new File(logFilePath), true));
                wLog.write(log);
                wLog.flush();
                wLog.close();
                return true;
            } catch (IOException e) {
                String logInfo = String.format("Save log to [%s] failed, check the Storage permission!", logFilePath);
                mParent.printLogInfo(TAG,logInfo, mLogInfoText);
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public String readOfflineLog(){
            //判断SD卡是否可用
            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                mParent.printLogInfo(TAG, "readOfflineLog not ready", mLogInfoText);
                return null;
            }

            String logFilePath = mLogPath + mProductID + mDevName + ".log";

            TXLog.i(TAG, "Read log from %s", logFilePath);

            try {
                BufferedReader logReader = new BufferedReader(new FileReader(logFilePath));
                StringBuilder offlineLog = new StringBuilder();
                int data;
                while (( data = logReader.read()) != -1 ) {
                   offlineLog.append((char)data);
                }
                logReader.close();
                return offlineLog.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public boolean delOfflineLog(){

            //判断SD卡是否可用
            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                mParent.printLogInfo(TAG, "delOfflineLog not ready", mLogInfoText);
                return false;
            }

            String logFilePath = mLogPath + mProductID + mDevName + ".log";

            File file = new File(logFilePath);
            if (file.exists() && file.isFile()) {
                if (file.delete()) {
                    return true;
                }
            }
            return false;
        }

    }

    /**
     * Callback for dynamic register
     */
    private class SelfMqttDynregCallback extends TXMqttDynregCallback {

        @Override
        public void onGetDevicePSK(String devicePsk) {
            SharedPreferences sharedPreferences =  mParent.getSharedPreferences("config",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(DEVICE_PSK, devicePsk);
            editor.commit();
            mDevPSK = devicePsk;
            String logInfo = String.format("Dynamic register OK! onGetDevicePSK, devicePSK[%s]", devicePsk);
            mParent.printLogInfo(TAG, logInfo, mLogInfoText, TXLog.LEVEL_INFO);
        }

        @Override
        public void onGetDeviceCert(String deviceCert, String devicePriv) {
            SharedPreferences sharedPreferences =  mParent.getSharedPreferences("config",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(DEVICE_CERT, deviceCert);
            editor.putString(DEVICE_PRIV, devicePriv);
            editor.commit();
            mDevCert = deviceCert;
            mDevPriv = devicePriv;
            String logInfo = String.format("Dynamic register OK!onGetDeviceCert, deviceCert[%s] devicePriv[%s]", deviceCert, devicePriv);
            mParent.printLogInfo(TAG, logInfo, mLogInfoText, TXLog.LEVEL_INFO);
        }

        @Override
        public void onFailedDynreg(Throwable cause, String errMsg) {
            String logInfo = String.format("Dynamic register failed! onFailedDynreg, ErrMsg[%s]", cause.toString() + errMsg);
            mParent.printLogInfo(TAG, logInfo, mLogInfoText, TXLog.LEVEL_ERROR);
        }

        @Override
        public void onFailedDynreg(Throwable cause) {
            String logInfo = String.format("Dynamic register failed! onFailedDynreg, ErrMsg[%s]", cause.toString());
            mParent.printLogInfo(TAG, logInfo, mLogInfoText, TXLog.LEVEL_ERROR);
        }
    }
}
