package exersolver.mcsrfairplay.verification_screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public abstract class VerificationScreen extends Screen {
    public static final Text NEXT = new TranslatableText("mcsrfairplay.gui.verification.next_page");

    protected final Screen parent;
    protected ButtonWidget next;
    private long activateNext;

    protected VerificationScreen(Screen parent) {
        super(new TranslatableText("mcsrfairplay.gui.verification.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.next = this.addButton(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, NEXT, button -> this.onClose()));
        this.next.active = false;
        this.activateNext = System.currentTimeMillis() + 250;
    }

    @Override
    public void tick() {
        if (!this.next.active && this.shouldActivateNext()) {
            this.next.active = true;
        }
    }

    protected boolean shouldActivateNext() {
        return System.currentTimeMillis() > this.activateNext;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return this.next.active;
    }

    public static void start() {
        MinecraftClient.getInstance().openScreen(new ModVerificationScreen(MinecraftClient.getInstance().currentScreen));
    }
}
