package exersolver.mcsrfairplay.verification_screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public abstract class VerificationScreen extends Screen {
    public static final Text NEXT = new TranslatableText("mcsrfairplay.gui.verification.next_page");

    protected final Screen parent;

    protected VerificationScreen(Screen parent) {
        super(new TranslatableText("mcsrfairplay.gui.verification.title"));
        this.parent = parent;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    public static void start() {
        MinecraftClient.getInstance().openScreen(new ModVerificationScreen(MinecraftClient.getInstance().currentScreen));
    }
}
