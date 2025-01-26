package exersolver.mcsrfairplay.verification_screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

public class VerificationScreen extends Screen {
    private final Screen parent;

    private ModListWidget list;
    private ButtonWidget done;
    private boolean hasScrolledToBottom;

    public VerificationScreen(Screen parent) {
        super(new TranslatableText("mcsrfairplay.gui.verification.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.list = this.addChild(new ModListWidget(this.client, this.width, this.height, 32, this.height - 32, 15));
        this.done = this.addButton(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, new TranslatableText("mcsrfairplay.gui.verification.please_scroll"), button -> this.onClose()));
        this.done.active = false;
    }

    @Override
    public void tick() {
        if (!this.hasScrolledToBottom && this.list.isScrolledToBottom()) {
            this.done.setMessage(ScreenTexts.DONE);
            this.done.active = true;
            this.hasScrolledToBottom = true;
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.list.render(matrices, mouseX, mouseY, delta);
        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return this.hasScrolledToBottom;
    }

    @Override
    public void onClose() {
        MinecraftClient.getInstance().openScreen(this.parent);
    }
}
