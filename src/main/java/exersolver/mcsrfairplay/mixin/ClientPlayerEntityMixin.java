package exersolver.mcsrfairplay.mixin;

import exersolver.mcsrfairplay.InputListener;
import exersolver.mcsrfairplay.ModHashing;
import exersolver.mcsrfairplay.output.BufferedCryptoZipWriter;
import exersolver.mcsrfairplay.output.OutputUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    @Inject(at = @At("HEAD"), method = "sendChatMessage")
    private void printHashInChatOnSeedCommand(String message, CallbackInfo ci) {
        if (!message.equals("/seed"))
            return;

        BufferedCryptoZipWriter fileWriter = InputListener.getFileWriter();
        if (fileWriter == null)
            return;

        OutputUtils.resetFileWriter();
        String hash = fileWriter.getHashHex();
        Text text = new LiteralText( fileWriter.getHashFileName() + ": " + hash);
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(text);

        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(
                new TranslatableText("mcsrfairplay.gui.verification.message", Texts.bracketed(
                        new TranslatableText("mcsrfairplay.gui.verification.here")
                                .styled(style -> style.withColor(Formatting.GREEN)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "mcsrfairplay.open_verification_screen"))
                                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("mcsrfairplay.gui.verification.tooltip")))
                                )
                )).append(" (" + ModHashing.SESSION + ")")
        );
    }
}
