package pt.ulisboa.tecnico.cmu.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class SharedPropertiesUtils {

    private SharedPropertiesUtils() {
    }


    public static String getToken(Context context, String userId) {
        SharedPreferences sp = context.getSharedPreferences("Login", Context.MODE_PRIVATE);
        return sp.getString("token_" + userId, "");
    }

    public static void saveToken(Context context, String userId, String tokenLogin) {
        SharedPreferences sp = context.getSharedPreferences("Login", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString("token_" + userId, tokenLogin);
        ed.apply();
    }

    public static String getAlbums(Context context, String userId) {
        SharedPreferences sp = context.getSharedPreferences("Albums", Context.MODE_PRIVATE);
        return sp.getString("albums_" + userId, "[]");
    }

    public static void saveAlbums(Context context, String userId, String albumsJson) {
        SharedPreferences sp = context.getSharedPreferences("Albums", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString("albums_" + userId, albumsJson);
        ed.apply();
    }
}
