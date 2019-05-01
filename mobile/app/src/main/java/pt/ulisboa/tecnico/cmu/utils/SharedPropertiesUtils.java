package pt.ulisboa.tecnico.cmu.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import pt.ulisboa.tecnico.cmu.dataobjects.User;

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

    public static void saveLastLoginId(Context context, String userId) {
        SharedPreferences sp = context.getSharedPreferences("Login", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString("lastLogin", userId);
        ed.apply();
    }

    public static String getLastLoginId(Context context) {
        SharedPreferences sp = context.getSharedPreferences("Login", Context.MODE_PRIVATE);
        return sp.getString("lastLogin", null);
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

    public static String getAlbumUserMetadata(Context context, String userId, String albumId) {
        SharedPreferences sp = context.getSharedPreferences("Albums", Context.MODE_PRIVATE);
        return sp.getString("album_" + albumId + userId, "[]");
    }

    public static void saveAlbumUserMetadata(Context context, String userId, String albumId, String metadata) {
        SharedPreferences sp = context.getSharedPreferences("Albums", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString("album_" + albumId + userId, metadata);
        ed.apply();
    }

    public static String getAlbumMetadata(Context context, String albumId) {
        SharedPreferences sp = context.getSharedPreferences("Albums", Context.MODE_PRIVATE);
        return sp.getString("album_" + albumId, "[]");
    }

    public static void saveAlbumMetadata(Context context, String albumId, String metadata) {
        SharedPreferences sp = context.getSharedPreferences("Albums", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString("album_" + albumId, metadata);
        ed.apply();
    }

    public static User[] getUsers(Context context, String userId, String q) {
        SharedPreferences sp = context.getSharedPreferences("Users", Context.MODE_PRIVATE);
        User[] savedUsers = new Gson().fromJson(sp.getString("users", "[]"), User[].class);

        if (userId == null) {
            return savedUsers;
        } else if (TextUtils.isEmpty(q)) {
            List<User> list = new ArrayList<>();
            for (User user : savedUsers) {
                if (!user.getId().equals(userId)) {
                    list.add(user);
                }
            }
            return list.toArray(new User[0]);
        } else {
            List<User> list = new ArrayList<>();
            for (User user : savedUsers) {
                if (!user.getId().equals(userId) && (user.getId().contains(q)
                    || user.getName().contains(q) || user.getEmail().contains(q))) {
                    list.add(user);
                }
            }
            return list.toArray(new User[0]);
        }
    }

    public static void saveUsers(Context context, User[] users) {
        List<User> savedUsersList = new ArrayList<>(Arrays.asList(getUsers(context, null, "")));
        Set<String> usersSet = new HashSet<>();

        for (User user : savedUsersList) {
            String id = user.getId();
            usersSet.add(id);
        }

        List<User> list = new ArrayList<>();
        for (User user : users) {
            if (!usersSet.contains(user.getId())) {
                list.add(user);
            }
        }
        savedUsersList.addAll(list);

        String usersJson = new Gson().toJson(savedUsersList);

        SharedPreferences sp = context.getSharedPreferences("Users", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString("users", usersJson);
        ed.apply();
    }
}
