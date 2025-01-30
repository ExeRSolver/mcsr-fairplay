package exersolver.mcsrfairplay.verification_screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.ResourcePackScreen;
import net.minecraft.client.util.math.MatrixStack;

import java.util.Objects;

public class ResourcePackVerificationScreen extends VerificationScreen {
    private final ResourcePackScreen resourcePackScreen;

    public ResourcePackVerificationScreen(Screen parent) {
        super(parent);
        this.resourcePackScreen = new ResourcePackScreen(null, MinecraftClient.getInstance().getResourcePackManager(), manager -> {}, MinecraftClient.getInstance().getResourcePackDir());
    }

    @Override
    protected void init() {
        this.resourcePackScreen.init(this.client, this.width, this.height);
        super.init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(0);
        this.resourcePackScreen.render(matrices, 0, 0, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        MinecraftClient.getInstance().openScreen(new DataPackVerificationScreen(this.parent, Objects.requireNonNull(MinecraftClient.getInstance().getServer())));
    }
}
