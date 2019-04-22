package pt.ulisboa.tecnico.cmu.utils;

import com.google.api.services.drive.Drive;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class GoogleDriveUtils {
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Drive driveService;

    public GoogleDriveUtils(Drive driveService) {
        this.driveService = driveService;
    }

}
