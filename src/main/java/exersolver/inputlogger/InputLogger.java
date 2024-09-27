package exersolver.inputlogger;

import net.fabricmc.api.ClientModInitializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InputLogger implements ClientModInitializer {
	public static final String MOD_ID = "inputlogger";

	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		InputListener.init();
	}
}