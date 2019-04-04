package pt.ulisboa.tecnico.cmu.pt.ulisboa.tecnico.cmu.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public final class AlertUtils {

    private AlertUtils() {}

    public static void alert(String message, Context context) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
