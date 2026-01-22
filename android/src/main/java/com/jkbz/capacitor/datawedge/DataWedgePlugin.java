package com.jkbz.capacitor.datawedge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Java implementation inspired by community capacitor-datawedge plugins.
 * - Dynamically registers BroadcastReceiver while app is in foreground.
 * - Auto creates/updates DataWedge profile on initialize().
 * - getAvailability() resolves as a Promise after collecting DW responses.
 */
@CapacitorPlugin(name = "DataWedge")
public class DataWedgePlugin extends Plugin {

  // DataWedge core
  private static final String DW_API_ACTION = "com.symbol.datawedge.api.ACTION";
  private static final String DW_RESULT_ACTION = "com.symbol.datawedge.api.RESULT_ACTION";
  private static final String DW_ENUMERATED_ACTION = "com.symbol.datawedge.api.ACTION_ENUMERATEDSCANNERLIST";

  // APIs
  private static final String EXTRA_SET_CONFIG = "com.symbol.datawedge.api.SET_CONFIG";
  private static final String EXTRA_GET_DW_STATUS = "com.symbol.datawedge.api.GET_DATAWEDGE_STATUS";
  private static final String EXTRA_ENUM_SCANNERS = "com.symbol.datawedge.api.ENUMERATE_SCANNERS";
  private static final String EXTRA_GET_SCANNER_STATUS = "com.symbol.datawedge.api.GET_SCANNER_STATUS";

  // Result plumbing
  private static final String EXTRA_SEND_RESULT = "SEND_RESULT";
  private static final String EXTRA_COMMAND_IDENTIFIER = "COMMAND_IDENTIFIER";

  // Result keys (TechDocs)
  private static final String RESULT_GET_DW_STATUS = "com.symbol.datawedge.api.RESULT_GET_DATAWEDGE_STATUS"; // Bundle
  private static final String RESULT_ENUMERATE_SCANNERS = "com.symbol.datawedge.api.RESULT_ENUMERATE_SCANNERS"; // Serializable ArrayList<Bundle>
  private static final String RESULT_SCANNER_STATUS = "com.symbol.datawedge.api.RESULT_SCANNER_STATUS"; // String

  // Scan keys (standard)
  private static final String KEY_DATA_STRING = "com.symbol.datawedge.data_string";
  private static final String KEY_LABEL_TYPE  = "com.symbol.datawedge.label_type";
  private static final String KEY_SOURCE      = "com.symbol.datawedge.source";

  private static final String DEFAULT_INTENT_CATEGORY = Intent.CATEGORY_DEFAULT;

  // Defaults
  private String profileName = "CAP_DW_PROFILE";
  private String scanIntentAction = null; // default set in load()
  private String intentCategory = DEFAULT_INTENT_CATEGORY;

  private boolean receiverRegistered = false;

  // Single outstanding availability promise (startup-friendly)
  private AvailabilityRequest pendingAvailability = null;

  // Handler para timeout activo
  private final Handler mainHandler = new Handler(Looper.getMainLooper());
  private Runnable timeoutRunnable = null;

  private static class AvailabilityRequest {
    String reqId;
    long deadlineMs;
    PluginCall call;

    @Nullable String dwStatusString;
    @Nullable String scannerStatus;
    @Nullable ArrayList<Bundle> scannerList;

    AvailabilityRequest(String reqId, long deadlineMs, PluginCall call) {
      this.reqId = reqId;
      this.deadlineMs = deadlineMs;
      this.call = call;
    }

    boolean isComplete() {
      return dwStatusString != null && scannerStatus != null && scannerList != null;
    }
  }

  private final BroadcastReceiver dwReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent == null) return;
      String action = intent.getAction();
      if (action == null) return;

      // 1) Scan broadcast
      if (action.equals(scanIntentAction)) {
        String data = intent.getStringExtra(KEY_DATA_STRING);
        if (data != null) {
          JSObject payload = new JSObject();
          payload.put("data", data);
          payload.put("labelType", intent.getStringExtra(KEY_LABEL_TYPE));
          payload.put("source", intent.getStringExtra(KEY_SOURCE));
          notifyListeners("scan", payload);
        }
        return;
      }

      // 2) Results from DataWedge APIs
      if (action.equals(DW_RESULT_ACTION) || action.equals(DW_ENUMERATED_ACTION)) {
        String cmdId = intent.getStringExtra(EXTRA_COMMAND_IDENTIFIER);
        Bundle extras = intent.getExtras();

        // Debug event
        JSObject evt = new JSObject();
        evt.put("action", action);
        evt.put("commandIdentifier", cmdId);
        evt.put("extras", bundleToJson(extras));
        notifyListeners("datawedgeResult", evt);

        if (pendingAvailability != null) {
          long now = System.currentTimeMillis();
          if (now > pendingAvailability.deadlineMs) {
            // En lugar de rechazar, resolver con datos parciales + timedOut=true
            resolveAvailabilityWithTimeout(pendingAvailability);
            pendingAvailability = null;
            return;
          }

          if (extras != null) {
            if (extras.containsKey(RESULT_GET_DW_STATUS) && pendingAvailability.dwStatusString == null) {
              // DataWedge returns a String (e.g., "ENABLED" or "DISABLED"), not a Bundle
              Object dwStatus = extras.get(RESULT_GET_DW_STATUS);
              if (dwStatus instanceof String) {
                pendingAvailability.dwStatusString = (String) dwStatus;
              } else if (dwStatus instanceof Bundle) {
                // Fallback for older DataWedge versions that might return a Bundle
                Bundle b = (Bundle) dwStatus;
                for (String k : b.keySet()) {
                  Object v = b.get(k);
                  if (v instanceof String) {
                    pendingAvailability.dwStatusString = (String) v;
                    break;
                  }
                }
              }
            }

            if (extras.containsKey(RESULT_SCANNER_STATUS) && pendingAvailability.scannerStatus == null) {
              pendingAvailability.scannerStatus = extras.getString(RESULT_SCANNER_STATUS);
            }

            if (extras.containsKey(RESULT_ENUMERATE_SCANNERS) && pendingAvailability.scannerList == null) {
              try {
                Serializable ser = extras.getSerializable(RESULT_ENUMERATE_SCANNERS);
                if (ser instanceof ArrayList) {
                  //noinspection unchecked
                  pendingAvailability.scannerList = (ArrayList<Bundle>) ser;
                }
              } catch (Exception ignored) {}
            }
          }

          if (pendingAvailability.isComplete()) {
            resolveAvailability(pendingAvailability);
            pendingAvailability = null;
          }
        }
      }
    }
  };

  @Override
  public void load() {
    super.load();
    if (scanIntentAction == null) {
      scanIntentAction = getContext().getPackageName() + ".SCAN";
    }
  }

  // lifecycle: dynamic receiver
  @Override
  protected void handleOnResume() {
    super.handleOnResume();
    registerReceiver();
  }

  @Override
  protected void handleOnPause() {
    unregisterReceiver();
    super.handleOnPause();
  }

  @Override
  protected void handleOnDestroy() {
    unregisterReceiver();
    super.handleOnDestroy();
  }

  private void registerReceiver() {
    if (receiverRegistered) return;

    IntentFilter filter = new IntentFilter();
    filter.addAction(scanIntentAction);
    filter.addAction(DW_RESULT_ACTION);
    filter.addAction(DW_ENUMERATED_ACTION);
    filter.addCategory(Intent.CATEGORY_DEFAULT);

    ContextCompat.registerReceiver(getContext(), dwReceiver, filter, ContextCompat.RECEIVER_EXPORTED);
    receiverRegistered = true;
  }

  private void unregisterReceiver() {
    if (!receiverRegistered) return;
    try {
      getContext().unregisterReceiver(dwReceiver);
    } catch (Exception ignored) {}
    receiverRegistered = false;
  }

  // ---- Public API ----

  @PluginMethod
  public void initialize(PluginCall call) {
    String pn = call.getString("profileName");
    String ia = call.getString("intentAction");
    String ic = call.getString("intentCategory");

    if (pn != null && !pn.trim().isEmpty()) profileName = pn.trim();
    if (ia != null && !ia.trim().isEmpty()) scanIntentAction = ia.trim();
    if (ic != null && !ic.trim().isEmpty()) intentCategory = ic.trim();

    registerReceiver();

    // Create/update profile
    setConfigProfile();

    JSObject ret = new JSObject();
    ret.put("profileName", profileName);
    ret.put("intentAction", scanIntentAction);
    call.resolve(ret);
  }

  /**
   * Returns DataWedge + scanner availability as a single Promise.
   * Zebra recommends polling GET_DATAWEDGE_STATUS until a response is received after boot.
   */
  @PluginMethod
  public void getAvailability(PluginCall call) {
    registerReceiver();

    // Cancelar timeout anterior si existe
    if (timeoutRunnable != null) {
      mainHandler.removeCallbacks(timeoutRunnable);
      timeoutRunnable = null;
    }

    if (pendingAvailability != null) {
      try { pendingAvailability.call.reject("Replaced by a new getAvailability() call"); } catch (Exception ignored) {}
      pendingAvailability = null;
    }

    int timeoutMs = 1000;
    Integer t = call.getInt("timeoutMs");
    if (t != null && t > 0) timeoutMs = t;

    String reqId = "AVAIL_" + UUID.randomUUID();
    long deadline = System.currentTimeMillis() + timeoutMs;
    pendingAvailability = new AvailabilityRequest(reqId, deadline, call);

    // Programar timeout activo
    timeoutRunnable = () -> {
      if (pendingAvailability != null) {
        resolveAvailabilityWithTimeout(pendingAvailability);
        pendingAvailability = null;
      }
      timeoutRunnable = null;
    };
    mainHandler.postDelayed(timeoutRunnable, timeoutMs);

    sendDwCommand(EXTRA_GET_DW_STATUS, "", "GET_DW_STATUS_" + reqId);
    sendDwCommand(EXTRA_ENUM_SCANNERS, "", "ENUM_SCANNERS_" + reqId);
    sendDwCommand(EXTRA_GET_SCANNER_STATUS, "", "GET_SCANNER_STATUS_" + reqId);
  }

  // ---- DataWedge profile config (SET_CONFIG) ----
  private void setConfigProfile() {
 Context ctx = getContext();
    String pkg = ctx.getPackageName();

    Bundle profileConfig = new Bundle();
    profileConfig.putString("PROFILE_NAME", profileName);
    profileConfig.putString("PROFILE_ENABLED", "true");
    profileConfig.putString("CONFIG_MODE", "CREATE_IF_NOT_EXIST");

    // Esto es CLAVE para que no queden defaults viejos (como Keystroke ON)
    profileConfig.putString("RESET_CONFIG", "true");

    // ---------- APP_LIST ----------
    Bundle app = new Bundle();
    app.putString("PACKAGE_NAME", pkg);
    app.putStringArray("ACTIVITY_LIST", new String[] { "*" });
    profileConfig.putParcelableArray("APP_LIST", new Bundle[] { app });

    // ---------- PLUGIN_CONFIG (ArrayList como en los samples oficiales) ----------
    ArrayList<Bundle> pluginConfig = new ArrayList<>();

    // BARCODE
    Bundle barcodeParams = new Bundle();
    barcodeParams.putString("scanner_input_enabled", "true");

    // Para TC26 podés dejar "auto" + configure_all_scanners=true para aplicar reader params
    // (si querés forzar, probá luego INTERNAL_IMAGER, pero primero hacelo “creable”)
    barcodeParams.putString("scanner_selection", "auto");
    barcodeParams.putString("configure_all_scanners", "true");

    // LCD Mode enabled: "3"
    barcodeParams.putString("lcd_mode", "3");

    Bundle barcodeConfig = new Bundle();
    barcodeConfig.putString("PLUGIN_NAME", "BARCODE");
    barcodeConfig.putString("RESET_CONFIG", "true");
    barcodeConfig.putBundle("PARAM_LIST", barcodeParams);
    pluginConfig.add(barcodeConfig);

    // INTENT
    Bundle intentParams = new Bundle();
    intentParams.putString("intent_output_enabled", "true");
    intentParams.putString("intent_action", scanIntentAction);

    // Broadcast = 2  (IMPORTANTE: int, no string)
    intentParams.putInt("intent_delivery", 2);

    // Si no necesitás category, NO la mandes
    // intentParams.putString("intent_category", "android.intent.category.DEFAULT");

    Bundle intentConfig = new Bundle();
    intentConfig.putString("PLUGIN_NAME", "INTENT");
    intentConfig.putString("RESET_CONFIG", "true");
    intentConfig.putBundle("PARAM_LIST", intentParams);
    pluginConfig.add(intentConfig);

    // KEYSTROKE OFF
    Bundle keystrokeParams = new Bundle();
    keystrokeParams.putString("keystroke_output_enabled", "false");

    Bundle keystrokeConfig = new Bundle();
    keystrokeConfig.putString("PLUGIN_NAME", "KEYSTROKE");
    keystrokeConfig.putString("RESET_CONFIG", "true");
    keystrokeConfig.putBundle("PARAM_LIST", keystrokeParams);
    pluginConfig.add(keystrokeConfig);

    // Meter la lista de plugins
    profileConfig.putParcelableArrayList("PLUGIN_CONFIG", pluginConfig);

    // ---------- Enviar SET_CONFIG ----------
    Intent i = new Intent();
    i.setAction(DW_API_ACTION); // normalmente "com.symbol.datawedge.api.ACTION"
    i.putExtra(EXTRA_SET_CONFIG, profileConfig);

    // OJO: en samples suelen usar el string literal "SEND_RESULT"
    // Si tu constante EXTRA_SEND_RESULT ya funcionaba antes, mantenela,
    // pero si no ves resultados, probá con "SEND_RESULT".
    i.putExtra("SEND_RESULT", "true");

    i.putExtra(EXTRA_COMMAND_IDENTIFIER, "SET_CONFIG_" + profileName);

    ctx.sendBroadcast(i);
  }

  private void sendDwCommand(String extraKey, String extraValue, String cmdId) {
    Intent i = new Intent();
    i.setAction(DW_API_ACTION);
    i.putExtra(extraKey, extraValue);
    i.putExtra(EXTRA_SEND_RESULT, "true");
    i.putExtra(EXTRA_COMMAND_IDENTIFIER, cmdId);
    getContext().sendBroadcast(i);
  }

  private void resolveAvailability(AvailabilityRequest r) {
    // Cancelar el timeout ya que resolvemos exitosamente
    if (timeoutRunnable != null) {
      mainHandler.removeCallbacks(timeoutRunnable);
      timeoutRunnable = null;
    }

    boolean dwPresent = (r.dwStatusString != null);
    Boolean dwEnabled = null;

    if (r.dwStatusString != null) {
      if ("enabled".equalsIgnoreCase(r.dwStatusString) || "disabled".equalsIgnoreCase(r.dwStatusString)) {
        dwEnabled = "enabled".equalsIgnoreCase(r.dwStatusString);
      }
    }

    Boolean scannerPresent = null;
    if (r.scannerList != null) {
      scannerPresent = !r.scannerList.isEmpty();
    }

    JSObject out = new JSObject();

    JSObject dw = new JSObject();
    dw.put("present", dwPresent);
    dw.put("enabled", dwEnabled);
    dw.put("statusRaw", r.dwStatusString);
    out.put("datawedge", dw);

    JSObject sc = new JSObject();
    sc.put("present", scannerPresent);
    sc.put("status", r.scannerStatus);

    ArrayList<JSObject> scanners = new ArrayList<>();
    if (r.scannerList != null) {
      for (Bundle b : r.scannerList) {
        JSObject s = new JSObject();
        if (b.containsKey("SCANNER_NAME")) s.put("name", b.getString("SCANNER_NAME"));
        if (b.containsKey("SCANNER_CONNECTION_STATE")) s.put("connected", b.getBoolean("SCANNER_CONNECTION_STATE"));
        if (b.containsKey("SCANNER_INDEX")) s.put("index", b.getInt("SCANNER_INDEX"));
        if (b.containsKey("SCANNER_IDENTIFIER")) s.put("identifier", b.getString("SCANNER_IDENTIFIER"));
        scanners.add(s);
      }
    }
    sc.put("scanners", scanners);
    out.put("scanner", sc);

    JSObject raw = new JSObject();
    raw.put("dwStatus", r.dwStatusString);
    raw.put("scannerStatus", r.scannerStatus);
    out.put("raw", raw);

    r.call.resolve(out);
  }

  private void resolveAvailabilityWithTimeout(AvailabilityRequest r) {
    // Resolver con datos parciales + flag timedOut
    boolean dwPresent = (r.dwStatusString != null);
    Boolean dwEnabled = null;

    if (r.dwStatusString != null) {
      if ("enabled".equalsIgnoreCase(r.dwStatusString) || "disabled".equalsIgnoreCase(r.dwStatusString)) {
        dwEnabled = "enabled".equalsIgnoreCase(r.dwStatusString);
      }
    }

    Boolean scannerPresent = null;
    if (r.scannerList != null) {
      scannerPresent = !r.scannerList.isEmpty();
    }

    JSObject out = new JSObject();

    JSObject dw = new JSObject();
    dw.put("present", dwPresent);
    dw.put("enabled", dwEnabled);
    dw.put("statusRaw", r.dwStatusString);
    out.put("datawedge", dw);

    JSObject sc = new JSObject();
    sc.put("present", scannerPresent);
    sc.put("status", r.scannerStatus);

    ArrayList<JSObject> scanners = new ArrayList<>();
    if (r.scannerList != null) {
      for (Bundle b : r.scannerList) {
        JSObject s = new JSObject();
        if (b.containsKey("SCANNER_NAME")) s.put("name", b.getString("SCANNER_NAME"));
        if (b.containsKey("SCANNER_CONNECTION_STATE")) s.put("connected", b.getBoolean("SCANNER_CONNECTION_STATE"));
        if (b.containsKey("SCANNER_INDEX")) s.put("index", b.getInt("SCANNER_INDEX"));
        if (b.containsKey("SCANNER_IDENTIFIER")) s.put("identifier", b.getString("SCANNER_IDENTIFIER"));
        scanners.add(s);
      }
    }
    sc.put("scanners", scanners);
    out.put("scanner", sc);

    JSObject raw = new JSObject();
    raw.put("dwStatus", r.dwStatusString);
    raw.put("scannerStatus", r.scannerStatus);
    out.put("raw", raw);

    // Flag indicando que hubo timeout
    out.put("timedOut", true);

    r.call.resolve(out);
  }

  private JSObject bundleToJson(@Nullable Bundle b) {
    JSObject o = new JSObject();
    if (b == null) return o;

    for (String key : b.keySet()) {
      Object v = b.get(key);
      if (v == null) {
        o.put(key, JSObject.NULL);
      } else if (v instanceof Bundle) {
        o.put(key, bundleToJson((Bundle) v));
      } else if (v instanceof String) {
        o.put(key, v);
      } else if (v instanceof Boolean) {
        o.put(key, (Boolean) v);
      } else if (v instanceof Integer) {
        o.put(key, (Integer) v);
      } else {
        o.put(key, String.valueOf(v));
      }
    }
    return o;
  }
}
