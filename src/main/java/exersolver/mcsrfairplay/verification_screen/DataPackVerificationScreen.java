package exersolver.mcsrfairplay.verification_screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.pack.DataPackScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

public class DataPackVerificationScreen extends VerificationScreen {
    private final DataPackScreen dataPackScreen;

    public DataPackVerificationScreen(Screen parent, MinecraftServer server) {
        super(parent);
        this.dataPackScreen = new DataPackScreen(null, server.getDataPackManager(), manager -> {}, server.getSavePath(WorldSavePath.DATAPACKS).toFile());
    }

    @Override
    protected void init() {
        this.dataPackScreen.init(this.client, this.width, this.height);
        super.init();
        this.next.setMessage(ScreenTexts.DONE);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(0);
        this.dataPackScreen.render(matrices, 0, 0, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        MinecraftClient.getInstance().openScreen(this.parent);
    }
}
