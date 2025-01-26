package exersolver.mcsrfairplay.verification_screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;

public class F3VerificationScreen extends VerificationScreen {
    private final DebugHud debugHud;
    private ButtonWidget done;
    private long activateDone;

    public F3VerificationScreen(Screen parent) {
        super(parent);
        this.debugHud = new DebugHud(MinecraftClient.getInstance());
    }

    @Override
    protected void init() {
        this.done = this.addButton(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, ScreenTexts.DONE, button -> this.onClose()));
        this.done.active = false;
        this.activateDone = System.currentTimeMillis() + 500;
    }

    @Override
    public void tick() {
        if (!this.done.active && System.currentTimeMillis() > this.activateDone) {
            this.done.active = true;
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(0);
        this.debugHud.render(matrices);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        MinecraftClient.getInstance().openScreen(this.parent);
    }
}
