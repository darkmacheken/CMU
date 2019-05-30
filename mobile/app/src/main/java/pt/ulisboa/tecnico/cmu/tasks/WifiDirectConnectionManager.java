package pt.ulisboa.tecnico.cmu.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.api.client.util.Base64;
import io.opencensus.internal.StringUtils;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmu.adapters.ViewAlbumAdapter;
import pt.ulisboa.tecnico.cmu.dataobjects.Album;
import pt.ulisboa.tecnico.cmu.dataobjects.Catalog;

public class WifiDirectConnectionManager {

    private static final String TAG = "WifiDirectConnectionM";
    private static final int PORT = 10001;
    private static final String SEND_CATALOG_MSG = "sendCatalog";
    private static final String SEND_PHOTO_MSG = "sendPhoto";
    private static final String ASK_FOR_PHOTO_MSG = "askForPhoto";
    public static List<SimWifiP2pDevice> peers = new ArrayList<>();
    public static List<SimWifiP2pDevice> networkPeers = new ArrayList<>();
    public static SimWifiP2pDevice thisDevice;
    private static List<Catalog> peersCatalogs = new ArrayList<>();

    private WifiDirectConnectionManager() {
    }

    public static void init() {
        new IncommingCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void broadcastCatalogs(Context context) {
        Log.d(TAG, "Broadcasting all catalogs");
        for (SimWifiP2pDevice device : WifiDirectConnectionManager.networkPeers) {
            for (Catalog catalog : getAllMyCatalogs(context)) {
                new SendCatalogTask(device.getVirtIp(), catalog).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    public static void broadcastAlbumCatalog(Context context, Album album) {
        Log.d(TAG, "Broadcasting catalog for album " + album.getName());
        Catalog albumCatalog = getAlbumCatalog(context, album);

        for (SimWifiP2pDevice device : WifiDirectConnectionManager.networkPeers) {
            new SendCatalogTask(device.getVirtIp(), albumCatalog).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private static Catalog getAlbumCatalog(Context context, Album album) {
        List<Catalog> allMyCatalogs = getAllMyCatalogs(context);
        for (Catalog catalog : allMyCatalogs) {
            if (TextUtils.equals(catalog.getAlbumName(), album.getName())) {
                return catalog;
            }
        }
        return null;
    }

    public static void getAlbumPhotos(Album album, Context context, ViewAlbumAdapter viewAlbumAdapter,
        LinearLayoutManager layoutManager) {
        Log.d(TAG, context.getCacheDir().getAbsolutePath());
        Log.d(TAG, "Getting photos for album " + album.getName());

        //Get my photos
        getMyPhotosForAlbum(album, context, viewAlbumAdapter, layoutManager);
        List<String> deviceIds = new ArrayList<>();
        for (SimWifiP2pDevice device : networkPeers) {
            deviceIds.add(device.getVirtIp());
        }

        //Get other users' photos
        AtomicInteger numDownloads = new AtomicInteger();
        for (Catalog catalog : peersCatalogs) {
            if (catalog.getAlbumName().equals(album.getName())) {
                if (deviceIds.contains(catalog.getTargetVirtIp())) {
                    for (String catalogLine : catalog.getCatalogLineList()) {
                        AskForPhotoTask askForPhotoTask = (AskForPhotoTask) new AskForPhotoTask(catalog.getTargetVirtIp(),
                            catalogLine, new File(context.getCacheDir(), album.getId()),
                            generateFileId(catalog.getUserName(), catalogLine))
                            .execute();
                        try {
                            File fileResult = askForPhotoTask.get();
                            numDownloads.getAndIncrement();
                            Log.d(TAG, "Downloaded " + numDownloads + " photos...");
                            viewAlbumAdapter.addPhoto(fileResult.getAbsolutePath());
                            layoutManager.scrollToPositionWithOffset(0, 0);
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    for (String catalogLine : catalog.getCatalogLineList()) {
                        File folderPath = new File(context.getCacheDir(), album.getId());
                        String fileId = generateFileId(catalog.getUserName(), catalogLine);

                        viewAlbumAdapter.addPhoto((new java.io.File(folderPath, fileId)).getAbsolutePath());
                        layoutManager.scrollToPositionWithOffset(0, 0);
                    }
                }
            }
        }
    }

    public static File getCatalogFolder() {
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "p2photo_catalogs");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
            if (success) {
                Log.d(TAG, "Catalog folder created successfully.");
            } else {
                Log.d(TAG, "Failed to create catalog folder!");
            }
        }
        return folder;
    }

    public static void createCatalog(Album album, File folder, Context context) {
        String fileName = GoogleSignIn.getLastSignedInAccount(context).getDisplayName() + "_" + album.getName();
        File catalog = new File(folder, fileName);
        boolean success = true;
        if (!catalog.exists()) {
            try {
                success = catalog.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (success) {
                Log.d(TAG, "Catalog file created successfully - " + catalog.getAbsolutePath() + ".");
            } else {
                Log.d(TAG, "Failed to create catalog file - " + catalog.getAbsolutePath() + "!");
            }
        }
    }

    public static void writeToCatalog(Album album, String photoUri, Context context) {
        String fileName = GoogleSignIn.getLastSignedInAccount(context).getDisplayName() + "_" + album.getName();
        File catalog = new File(getCatalogFolder(), fileName);

        try {
            FileWriter writer = new FileWriter(catalog, true);
            writer.append(photoUri + "\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Line " + photoUri + " added to catalog " + catalog.getAbsolutePath());
    }

    private static List<Catalog> getAllMyCatalogs(Context context) {
        List<Catalog> catalogs = new ArrayList<>();
        String userName = GoogleSignIn.getLastSignedInAccount(context).getDisplayName();

        File catalogFolder = getCatalogFolder();
        File[] catalogFileList = catalogFolder.listFiles();

        for (File catalogFile : catalogFileList) {
            Log.d(TAG, "Found catalog: " + catalogFile.getName());
            String[] splitFileName = catalogFile.getName().split("_");
            String albumName = splitFileName[splitFileName.length - 1];
            List<String> catalogLineList = new ArrayList<>();
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(catalogFile));
                String catalogLine;
                Log.d(TAG, "Reading catalog lines...");
                while ((catalogLine = bufferedReader.readLine()) != null) {
                    if (!catalogLine.equals("")) {
                        catalogLineList.add(catalogLine);
                        Log.d(TAG, catalogLine);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            catalogs.add(new Catalog("", userName, albumName, catalogLineList));
        }
        return catalogs;
    }

    private static void getMyPhotosForAlbum(Album album, Context context, ViewAlbumAdapter viewAlbumAdapter,
        LinearLayoutManager layoutManager) {
        List<Catalog> allMyCatalogs = getAllMyCatalogs(context);
        for (Catalog catalog : allMyCatalogs) {
            if (TextUtils.equals(catalog.getAlbumName(), album.getName())) {
                List<String> photoUriStrings = catalog.getCatalogLineList();

                for (String photoUriString : photoUriStrings) {
                    Log.d(TAG, "Getting photo " + photoUriString);
                    viewAlbumAdapter.addPhoto(photoUriString);
                    layoutManager.scrollToPositionWithOffset(0, 0);
                }
            }
        }
    }

    public static String generateFileId(String userId, String catalogLine) {
        return userId + "_" + FilenameUtils.getName(catalogLine);
    }

    private static void sendPhoto(OutputStream outputStream, String photoUriString) {
        Log.d(TAG, "Sending photo: " + photoUriString);
        try {
            File file = new File(photoUriString);
            byte[] bytes = FileUtils.readFileToByteArray(file);
            outputStream.write((Base64.encodeBase64String(bytes) + "\n").getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Sent photo " + photoUriString);
    }

    private static class SendCatalogTask extends AsyncTask<Void, String, Void> {

        private final Catalog catalog;
        private final String targetDeviceVirtIp;

        public SendCatalogTask(String targetDeviceVirtIp, Catalog catalog) {
            this.targetDeviceVirtIp = targetDeviceVirtIp;
            this.catalog = catalog;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                SimWifiP2pSocket clientSocket = new SimWifiP2pSocket(targetDeviceVirtIp, PORT);
                clientSocket.getOutputStream().write(
                    (SEND_CATALOG_MSG + "\n" + thisDevice.getVirtIp() + "\n" + catalog.getUserName() + "\n" +
                        catalog.getAlbumName() + "\n" + catalog.getContentString()).getBytes());
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.d(TAG, "Sent catalog for album " + catalog.getAlbumName());
        }
    }

    private static class AskForPhotoTask extends AsyncTask<Void, String, File> {

        private final String targetDeviceVirtIp;
        private final String photoUriString;
        private final File folderPath;
        private final String fileId;

        public AskForPhotoTask(String targetDeviceVirtIp, String photoUriString, File folderPath, String fileId) {
            this.targetDeviceVirtIp = targetDeviceVirtIp;
            this.photoUriString = photoUriString;
            this.folderPath = folderPath;
            this.fileId = fileId;
        }

        @Override
        protected File doInBackground(Void... voids) {
            File file = null;
            try {
                Log.d(TAG, "Asking for photo: " + photoUriString);
                SimWifiP2pSocket clientSocket = new SimWifiP2pSocket(targetDeviceVirtIp, PORT);
                clientSocket.getOutputStream().write(
                    (ASK_FOR_PHOTO_MSG + "\n" + photoUriString + "\n").getBytes());

                Log.d(TAG, "Downloading photo to " + folderPath.getAbsolutePath() + fileId);

                InputStream is = clientSocket.getInputStream();

                BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(is));
                String encodedPhoto = bufferedReader.readLine();
                byte[] photoBytes = Base64.decodeBase64(encodedPhoto);

                file = new java.io.File(folderPath, fileId);

                FileUtils.writeByteArrayToFile(file, photoBytes);

                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return file;
        }

        @Override
        protected void onPostExecute(File result) {
            Log.d(TAG, "Asked for photo " + photoUriString + " to device " + targetDeviceVirtIp);
        }
    }

    private static class IncommingCommTask extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            SimWifiP2pSocketServer serverSocket = null;
            try {
                serverSocket = new SimWifiP2pSocketServer(PORT);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            Log.d(TAG, "ServerSocket listening on port " + PORT);

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SimWifiP2pSocket socket = serverSocket.accept();
                    Log.d(TAG, "Socket connection accepted");
                    try {
                        BufferedReader sockIn = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
                        String messageCode = sockIn.readLine();
                        Log.d(TAG, "Received " + messageCode);

                        if (messageCode.equals(SEND_CATALOG_MSG)) {
                            String targetVirtIp = sockIn.readLine();
                            String userId = sockIn.readLine();
                            String albumId = sockIn.readLine();
                            List<String> catalogLineList = new ArrayList<>();
                            String catalogLine;
                            while ((catalogLine = sockIn.readLine()) != null) {
                                if (!catalogLine.equals("")) {
                                    catalogLineList.add(catalogLine);
                                }
                            }
                            Catalog catalog = new Catalog(targetVirtIp, userId, albumId, catalogLineList);
                            Log.d(TAG, "Received catalog:\n" + catalog.toString());
                            peersCatalogs.remove(catalog);
                            peersCatalogs.add(new Catalog(targetVirtIp, userId, albumId, catalogLineList));

                        } else if (messageCode.equals(ASK_FOR_PHOTO_MSG)) {
                            String photoUriString = sockIn.readLine();
                            Log.d(TAG, "Received askForPhoto: " + photoUriString);
                            OutputStream outputStream = socket.getOutputStream();
                            sendPhoto(outputStream, photoUriString);
                        }

                        socket.getOutputStream().write(("\n").getBytes());
                    } catch (IOException e) {
                        Log.d("Error reading socket:", e.getMessage());
                    } finally {
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }

            return null;

        }
    }
}
