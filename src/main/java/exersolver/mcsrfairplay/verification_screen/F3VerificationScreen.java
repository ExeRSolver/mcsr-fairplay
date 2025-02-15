package exersolver.mcsrfairplay.verification_screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

public class F3VerificationScreen extends VerificationScreen {
    private final DebugHud debugHud;

    public F3VerificationScreen(Screen parent) {
        super(parent);
        this.debugHud = new DebugHud(MinecraftClient.getInstance());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(0);
        this.debugHud.render(matrices);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        MinecraftClient.getInstance().openScreen(new ResourcePackVerificationScreen(this.parent));
    }
}
