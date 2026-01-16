package com.jkbz.capacitor.datawedge;

import android.content.Intent;
import android.os.Bundle;
import java.util.ArrayList;

public class DataWedge {
    public static final String DATAWEDGE_PACKAGE = "com.symbol.datawedge.api";
    public static final String DATAWEDGE_INPUT_FILTER = "com.capacitor.datawedge.RESULT_ACTION";
    public static final String DATAWEDGE_RESULT_ACTION = "com.symbol.datawedge.api.RESULT_ACTION";
    public static final String EXTRA_SEND_RESULT = "SEND_RESULT";
    public static final String EXTRA_CREATE_PROFILE = "com.symbol.datawedge.api.CREATE_PROFILE";
    public static final String EXTRA_SET_CONFIG = "com.symbol.datawedge.api.SET_CONFIG";
    public static final String EXTRA_GET_VERSION_INFO = "com.symbol.datawedge.api.GET_VERSION_INFO";
    public static final String EXTRA_GET_SCANNER_STATUS = "com.symbol.datawedge.api.GET_SCANNER_STATUS";

    public Intent enable() {
        Intent intent = new Intent();
        intent.setAction(DATAWEDGE_PACKAGE + ".ACTION");
        intent.putExtra("com.symbol.datawedge.api.ENABLE_DATAWEDGE", true);

        return intent;
    }

    public Intent disable() {
        Intent intent = new Intent();
        intent.setAction(DATAWEDGE_PACKAGE + ".ACTION");
        intent.putExtra("com.symbol.datawedge.api.ENABLE_DATAWEDGE", false);

        return intent;
    }

    public Intent enableScanner() {
        Intent intent = new Intent();
        intent.setAction(DATAWEDGE_PACKAGE + ".ACTION");
        intent.putExtra("com.symbol.datawedge.api.SCANNER_INPUT_PLUGIN", "ENABLE_PLUGIN");

        return intent;
    }

    public Intent disableScanner() {
        Intent intent = new Intent();
        intent.setAction(DATAWEDGE_PACKAGE + ".ACTION");
        intent.putExtra("com.symbol.datawedge.api.SCANNER_INPUT_PLUGIN", "DISABLE_PLUGIN");

        return intent;
    }

    public Intent startScanning() {
        Intent intent = new Intent();
        intent.setAction(DATAWEDGE_PACKAGE + ".ACTION");
        intent.putExtra("com.symbol.datawedge.api.SOFT_SCAN_TRIGGER", "START_SCANNING");

        return intent;
    }

    public Intent stopScanning() {
        Intent intent = new Intent();
        intent.setAction(DATAWEDGE_PACKAGE + ".ACTION");
        intent.putExtra("com.symbol.datawedge.api.SOFT_SCAN_TRIGGER", "STOP_SCANNING");

        return intent;
    }

    public Intent getVersionInfo() {
        Intent intent = new Intent();
        intent.setAction(DATAWEDGE_PACKAGE + ".ACTION");
        intent.putExtra(EXTRA_GET_VERSION_INFO, "");
        intent.putExtra(EXTRA_SEND_RESULT, "true");

        return intent;
    }

    public Intent getScannerStatus() {
        Intent intent = new Intent();
        intent.setAction(DATAWEDGE_PACKAGE + ".ACTION");
        intent.putExtra(EXTRA_GET_SCANNER_STATUS, "");
        intent.putExtra(EXTRA_SEND_RESULT, "true");

        return intent;
    }

    public Intent createProfile(String profileName) {
        Intent intent = new Intent();
        intent.setAction(DATAWEDGE_PACKAGE + ".ACTION");
        intent.putExtra(EXTRA_CREATE_PROFILE, profileName);
        intent.putExtra(EXTRA_SEND_RESULT, "true");

        return intent;
    }

    public Intent setConfig(String profileName, String packageName, String activityName, String intentAction) {
        Intent intent = new Intent();
        intent.setAction(DATAWEDGE_PACKAGE + ".ACTION");

        Bundle profileConfig = new Bundle();
        profileConfig.putString("PROFILE_NAME", profileName);
        profileConfig.putString("PROFILE_ENABLED", "true");
        profileConfig.putString("CONFIG_MODE", "UPDATE");

        Bundle barcodeProps = new Bundle();
        barcodeProps.putString("scanner_selection", "auto");
        barcodeProps.putString("scanner_input_enabled", "true");

        Bundle barcodeConfig = new Bundle();
        barcodeConfig.putString("PLUGIN_NAME", "BARCODE");
        barcodeConfig.putString("RESET_CONFIG", "true");
        barcodeConfig.putBundle("PARAM_LIST", barcodeProps);

        Bundle intentProps = new Bundle();
        intentProps.putString("intent_output_enabled", "true");
        intentProps.putString("intent_action", intentAction);
        intentProps.putString("intent_delivery", "2");

        Bundle intentConfig = new Bundle();
        intentConfig.putString("PLUGIN_NAME", "INTENT");
        intentConfig.putString("RESET_CONFIG", "true");
        intentConfig.putBundle("PARAM_LIST", intentProps);

        ArrayList<Bundle> pluginConfigs = new ArrayList<>();
        pluginConfigs.add(barcodeConfig);
        pluginConfigs.add(intentConfig);
        profileConfig.putParcelableArrayList("PLUGIN_CONFIG", pluginConfigs);

        Bundle appConfig = new Bundle();
        appConfig.putString("PACKAGE_NAME", packageName);
        appConfig.putStringArray("ACTIVITY_LIST", new String[] { activityName });

        ArrayList<Bundle> appList = new ArrayList<>();
        appList.add(appConfig);
        profileConfig.putParcelableArrayList("APP_LIST", appList);

        intent.putExtra(EXTRA_SET_CONFIG, profileConfig);
        intent.putExtra(EXTRA_SEND_RESULT, "true");

        return intent;
    }
}
