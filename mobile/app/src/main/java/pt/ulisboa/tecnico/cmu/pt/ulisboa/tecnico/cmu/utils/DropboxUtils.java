package pt.ulisboa.tecnico.cmu.pt.ulisboa.tecnico.cmu.utils;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

public final class DropboxUtils {

    private DropboxUtils() {
    }

    /**
     * Creates Dropbox client and returns it.
     *
     * @param accessToken The client's Dropbox access token
     *
     * @return Dropbox client
     */
    public static DbxClientV2 getClient(String accessToken) {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/sample-app").build();
        return new DbxClientV2(config, accessToken);
    }
}
