package exersolver.mcsrfairplay.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import exersolver.mcsrfairplay.verification_screen.VerificationScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.ClickEvent;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @WrapWithCondition(
            method = "handleTextClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/apache/logging/log4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V",
                    remap = false
            )
    )
    private boolean openVerificationScreenClickEvent(Logger logger, String string, Object o) {
        ClickEvent event = (ClickEvent) o;
        if (event.getAction() == ClickEvent.Action.CHANGE_PAGE && event.getValue().equals("mcsrfairplay.open_verification_screen")) {
            VerificationScreen.start();
            return false;
        }
        return true;
    }
}
