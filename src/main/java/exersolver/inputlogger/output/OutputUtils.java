package exersolver.inputlogger.output;

import exersolver.inputlogger.InputListener;
import exersolver.inputlogger.InputLogger;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

public class OutputUtils {
    private static String worldName = null;

    public static void resetFileWriter() {
        if (worldName != null)
            setFileWriter(worldName);
    }

    public static void setFileWriter(String worldName) {
        try {
            File logFile = FabricLoader.getInstance().getGameDir().resolve("saves").resolve(worldName).resolve(InputLogger.MOD_ID).toFile();
            logFile.mkdirs();

            int logNum = getLogCount(logFile) + 1;
            logFile = logFile.toPath().resolve("input_log-" + logNum + ".zip").toFile();
            String logFileName = "input_log-" + logNum + ".log";
            String hashFileName = "input_log_hash-" + logNum + ".blake3";

            ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(logFile));
            BufferedCryptoZipWriter fileWriter = new BufferedCryptoZipWriter(new CryptoZipWriter(outputStream, logFileName, hashFileName));
            InputListener.setFileWriter(fileWriter);
            OutputUtils.worldName = worldName;
        } catch (IOException e) {
            InputLogger.LOGGER.error(e.getMessage(), e);
        }
    }

    public static int getLogCount(File logFile) throws IOException {
        return logFile.list().length;
    }
}
