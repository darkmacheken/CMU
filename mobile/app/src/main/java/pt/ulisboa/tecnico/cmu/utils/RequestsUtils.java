package pt.ulisboa.tecnico.cmu.utils;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Properties;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pt.ulisboa.tecnico.cmu.R;
import pt.ulisboa.tecnico.cmu.dataobjects.User;
import pt.ulisboa.tecnico.cmu.exceptions.UnauthorizedException;
import pt.ulisboa.tecnico.cmu.exceptions.UserNotFoundException;

public final class RequestsUtils {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String TAG = "RequestsUtils";
    private static final String LOGIN_ENDPOINT = "/login";
    private static final String REGISTER_ENDPOINT = "/register";
    private static final String ALBUMS_ENDPOINT = "/albums";
    private static final String USERS_ENDPOINT = "/users";
    private static final String ALBUMS_ADD_USER_ENDPOINT = "/addUser";
    private static OkHttpClient httpClient;
    private static String token;
    private static String userId;

    private RequestsUtils() {
    }

    /**
     * Tries to login the user into the server.
     *
     * @param context    the activity Context
     * @param userId     the Google's account id of the user.
     * @param oauthToken the oauth token generated by Google.
     * @return a session token if login is successful and null otherwise.
     * @throws UserNotFoundException if the user is not registered.
     */
    public static String login(Context context, String userId, String oauthToken)
        throws UserNotFoundException, IOException {
        OkHttpClient client = getHttpClient(context);

        if (client == null) {
            return null;
        }

        RequestBody body = RequestBody.create(RequestsUtils.JSON,
            "{\"userid\": \"" + userId + "\","
                + "\"oauthToken\" : \"" + oauthToken + "\"}");
        Request request = new Request.Builder()
            .url(context.getResources().getString(R.string.server_url) + LOGIN_ENDPOINT)
            .post(body)
            .build();

        Response response = client.newCall(request).execute();

        if (response.body() == null) {
            Log.e(TAG, "Response Body is Empty.");
            return null;
        }

        // User not found
        if (response.code() == 404) {
            throw new UserNotFoundException(response.body().string());
        }

        if (response.code() != 200) {
            Log.e(TAG, response.body().string());
            return null;
        }

        String jsonResponse = response.body().string();
        final Properties node = new Gson().fromJson(jsonResponse, Properties.class);

        String tokenLogin = node.getProperty("token");

        // Save token.
        if (tokenLogin != null) {
            token = tokenLogin;
            RequestsUtils.userId = userId;
            SharedPropertiesUtils.saveToken(context, userId, tokenLogin);
        } else {
            return "";
        }

        return token;
    }

    /**
     * Tries to register the user in the server.
     *
     * @param context the activity Context.
     * @param userId  the Google's account id of the user.
     * @param name    the name present in the Google's account.
     * @param email   the email of the Google's account.
     * @return true if successful and false otherwise.
     */
    public static boolean register(Context context, String userId, String name, String email, String accessToken) {
        OkHttpClient client = getHttpClient(context);

        if (client == null) {
            return false;
        }

        RequestBody body = RequestBody.create(RequestsUtils.JSON,
            "{\"userid\": \"" + userId + "\", "
                + "\"name\" : \"" + name + "\","
                + "\"email\" : \"" + email + "\","
                + "\"accessToken\" : \"" + accessToken + "\" }");
        Request request = new Request.Builder()
            .url(context.getResources().getString(R.string.server_url) + REGISTER_ENDPOINT)
            .post(body)
            .build();

        try {
            Response response = client.newCall(request).execute();

            if (response.body() == null) {
                Log.e(TAG, "Response Body is Empty.");
                return false;
            }

            if (response.code() != 200) {
                Log.e(TAG, response.body().string());
                return false;
            }
            return true;
        } catch (IOException e) {
            AlertUtils.alert("Unable to sign in.", context);
            Log.e(TAG, "Unable to POST request /register.", e);
        }
        return false;
    }

    /**
     * Requests the user's albums.
     *
     * @param context the activity context.
     * @return the string representing a list of albums in json.
     * @throws UnauthorizedException if the token is invalid.
     */
    public static String getAlbums(Context context) throws UnauthorizedException {
        OkHttpClient client = getHttpClient(context);

        if (client == null) {
            return null;
        }

        Request request = new Request.Builder().addHeader("Authorization", "Bearer " + token)
            .url(context.getResources().getString(R.string.server_url) + ALBUMS_ENDPOINT)
            .get()
            .build();

        try {
            Response response = client.newCall(request).execute();

            if (response.code() == 401 || response.code() == 403) {
                throw new UnauthorizedException();
            }

            if (response.body() == null) {
                Log.e(TAG, "Response Body is Empty.");
                return "[]";
            }

            if (response.code() == 200) {
                String albumsJson = response.body().string();
                SharedPropertiesUtils.saveAlbums(context, userId, albumsJson);
                return albumsJson;
            }
        } catch (IOException e) {
            Log.e(TAG, "Timeout to GET request /albums.", e);
            return SharedPropertiesUtils.getAlbums(context, userId);
        }

        return "[]";
    }

    /**
     * Creates an album.
     *
     * @param context the activity context.
     * @param name    the name of the album.
     * @param users   the users in the album.
     * @return true if created with success and false otherwise.
     * @throws UnauthorizedException if the token is invalid.
     */
    public static boolean createAlbum(Context context, String name, User[] users) throws UnauthorizedException {
        OkHttpClient client = getHttpClient(context);

        if (client == null) {
            return false;
        }

        String usersJson = new Gson().toJson(users);

        RequestBody body = RequestBody.create(RequestsUtils.JSON,
            "{\"name\": \"" + name + "\","
                + "\"users\":" + usersJson + "}");

        Request request = new Request.Builder().addHeader("Authorization", "Bearer " + token)
            .url(context.getResources().getString(R.string.server_url) + ALBUMS_ENDPOINT)
            .post(body)
            .build();

        try {
            Response response = client.newCall(request).execute();

            if (response.code() == 401 || response.code() == 403) {
                throw new UnauthorizedException();
            }

            if (response.body() == null) {
                Log.e(TAG, "Response Body is Empty.");
                return false;
            }

            if (response.code() == 200) {
                return true;
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to POST request /albums.", e);
        }
        return false;
    }

    /**
     * Requests all users (at maximum of 100) which name, id or email contains the sub-string in q.
     *
     * @param context the activity context.
     * @param userId  the user ID that requests.
     * @param q       the string to match.
     * @return an array of users.
     * @throws UnauthorizedException if the token is invalid.
     */
    public static User[] getUsers(Context context, String userId, String q) throws UnauthorizedException {
        OkHttpClient client = getHttpClient(context);

        if (client == null) {
            return new User[]{};
        }

        RequestBody body = RequestBody.create(RequestsUtils.JSON, "{\"q\": \"" + q + "\"}");

        Request request = new Request.Builder().addHeader("Authorization", "Bearer " + token)
            .url(context.getResources().getString(R.string.server_url) + USERS_ENDPOINT)
            .post(body)
            .build();

        try {
            Response response = client.newCall(request).execute();

            if (response.code() == 401 || response.code() == 403) {
                throw new UnauthorizedException();
            }

            if (response.body() == null) {
                Log.e(TAG, "Response Body is Empty.");
                return new User[]{};
            }

            if (response.code() == 200) {
                String jsonResponse = response.body().string();
                User[] users = new Gson().fromJson(jsonResponse, User[].class);
                SharedPropertiesUtils.saveUsers(context, users);
                return users;
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to GET request /users.", e);
            return SharedPropertiesUtils.getUsers(context, userId, q);
        }
        return new User[]{};
    }

    /**
     * Adds an user to the given album.
     *
     * @param context     the activity context.
     * @param albumId     the ID of the album.
     * @param userIdToAdd the ID of the user to add.
     * @throws UnauthorizedException if the token is invalid.
     * @throws IOException           if an error occur while requesting.
     */
    public static void addUserToAlbum(Context context, String albumId, String userIdToAdd)
        throws UnauthorizedException, IOException {
        OkHttpClient client = getHttpClient(context);

        if (client == null) {
            throw new IOException("Couldn't get http client.");
        }

        RequestBody body = RequestBody.create(RequestsUtils.JSON, "{\"id\": \"" + userIdToAdd + "\"}");

        Request request = new Request.Builder().addHeader("Authorization", "Bearer " + token)
            .url(context.getResources().getString(R.string.server_url) + ALBUMS_ENDPOINT + "/" + albumId
                + ALBUMS_ADD_USER_ENDPOINT)
            .post(body)
            .build();

        Response response = client.newCall(request).execute();

        if (response.code() == 401 || response.code() == 403) {
            throw new UnauthorizedException();
        }

        if (response.code() != 200) {
            throw new IOException("The request has not code status 200.");
        }
    }


    /**
     * Creates an HTTPS client that accepts the server's certificate in the resources. It accepts any domain.
     *
     * @param context the activity Context
     * @return the created client
     */
    private static OkHttpClient getHttpClient(Context context) {
        if (httpClient != null) {
            return httpClient;
        }
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType()); // "BKS"
            ks.load(null, null);

            // CertificateFactory
            // certificate
            Certificate ca;
            try (InputStream is = context.getResources().openRawResource(R.raw.certificate)) {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                ca = cf.generateCertificate(is);
            }

            ks.setCertificateEntry("av-ca", ca);

            // TrustManagerFactory
            String algorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
            // Create a TrustManager that trusts the CAs in our KeyStore
            tmf.init(ks);

            // Create a SSLContext with the certificate that uses tmf (TrustManager)
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());

            // selfSocketFactory is deprecated, didn't find a way to get the new one
            httpClient = new OkHttpClient.Builder().sslSocketFactory(sslContext.getSocketFactory()).hostnameVerifier(
                (hostname, session) -> true).build();

        } catch (IOException | KeyStoreException | CertificateException | NoSuchAlgorithmException | KeyManagementException e) {
            Log.e(TAG, "Unable to create HTTPS client.", e);
        }
        return httpClient;
    }

    /**
     * Returns the saved token of the given user ID.
     *
     * @param context the activity Context.
     * @param userId  the user ID.
     * @return the token if exists and null/empty string otherwise.
     */
    public static String getToken(Context context, String userId) {
        token = SharedPropertiesUtils.getToken(context, userId);
        RequestsUtils.userId = userId;

        return token;
    }

    public enum State {UNAUTHORIZED_REQUEST, SUCCESS, NOT_SUCCESS}
}
