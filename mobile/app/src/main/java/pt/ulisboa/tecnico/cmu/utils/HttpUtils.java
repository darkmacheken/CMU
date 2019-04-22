package pt.ulisboa.tecnico.cmu.utils;

import android.content.Context;
import android.util.Log;
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
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import pt.ulisboa.tecnico.cmu.R;

public final class HttpUtils {

    private static final String TAG = "HttpUtils";

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private HttpUtils() {
    }

    public static OkHttpClient getHttpClient(Context context) {
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
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());

            // selfSocketFactory is deprecated, didnt find a way to get the new one
            return new OkHttpClient.Builder().sslSocketFactory(sslContext.getSocketFactory()).hostnameVerifier(
                (hostname, session) -> true).build();
        } catch (IOException | KeyStoreException | CertificateException | NoSuchAlgorithmException | KeyManagementException e) {
            Log.e(TAG, "Unable to sign in.", e);
            AlertUtils.alert("Unable to sign in.", context);
        }
        return null;
    }
}
