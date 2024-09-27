package exersolver.inputlogger.mixin;

import com.mojang.brigadier.context.CommandContext;
import exersolver.inputlogger.output.BufferedCryptoZipWriter;
import exersolver.inputlogger.InputListener;
import exersolver.inputlogger.InputLogger;
import net.minecraft.server.command.SeedCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;

@Mixin(SeedCommand.class)
public abstract class SeedCommandMixin {

    @Inject(at = @At("RETURN"), method = "method_13617")
    private static void printHashInChat(CommandContext commandContext, CallbackInfoReturnable<Integer> cir) {
        BufferedCryptoZipWriter fileWriter = InputListener.getFileWriter();
        if (fileWriter == null)
            return;

        try {
            fileWriter.close();
            String hash = fileWriter.getHashHex();
            Text text = new LiteralText(InputLogger.MOD_ID + " hash: " + hash);
            ((ServerCommandSource) commandContext.getSource()).sendFeedback(text, false);
        } catch (IOException e) {
            InputLogger.LOGGER.error(e.getMessage(), e);
        }
    }
}
