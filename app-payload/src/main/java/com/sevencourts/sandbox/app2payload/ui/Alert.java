package com.sevencourts.sandbox.app2payload.ui;

import android.app.AlertDialog;
import android.content.Context;

public class Alert {

    /**
     * A simple static method that App 1 will call.
     * @param context The context from the calling application (App 1).
     * @param message The message from the calling application (App 1).
     */
    public static void show (Context context, String message) {
        new AlertDialog.Builder(context)
                .setTitle("Alert from App 2")
                .setMessage("Msg from App 1: " + message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }
}
