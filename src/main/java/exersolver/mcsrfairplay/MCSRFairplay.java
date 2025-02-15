package exersolver.mcsrfairplay;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

public class MCSRFairplay implements ClientModInitializer, PreLaunchEntrypoint {
	public static final String MOD_ID = "mcsrfairplay";

	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final Path DIRECTORY = FabricLoader.getInstance().getGameDir().resolve("mcsrfairplay");

	public static final boolean HAS_ATUM = FabricLoader.getInstance().isModLoaded("atum");

    @Override
	public void onPreLaunch() {
		ModHashing.init();
	}

	@Override
	public void onInitializeClient() {
		InputListener.init();
	}
}