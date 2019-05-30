package pt.ulisboa.tecnico.cmu.utils;

import android.app.AlertDialog;
import android.content.Context;

public final class AlertUtils {

    private AlertUtils() {
    }

    /**
     * Alerts user with a custom message
     *
     * @param message The message to be displayed
     * @param context Current app context
     */
    public static void alert(String message, Context context) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
            (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }
}
