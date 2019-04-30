package pt.ulisboa.tecnico.cmu.utils;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Builder;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.GeneratedIds;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.apache.commons.io.FileUtils;
import pt.ulisboa.tecnico.cmu.exceptions.DriveServiceNullException;

public final class GoogleDriveUtils {

    private static final String TAG = "GoogleDriveUtils";
    private static final Executor executor = Executors.newSingleThreadExecutor();

    private static final String TYPE_GOOGLE_FOLDER = "application/vnd.google-apps.folder";

    private static Drive driveService;

    private GoogleDriveUtils() {
    }

    public static void setGoogleDriveService(Drive googleDriveService) {
        driveService = googleDriveService;
    }

    /**
     * Tries to connect to the google account
     *
     * @param context the context of the activity.
     */
    public static void connectDriveService(Context context) {
        if (driveService == null) {
            GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(context);

            GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                    context, Arrays.asList(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA));
            credential.setSelectedAccount(googleAccount.getAccount());

            driveService = new Builder(
                AndroidHttp.newCompatibleTransport(),
                new GsonFactory(),
                credential)
                .setApplicationName("P2Photo")
                .build();
        }
    }

    /**
     * Creates a folder in the app's folder with the provided name.
     *
     * @param name of the folder.
     * @return the id of the folder.
     */
    public static Task<String> createFolder(String name) {
        return Tasks.call(executor, () -> {
            checkDriveService();

            File metadata = new File()
                .setMimeType(TYPE_GOOGLE_FOLDER)
                .setName(name);

            File googleFile = driveService.files().create(metadata)
                .setFields("id")
                .execute();

            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });
    }

    /**
     * Uploads a file in the user's My Drive folder and returns its file ID.
     *
     * @param filePath     the file to upload
     * @param parentFolder the id of the parent folder where the image will be
     * @return the id of the created file
     */
    public static Task<String> createFile(java.io.File filePath, String parentFolder) {
        return Tasks.call(executor, () -> {
            checkDriveService();

            GeneratedIds ids = driveService.files().generateIds().setCount(1).execute();
            if (ids == null) {
                throw new IOException("Could not generate id.");
            }

            File metadata = new File()
                .setParents(Collections.singletonList(parentFolder))
                .setName(ids.getIds().get(0));

            File googleFile = driveService.files().create(metadata, new FileContent(null, filePath)).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });
    }

    /**
     * Updates the content of a file.
     *
     * @param fileId  the id of the file to update.
     * @param content the new content
     * @return the id of the file updated.
     */
    public static Task<String> updateFile(String fileId, String content) {
        return Tasks.call(executor, () -> {
            checkDriveService();

            File googleFile = driveService.files().update(fileId, null,
                ByteArrayContent.fromString("application/json", content)).execute();

            if (googleFile == null) {
                throw new IOException("Null result when requesting file update.");
            }

            return googleFile.getId();
        });
    }

    /**
     * Opens the file identified by {@code fileId} and returns its content in a string.
     *
     * @param fileId the if of the file to read.
     * @return the content encoded as a utf8 string.
     */
    public static Task<String> readFile(String fileId) {
        return Tasks.call(executor, () -> {
            checkDriveService();

            // Stream the file contents to a String.
            try (InputStream is = driveService.files().get(fileId).executeMediaAsInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                return stringBuilder.toString();
            }
        });
    }

    /**
     * Downloads the file identified by {@code fileId} to the {@code folderPath} with the name of the file's id.
     *
     * @param fileId     the id of the file.
     * @param folderPath the path where the image will be downloaded.
     * @return the file pointer to the new downloaded file.
     */
    public static Task<java.io.File> downloadFile(String fileId, java.io.File folderPath) {
        return Tasks.call(executor, () -> {
            checkDriveService();

            java.io.File file = new java.io.File(folderPath, fileId);
            // Stream the file contents to a File.
            try (InputStream is = driveService.files().get(fileId).executeMediaAsInputStream()) {
                FileUtils.copyInputStreamToFile(is, file);
                return file;
            }
        });
    }

    /**
     * Copies the content from the src to the dst.
     *
     * @param src source file
     * @param dst destination file
     */
    public static void copy(java.io.File src, java.io.File dst) {
        try {
            FileInputStream inStream = new FileInputStream(src);
            FileOutputStream outStream = new FileOutputStream(dst);
            FileChannel inChannel = inStream.getChannel();
            FileChannel outChannel = outStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            inStream.close();
            outStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Unable update metadata file.", e);
        }
    }

    private static void checkDriveService() throws DriveServiceNullException {
        if (driveService == null) {
            throw new DriveServiceNullException("Drive service is not initialized.");
        }
    }
}
