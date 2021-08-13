package com.ramymokako.plugin.ussd.android;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.ramymokako.plugin.ussd.android.USSDController;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

public class VoIpUSSD extends CordovaPlugin {
    private static final int REQUEST_PERMISSION_REQ_CODE = 1;
    private static final int SEND_SMS_REQ_CODE = 0;
    public static String TAG = "USSDService";
    public final String ACTION_HAS_PERMISSION = "has_permission";
    public final String ACTION_REQUEST_PERMISSION = "request_permission";
    public final String ACTION_SEND_SMS = "show";
    private JSONArray _args;
    CallbackContext callbackContext;
    private Context context;
    private HashMap<String, HashSet<String>> map;
    public String[] res = {""};
    private String result;
    private USSDApi ussdApi;

    public boolean execute(String str, JSONArray jSONArray, CallbackContext callbackContext2) throws JSONException {
        HashMap<String, HashSet<String>> hashMap = new HashMap<>();
        this.map = hashMap;
        hashMap.put("KEY_LOGIN", new HashSet(Arrays.asList(new String[]{"espere", "waiting", "loading", "esperando"})));
        this.map.put("KEY_ERROR", new HashSet(Arrays.asList(new String[]{"problema", "problem", "error", "null"})));
        Activity activity = this.cordova.getActivity();
        this.context = activity;
        this.callbackContext = callbackContext2;
        this._args = jSONArray;
        this.ussdApi = USSDController.getInstance(activity);
        this.result = "";
        if (str.equals("show")) {
            try {
                String string = jSONArray.getJSONObject(0).getString("ussdCode");
                if (hasPermission()) {
                    executeSimpleUssd(string, callbackContext2);
                    new PluginResult(PluginResult.Status.NO_RESULT).setKeepCallback(true);
                    return true;
                }
                requestPermission(0);
                return false;
            } catch (JSONException e) {
                callbackContext2.error("Error encountered: " + e.getMessage());
                return false;
            }
        } else if (str.equals("has_permission")) {
            callbackContext2.sendPluginResult(new PluginResult(PluginResult.Status.OK, hasPermission()));
            return false;
        } else {
            if (str.equals("request_permission")) {
                requestPermission(1);
            }
            return false;
        }
    }

    private void executeSimpleUssd(String str, CallbackContext callbackContext2) {
        final int[] iArr = {0};
        String[] split = str.split("-");
        int parseInt = Integer.parseInt(split[0]);
        final String[] split2 = split[1].split(",");
        String str2 = split2[0];
        final PluginResult[] pluginResultArr = new PluginResult[split2.length];
        String str3 = TAG;
        Log.d(str3, "PROCESSING " + str2);
        final CallbackContext callbackContext3 = callbackContext2;
        this.ussdApi.callUSSDInvoke(str2, parseInt, this.map, new USSDController.CallbackInvoke() {
            public void responseInvoke(String str) {
                Log.d(VoIpUSSD.TAG, "callUSSDInvoke/responseInvoke()");
                VoIpUSSD.this.res[0] = str;
                pluginResultArr[0] = new PluginResult(PluginResult.Status.OK, VoIpUSSD.this.res[0]);
                pluginResultArr[0].setKeepCallback(true);
                callbackContext3.sendPluginResult(pluginResultArr[0]);
                VoIpUSSD voIpUSSD = VoIpUSSD.this;
                voIpUSSD.doUSSDmenuOptions(split2, iArr, voIpUSSD.res, pluginResultArr);
            }

            public void over(String str) {
                Log.d(VoIpUSSD.TAG, "callUSSDInvoke/over()");
                String[] strArr = VoIpUSSD.this.res;
                strArr[0] = str + "DONEENOD.";
                pluginResultArr[0] = new PluginResult(PluginResult.Status.OK, VoIpUSSD.this.res[0]);
                pluginResultArr[0].setKeepCallback(true);
                callbackContext3.sendPluginResult(pluginResultArr[0]);
            }
        });
    }

    public void doUSSDmenuOptions(String[] strArr, int[] iArr, String[] strArr2, PluginResult[] pluginResultArr) {
        iArr[0] = iArr[0] + 1;
        if (iArr[0] < strArr.length) {
            String str = TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("COMMAND ");
            sb.append(iArr[0]);
            sb.append("/");
            sb.append(strArr.length - 1);
            sb.append(" - ");
            sb.append(strArr[iArr[0]]);
            Log.d(str, sb.toString());
            final int[] iArr2 = iArr;
            final String[] strArr3 = strArr;
            final String[] strArr4 = strArr2;
            final PluginResult[] pluginResultArr2 = pluginResultArr;
            this.ussdApi.send(strArr[iArr[0]], new USSDController.CallbackMessage() {
                public void responseMessage(String str) {
                    if (iArr2[0] == strArr3.length - 1) {
                        str = str + "DONEENOD";
                    }
                    strArr4[0] = str;
                    pluginResultArr2[iArr2[0]] = new PluginResult(PluginResult.Status.OK, strArr4[0]);
                    pluginResultArr2[iArr2[0]].setKeepCallback(true);
                    VoIpUSSD.this.callbackContext.sendPluginResult(pluginResultArr2[iArr2[0]]);
                    VoIpUSSD voIpUSSD = VoIpUSSD.this;
                    voIpUSSD.doUSSDmenuOptions(strArr3, iArr2, voIpUSSD.res, pluginResultArr2);
                }
            });
        }
    }

    private boolean hasPermission() {
        return this.cordova.hasPermission("android.permission.CALL_PHONE") && this.cordova.hasPermission("android.permission.READ_PHONE_STATE");
    }

    private void requestPermission(int i) {
        this.cordova.requestPermissions(this, i, new String[]{"android.permission.READ_PHONE_STATE", "android.permission.CALL_PHONE"});
    }

    public void onRequestPermissionResult(int i, String[] strArr, int[] iArr) throws JSONException {
        for (int i2 : iArr) {
            if (i2 == -1) {
                this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "User has denied permission"));
                return;
            }
        }
        if (i == 0) {
            try {
                executeSimpleUssd(this._args.getJSONObject(0).getString("ussdCode"), this.callbackContext);
                new PluginResult(PluginResult.Status.NO_RESULT).setKeepCallback(true);
            } catch (JSONException e) {
                this.callbackContext.error("Error encountered: " + e.getMessage());
            }
        } else {
            this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
        }
    }
}
