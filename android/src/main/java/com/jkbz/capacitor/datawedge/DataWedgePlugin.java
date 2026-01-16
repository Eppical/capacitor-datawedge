package com.jkbz.capacitor.datawedge;

import com.getcapacitor.Plugin;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;

import static android.content.Context.RECEIVER_EXPORTED;

import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.ActivityNotFoundException;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.util.Log;


@CapacitorPlugin(name = "DataWedge")
public class DataWedgePlugin extends Plugin {

    private final DataWedge implementation = new DataWedge();
    private static final String EXTRA_RESULT_GET_VERSION_INFO = "com.symbol.datawedge.api.RESULT_GET_VERSION_INFO";
    private static final String EXTRA_RESULT_GET_SCANNER_STATUS = "com.symbol.datawedge.api.RESULT_GET_SCANNER_STATUS";
    private static final String EXTRA_RESULT_COMMAND = "COMMAND";
    private static final String EXTRA_RESULT_STATUS = "RESULT";
    private static final long RESULT_TIMEOUT_MS = 3000;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // This is the default and can be changed when re-registering
    private String scanIntent = "com.capacitor.datawedge.RESULT_ACTION";
    private String resultIntent = DataWedge.DATAWEDGE_RESULT_ACTION;

    @PluginMethod
    public void enable(PluginCall call) {
        Intent intent = implementation.enable();

        try {
            broadcast(intent);
        } catch (ActivityNotFoundException e) {
            call.reject("DataWedge is not installed or not running");
        }
    }
    @PluginMethod
    public void disable(PluginCall call) {
        Intent intent = implementation.disable();

        try {
            broadcast(intent);
        } catch (ActivityNotFoundException e) {
            call.reject("DataWedge is not installed or not running");
        }
    }

    @PluginMethod
    public void enableScanner(PluginCall call) {
        Intent intent = implementation.enableScanner();

        try {
            broadcast(intent);
        } catch (ActivityNotFoundException e) {
            call.reject("DataWedge is not installed or not running");
        }
    }

    @PluginMethod
    public void disableScanner(PluginCall call) {
        Intent intent = implementation.disableScanner();

        try {
            broadcast(intent);
        } catch (ActivityNotFoundException e) {
            call.reject("DataWedge is not installed or not running");
        }
    }

    @PluginMethod
    public void startScanning(PluginCall call) {
         Intent intent = implementation.startScanning();

         try {
            broadcast(intent);
         } catch (ActivityNotFoundException e) {
            call.reject("DataWedge is not installed or not running");
         }
    }

    @PluginMethod
    public void stopScanning(PluginCall call) {
        Intent intent = implementation.stopScanning();

        try {
            broadcast(intent);
        } catch (ActivityNotFoundException e) {
            call.reject("DataWedge is not installed or not running");
        }
    }

    @PluginMethod
    public void isReady(PluginCall call) {
        Intent intent = implementation.getVersionInfo();

        try {
            call.setKeepAlive(true);
            pendingReadyCall = call;
            scheduleTimeout(ReadyTimeoutType.READY);
            broadcast(intent);
        } catch (ActivityNotFoundException e) {
            call.reject("DataWedge is not installed or not running");
        }
    }

    @PluginMethod
    public void hasScanner(PluginCall call) {
        Intent intent = implementation.getScannerStatus();

        try {
            call.setKeepAlive(true);
            pendingScannerCall = call;
            scheduleTimeout(ReadyTimeoutType.SCANNER);
            broadcast(intent);
        } catch (ActivityNotFoundException e) {
            call.reject("DataWedge is not installed or not running");
        }
    }

    @PluginMethod
    public void configure(PluginCall call) {
        String profileName = call.getString("profileName");
        if (profileName == null || profileName.trim().isEmpty()) {
            call.reject("profileName is required");
            return;
        }

        String packageName = call.getString("packageName");
        if (packageName == null || packageName.trim().isEmpty()) {
            packageName = getBridge().getContext().getPackageName();
        }

        String intentAction = call.getString("intentAction");
        if (intentAction == null || intentAction.trim().isEmpty()) {
            intentAction = scanIntent;
        }

        String activityName = call.getString("activityName");
        if (activityName == null || activityName.trim().isEmpty()) {
            activityName = "*";
        }

        try {
            call.setKeepAlive(true);
            pendingConfigureCall = call;
            scheduleTimeout(ReadyTimeoutType.CONFIGURE);
            broadcast(implementation.createProfile(profileName));
            broadcast(implementation.setConfig(profileName, packageName, activityName, intentAction));
        } catch (ActivityNotFoundException e) {
            call.reject("DataWedge is not installed or not running");
        }
    }

    @PluginMethod
    public void __registerReceiver(PluginCall call) { 
        Context context = getBridge().getContext();

        if (isReceiverRegistered) {
          context.unregisterReceiver(broadcastReceiver);
        }

        final String intentName = call.getString("intent");
        if (intentName != null) this.scanIntent = intentName;

        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(this.scanIntent);
            filter.addAction(this.resultIntent);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
              context.registerReceiver(broadcastReceiver, filter, RECEIVER_EXPORTED);
            } else {
              context.registerReceiver(broadcastReceiver, filter);
            }

            isReceiverRegistered = true;
        } catch(Exception e) {
            Log.d("Capacitor/DataWedge", "Failed to register event receiver");
        }
    }

    private void broadcast(Intent intent) {
        Context context = getBridge().getContext();
        context.sendBroadcast(intent);
    }

    private void scheduleTimeout(ReadyTimeoutType timeoutType) {
        Runnable timeoutTask = () -> {
            if (timeoutType == ReadyTimeoutType.READY && pendingReadyCall != null) {
                pendingReadyCall.reject("DataWedge did not respond in time");
                pendingReadyCall = null;
            }
            if (timeoutType == ReadyTimeoutType.SCANNER && pendingScannerCall != null) {
                pendingScannerCall.reject("DataWedge did not respond in time");
                pendingScannerCall = null;
            }
            if (timeoutType == ReadyTimeoutType.CONFIGURE && pendingConfigureCall != null) {
                pendingConfigureCall.reject("DataWedge did not respond in time");
                pendingConfigureCall = null;
            }
        };

        if (timeoutType == ReadyTimeoutType.READY) {
            readyTimeoutTask = timeoutTask;
        }
        if (timeoutType == ReadyTimeoutType.SCANNER) {
            scannerTimeoutTask = timeoutTask;
        }
        if (timeoutType == ReadyTimeoutType.CONFIGURE) {
            configureTimeoutTask = timeoutTask;
        }

        mainHandler.postDelayed(timeoutTask, RESULT_TIMEOUT_MS);
    }

    private void cancelTimeout(ReadyTimeoutType timeoutType) {
        if (timeoutType == ReadyTimeoutType.READY && readyTimeoutTask != null) {
            mainHandler.removeCallbacks(readyTimeoutTask);
            readyTimeoutTask = null;
        }
        if (timeoutType == ReadyTimeoutType.SCANNER && scannerTimeoutTask != null) {
            mainHandler.removeCallbacks(scannerTimeoutTask);
            scannerTimeoutTask = null;
        }
        if (timeoutType == ReadyTimeoutType.CONFIGURE && configureTimeoutTask != null) {
            mainHandler.removeCallbacks(configureTimeoutTask);
            configureTimeoutTask = null;
        }
    }

    private JSObject bundleToJSObject(Bundle bundle) {
        JSObject obj = new JSObject();
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            if (value != null) {
                obj.put(key, value.toString());
            }
        }
        return obj;
    }

    private boolean isScannerAvailableFromStatus(String status) {
        if (status == null) {
            return false;
        }

        String normalized = status.trim().toUpperCase();
        return !normalized.equals("NO_SCANNER") && !normalized.equals("SCANNER_NOT_AVAILABLE");
    }

    private boolean isReceiverRegistered = false;
    private PluginCall pendingReadyCall;
    private PluginCall pendingScannerCall;
    private PluginCall pendingConfigureCall;
    private Runnable readyTimeoutTask;
    private Runnable scannerTimeoutTask;
    private Runnable configureTimeoutTask;

    private enum ReadyTimeoutType {
        READY,
        SCANNER,
        CONFIGURE
    }
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(scanIntent)) {
                try {
                    String data = intent.getStringExtra("com.symbol.datawedge.data_string");
                    String type = intent.getStringExtra("com.symbol.datawedge.label_type");

                    JSObject ret = new JSObject();
                    ret.put("data", data);
                    ret.put("type", type);

                    notifyListeners("scan", ret);
                } catch(Exception e) {}
                return;
            }

            if (!action.equals(resultIntent)) {
                return;
            }

            if (pendingReadyCall != null && intent.hasExtra(EXTRA_RESULT_GET_VERSION_INFO)) {
                cancelTimeout(ReadyTimeoutType.READY);
                Bundle versionBundle = intent.getBundleExtra(EXTRA_RESULT_GET_VERSION_INFO);

                JSObject ret = new JSObject();
                ret.put("ready", versionBundle != null);
                if (versionBundle != null) {
                    ret.put("versionInfo", bundleToJSObject(versionBundle));
                }

                pendingReadyCall.resolve(ret);
                pendingReadyCall = null;
                return;
            }

            if (pendingScannerCall != null && intent.hasExtra(EXTRA_RESULT_GET_SCANNER_STATUS)) {
                cancelTimeout(ReadyTimeoutType.SCANNER);
                Bundle statusBundle = intent.getBundleExtra(EXTRA_RESULT_GET_SCANNER_STATUS);
                String status = null;
                if (statusBundle != null) {
                    status = statusBundle.getString("SCANNER_STATUS");
                    if (status == null) {
                        status = statusBundle.getString("STATUS");
                    }
                }
                if (status == null) {
                    status = intent.getStringExtra("SCANNER_STATUS");
                }

                JSObject ret = new JSObject();
                ret.put("status", status);
                ret.put("hasScanner", isScannerAvailableFromStatus(status));

                pendingScannerCall.resolve(ret);
                pendingScannerCall = null;
                return;
            }

            if (pendingConfigureCall != null) {
                String command = intent.getStringExtra(EXTRA_RESULT_COMMAND);
                String result = intent.getStringExtra(EXTRA_RESULT_STATUS);
                if (command != null && command.contains("SET_CONFIG")) {
                    cancelTimeout(ReadyTimeoutType.CONFIGURE);
                    JSObject ret = new JSObject();
                    ret.put("command", command);
                    ret.put("result", result);
                    ret.put("success", "SUCCESS".equalsIgnoreCase(result));
                    pendingConfigureCall.resolve(ret);
                    pendingConfigureCall = null;
                }
            }
        }
    };
}
