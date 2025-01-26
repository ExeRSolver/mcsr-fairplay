package exersolver.mcsrfairplay;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public class MCSRFairplay implements ClientModInitializer, PreLaunchEntrypoint {
	public static final String MOD_ID = "mcsrfairplay";

	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static final Object2IntMap<String> MOD_HASHES = new Object2IntOpenHashMap<>();

	@Override
	public void onPreLaunch() {
		LOGGER.info("Generating hashes for loaded mods...");

		int sessionId = (int) System.currentTimeMillis();
		int sessionHash = sessionId;

		for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
			int hash = 1;
			for (Path root : mod.getRootPaths()) {
				try (Stream<Path> stream = Files.walk(root)) {
					hash = hash * 31 + stream.filter(path -> !Files.isDirectory(path)).mapToInt(path -> {
						try (CheckedInputStream checked = new CheckedInputStream(Files.newInputStream(path), new CRC32())) {
							return path.hashCode() * 31 + Long.hashCode(checked.getChecksum().getValue());
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}).reduce((i, j) -> i * 31 + j).orElse(1);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			sessionHash = (sessionHash * 31 + hash) * 31 + sessionId;

			String id = mod.getMetadata().getId();
			MOD_HASHES.put(id, hash);
			LOGGER.info("{}: {}", id, hash);
		}

		LOGGER.info("Finished generating hashes for session {}", Math.abs(sessionId) + "-" + Math.abs(sessionHash));
	}

	@Override
	public void onInitializeClient() {
		InputListener.init();
	}
}