package pt.ulisboa.tecnico.cmu.tasks;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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

    public WifiDirectConnectionManager() {
        init();
    }

    private void init() {
        new IncommingCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void broadcastCatalogs(Context context) {
        for (SimWifiP2pDevice device : WifiDirectConnectionManager.networkPeers) {
            for (Catalog catalog : getAllMyCatalogs(context)) {
                new SendCatalogTask(device.getVirtIp(), catalog).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    public static void getAlbumPhotos(Album album, Context context, ViewAlbumAdapter viewAlbumAdapter) {
        //Get my photos
        getMyPhotosForAlbum(album, context, viewAlbumAdapter);

        //Get other users' photos
        AtomicInteger numDownloads = new AtomicInteger();
        for (Catalog catalog : peersCatalogs) {
            if (catalog.getAlbumName().equals(album.getName())) {
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
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
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
                Log.d(TAG, "Catalog file created successfully - " + fileName + ".");
            } else {
                Log.d(TAG, "Failed to create catalog file - " + fileName + "!");
            }
        }
    }

    public static void writeToCatalog(Album album, String photoUri, Context context) {
        String fileName = GoogleSignIn.getLastSignedInAccount(context).getDisplayName() + "_" + album.getName();
        File catalog = new File(getCatalogFolder(), fileName);
        try {
            FileWriter writer = new FileWriter(catalog);
            writer.append(photoUri);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Line " + photoUri + " added to catalog " + fileName);
    }

    private static List<Catalog> getAllMyCatalogs(Context context) {
        List<Catalog> catalogs = new ArrayList<>();
        String userName = GoogleSignIn.getLastSignedInAccount(context).getDisplayName();

        File catalogFolder = getCatalogFolder();
        File[] catalogFileList = catalogFolder.listFiles();

        for (File catalogFile : catalogFileList) {
            String[] splitFileName = catalogFile.getName().split("_");
            String albumName = splitFileName[splitFileName.length-1];
            List<String> catalogLineList = new ArrayList<>();
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(catalogFile));
                String catalogLine;
                while ((catalogLine = bufferedReader.readLine()) != null) {
                    if (!catalogLine.equals("")) {
                        catalogLineList.add(catalogLine);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            catalogs.add(new Catalog(null, userName, albumName, catalogLineList));
        }
        return catalogs;
    }

    private static void getMyPhotosForAlbum(Album album, Context context, ViewAlbumAdapter viewAlbumAdapter) {
        List<Catalog> allMyCatalogs = getAllMyCatalogs(context);
        for (Catalog catalog : allMyCatalogs) {
            if (catalog.getAlbumName() == album.getName()) {
                List<String> photoUriStrings = catalog.getCatalogLineList();
                for (String photoUriString : photoUriStrings) {
                    viewAlbumAdapter.addPhoto(photoUriString);
                }
            }
        }
    }

    public static String generateFileId(String userId, String catalogLine) {
        return userId + "_" + FilenameUtils.getName(catalogLine);
    }

    private void sendPhoto(OutputStream outputStream, String photoUriString) {
        new SendPhotoTask(outputStream, photoUriString).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                SimWifiP2pSocket clientSocket = new SimWifiP2pSocket(targetDeviceVirtIp, PORT);
                clientSocket.getOutputStream().write(
                    (ASK_FOR_PHOTO_MSG + "\n" + thisDevice.getVirtIp() + "\n" + photoUriString).getBytes());

                file = new java.io.File(folderPath, fileId);
                // Stream the file contents to a File.
                InputStream is = clientSocket.getInputStream();
                FileUtils.copyInputStreamToFile(is, file);

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

    private static class SendPhotoTask extends AsyncTask<Void, String, Void> {

        private final OutputStream outputStream;
        private final String photoUriString;

        public SendPhotoTask(OutputStream outputStream, String photoUriString) {
            this.outputStream = outputStream;
            this.photoUriString = photoUriString;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            int len;
            byte buf[] = new byte[1024];
            try {
                InputStream inputStream = getInputStreamFromPhotoUri();
                while ((len = inputStream.read(buf)) != -1) {
                    outputStream.write(buf, 0, len);
                }
                outputStream.close();
                inputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private InputStream getInputStreamFromPhotoUri() throws FileNotFoundException {
            File file = new File(photoUriString);
            return new FileInputStream(file);
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.d(TAG, "Sent photo " + photoUriString);
        }
    }

    private class IncommingCommTask extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            SimWifiP2pSocketServer serverSocket = null;
            try {
                serverSocket = new SimWifiP2pSocketServer(PORT);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SimWifiP2pSocket socket = serverSocket.accept();
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
                            peersCatalogs.remove(catalog);
                            peersCatalogs.add(new Catalog(targetVirtIp, userId, albumId, catalogLineList));

                        } else if (messageCode.equals(ASK_FOR_PHOTO_MSG)) {
                            String targetVirtIp = sockIn.readLine();
                            String photoUriString = sockIn.readLine();
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
                    Log.d("Error socket:", e.getMessage());
                    break;
                    //e.printStackTrace();
                }
            }

            return null;

        }
    }
}
