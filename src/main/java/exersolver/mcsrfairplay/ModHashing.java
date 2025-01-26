package exersolver.mcsrfairplay;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.ModOrigin;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ModHashing {
    public static final Map<String, String> MOD_HASHES = new HashMap<>();
    public static final Map<String, String> FILE_HASHES = new HashMap<>();

    public static void init() {
        MCSRFairplay.LOGGER.info("Generating hashes for loaded mods...");

        int sessionId = (int) System.currentTimeMillis();
        int sessionHash = sessionId;

        MessageDigest sha512;
        try {
            sha512 = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            String id = mod.getMetadata().getId();
            String modHash = hashMod(mod, sha512);
            String fileHash = hashFile(mod, sha512);

            MOD_HASHES.put(id, modHash);
            MCSRFairplay.LOGGER.info("{}: {}", id, modHash);

            if (fileHash != null) {
                FILE_HASHES.put(id, fileHash);
                MCSRFairplay.LOGGER.info("{} (file): {}", id, fileHash);
            }

            sessionHash = sessionHash * 31 + modHash.hashCode() * sessionId;
        }

        MCSRFairplay.LOGGER.info("Finished generating hashes for session {}", Math.abs(sessionId) + "-" + Math.abs(sessionHash));
    }

    private static String hashMod(ModContainer mod, MessageDigest sha512) {
        for (Path root : mod.getRootPaths()) {
            try (Stream<Path> stream = Files.walk(root)) {
                stream.filter(path -> !Files.isDirectory(path)).forEach(path -> {
                    try {
                        sha512.update(path.toString().getBytes(StandardCharsets.UTF_8));
                        sha512.update(Files.readAllBytes(path));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return Hex.encodeHexString(sha512.digest());
    }

    private static String hashFile(ModContainer mod, MessageDigest sha512) {
        ModMetadata metadata = mod.getMetadata();
        ModOrigin origin = mod.getOrigin();
        if (metadata.getType().equals("builtin") || origin.getKind() != ModOrigin.Kind.PATH) {
            return null;
        }
        List<Path> paths = origin.getPaths();
        if (paths.size() != 1) {
            MCSRFairplay.LOGGER.warn("Mod {} has multiple origin paths, skipping hashing.", metadata.getId());
            return null;
        }
        Path path = paths.get(0);
        if (Files.isDirectory(path)) {
            MCSRFairplay.LOGGER.warn("Mod {} has a directory origin, skipping hashing.", metadata.getId());
            return null;
        }
        try {
            sha512.update(Files.readAllBytes(paths.get(0)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Hex.encodeHexString(sha512.digest());
    }
}
