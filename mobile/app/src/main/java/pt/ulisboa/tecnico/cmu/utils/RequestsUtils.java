package pt.ulisboa.tecnico.cmu.utils;

import android.content.Context;
import android.util.Log;
import com.google.api.client.util.Base64;
import com.google.gson.Gson;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
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

    public static boolean createAlbumWifiDirect(Context context, String name, User[] users)
        throws UnauthorizedException {
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

    public static String getUserId() {
        return userId;
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

    public static Key getPrivateKey() {
        try {
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.decodeBase64("MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDmfqQf+/N+DKXHBNcR+xsbzrrPP8c9goA4R3MnD3gLDp5GUZGNnPTYAPAsWg1+dIaTYU3DGK05nYrd8avKRfWCCypiVQUp4Za+D05GOpmCCVNrMJxJh73AErX/7UojA2ZH4TJXLBCEcBR5Z4tFbQBZZLxEHvsroQ7Opys0NiXyTuzzNyIGSvxrRO700dyvCQpgR2B9A7GquuEWUT6xPY3M5vxmIFlWju/ab/lKQPWHUsgZlrBSQvsDd8ddwN5E5HL2qRhwN3tgV2x1GFQgBNAvv9EUBg5vCur7kTymsSAGjHIe7P+5HFAZxCb1ZQ2fgP9OEdB+ekaqgXfNiKU/Wy53AgMBAAECggEATHTXplBh3X7+gnzFho5f2KKDbvm98lZWh9STivJjpG9N6w9lk67rvLba9CtO7JJkjYCqVbvawhDTHnnqvSbloCRqA8Il+1V8NkFHep43i13ikNzICs//DjZmrqUcgW7AP7mghC/2rqeq8vZ4ySe2BPEYThRkxn0fN0dWWnRXs/7+7IWGwc9rabZ0ZUqsnJE/63lodym5GY84wDo52I7TTTCOifpZmIPKDhJXEbWW8NooXV0cpsKlZEY4v8NgfNMIPgiVAVBNIOSegwO6fBpunW9Ab3yM6bEpU55RgHcI4f/gfMVFpPLoMPpVzybhAIefQRiA7gKMu3P8sLdH+8lpAQKBgQD8vm/dqDhdf9Yiac8MvVUOoRet0EcTRp9uoSqLB7C4wrt6Rp2vBuYMHZmT+JV/LtcGpd9xOFRNG8crsvKlHwm0o/4w2qM6mif2MRhZ6GxIuaPSLRN2eBD7X4MyiVQ4lIl1RctAVudEEN8wlzQ/jx5X1AFiX1aPcFM806QJUENAgQKBgQDpdtM3a+5cELqrwuxGJkXoHo6ODmS+GcXa3umPQNRTjMSrytR6iYjaG0DckBxK/iLhDYd7iZ/3XXeHeK9j+Z4PgaH0UMGS6/dMEIMZG5figPTply071nMuo0ETWYVBr1smPsYf9+XsBFe1/oOEq7RS5ILMj0cC+5IN76jxfH7y9wKBgQC6E5LUhFcLL2T97RyM6o/Gt39xblgFrwcOMgXaWg0X2fahLYBGLjQMU3aQZIHcIyYYNOLuvmQCaSMX3yWZv+IrZllsqmtmZ7xoGvksqFugp1wfDyS3IeqOx2EWQdkJ1wHknz/m3JRjnnBTm97RtJLIYsOqIzrdW/tMWxz35mm9AQKBgQDJB3h4kId+3yjeHco13V70sNsvl1VIHAkynh+fKsOp7dyr0MuFeEhPBoijY7P5HzwJbgzrY2ZLKkBydokQHTDtSUKbja4hRO58oPtB83ClqUU6nuJkVBR6ZDj04HDOTqC+He+cN2nUASlFnRLCetebSQkX+4e6GcV6GpPu3LSzoQKBgQCEchb/koer5RM1BlGVrzQZCvQ14bdKCfo64BcKZl+RFaLQef6Mcv1LYrNtg/g3ao5llxZYS+gk2+rtKz9B7LgNVBMIbVGEFlPrF1l2tKFV33moi0pm0hqlyJwvVkVBHnl8SjEprTD8VwylTNAxIRkuo4pCNbgE3w2CYNODnRkCaw=="));
            KeyFactory rsaFact = KeyFactory.getInstance("RSA");
            return rsaFact.generatePrivate(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static Key getPublicKey(String userId) {
        try {
            byte[] encoded = Base64.decodeBase64("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5n6kH/vzfgylxwTXEfsbG866zz/HPYKAOEdzJw94Cw6eRlGRjZz02ADwLFoNfnSGk2FNwxitOZ2K3fGrykX1ggsqYlUFKeGWvg9ORjqZgglTazCcSYe9wBK1/+1KIwNmR+EyVywQhHAUeWeLRW0AWWS8RB77K6EOzqcrNDYl8k7s8zciBkr8a0Tu9NHcrwkKYEdgfQOxqrrhFlE+sT2NzOb8ZiBZVo7v2m/5SkD1h1LIGZawUkL7A3fHXcDeRORy9qkYcDd7YFdsdRhUIATQL7/RFAYObwrq+5E8prEgBoxyHuz/uRxQGcQm9WUNn4D/ThHQfnpGqoF3zYilP1sudwIDAQAB");
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(new X509EncodedKeySpec(encoded));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
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
