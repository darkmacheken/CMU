package pt.ulisboa.tecnico.cmu.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class SharedPropertiesUtils {

    private SharedPropertiesUtils() {
    }


    public static String getToken(Context context) {
        SharedPreferences sp = context.getSharedPreferences("Login", Context.MODE_PRIVATE);
        return sp.getString("token", "");
    }

    public static void saveToken(Context context, String tokenLogin) {
        SharedPreferences sp = context.getSharedPreferences("Login", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString("token", tokenLogin);
        ed.apply();
    }

    public static String getAlbums(Context context) {
        SharedPreferences sp = context.getSharedPreferences("Albums", Context.MODE_PRIVATE);
        return sp.getString("albums", "[]");
    }

    public static void saveAlbums(Context context, String albumsJson) {
        SharedPreferences sp = context.getSharedPreferences("Albums", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString("albums", albumsJson);
        ed.apply();
    }

}
