package pt.ulisboa.tecnico.cmu.utils;

import android.support.v4.util.Pair;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class GoogleDriveUtils {

    private static final Executor executor = Executors.newSingleThreadExecutor();
    public static String TYPE_AUDIO = "application/vnd.google-apps.audio";
    public static String TYPE_GOOGLE_DOCS = "application/vnd.google-apps.document";
    public static String TYPE_GOOGLE_DRAWING = "application/vnd.google-apps.drawing";
    public static String TYPE_GOOGLE_DRIVE_FILE = "application/vnd.google-apps.file";
    public static String TYPE_GOOGLE_FORMS = "application/vnd.google-apps.form";
    public static String TYPE_GOOGLE_FUSION_TABLES = "application/vnd.google-apps.fusiontable";
    public static String TYPE_GOOGLE_MY_MAPS = "application/vnd.google-apps.map";
    public static String TYPE_PHOTO = "application/vnd.google-apps.photo";
    public static String TYPE_GOOGLE_SLIDES = "application/vnd.google-apps.presentation";
    public static String TYPE_GOOGLE_APPS_SCRIPTS = "application/vnd.google-apps.script";
    public static String TYPE_GOOGLE_SITES = "application/vnd.google-apps.site";
    public static String TYPE_GOOGLE_SHEETS = "application/vnd.google-apps.spreadsheet";
    public static String TYPE_GOOGLE_FOLDER = "application/vnd.google-apps.folder";
    public static String TYPE_UNKNOWN = "application/vnd.google-apps.unknown";
    public static String TYPE_VIDEO = "application/vnd.google-apps.video";
    public static String TYPE_3_RD_PARTY_SHORTCUT = "application/vnd.google-apps.drive-sdk";
    private static Drive driveService;

    private GoogleDriveUtils() {
    }

    public static void setGoogleDriveService(Drive googleDriveService) {
        driveService = googleDriveService;
    }

    /**
     * Creates a folder in the app's folder with the provided name.
     *
     * @param name of the folder.
     * @return returns the id of the folder.
     */
    public static Task<String> createFolder(String name) {
        return Tasks.call(executor, () -> {
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
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */
    public static Task<String> createFile() {
        return Tasks.call(executor, () -> {
            File metadata = new File()
                .setParents(Collections.singletonList("root"))
                .setMimeType("text/plain")
                .setName("Untitled file");

            File googleFile = driveService.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });
    }

    /**
     * Returns a {@link FileList} containing all the visible files in the user's My Drive.
     *
     * <p>The returned list will only contain files visible to this app, i.e. those which were
     * created by this app. To perform operations on files not created by the app, the project must request Drive Full
     * Scope in the <a href="https://play.google.com/apps/publish">Google Developer's Console</a> and be submitted to
     * Google for verification.</p>
     */
    public static Task<FileList> queryFiles() {
        return Tasks.call(executor, () -> {
            //File execute = driveService.files().get("1GE0DL_-yAf-nfWRkw1Bu1JBXfKFuEEUg").execute();
            //FileList fileList = driveService.files().list().setSpaces("drive").execute();
            return null;
        });
    }

    /**
     * Opens the file identified by {@code fileId} and returns a {@link Pair} of its name and contents.
     */
    public Task<Pair<String, String>> readFile(String fileId) {
        return Tasks.call(executor, () -> {
            // Retrieve the metadata as a File object.
            File metadata = driveService.files().get(fileId).execute();
            String name = metadata.getName();

            // Stream the file contents to a String.
            try (InputStream is = driveService.files().get(fileId).executeMediaAsInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String contents = stringBuilder.toString();

                return Pair.create(name, contents);
            }
        });
    }

    /**
     * Updates the file identified by {@code fileId} with the given {@code name} and {@code content}.
     */
    public Task<Void> saveFile(String fileId, String name, String content) {
        return Tasks.call(executor, () -> {
            // Create a File containing any metadata changes.
            File metadata = new File().setName(name);

            // Convert content to an AbstractInputStreamContent instance.
            ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);

            // Update the metadata and contents.
            driveService.files().update(fileId, metadata, contentStream).execute();
            return null;
        });
    }

}
