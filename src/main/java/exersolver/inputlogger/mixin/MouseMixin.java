package exersolver.inputlogger.mixin;

import exersolver.inputlogger.InputListener;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Shadow public abstract double getX();
    @Shadow public abstract double getY();

    @Inject(method = "lockCursor",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/InputUtil;setCursorParameters(JIDD)V"))
    private void onCursorLock(CallbackInfo ci) {
        InputListener.onCursorLockChanged(true, this.getX(), this.getY());
    }

    @Inject(method = "unlockCursor",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/InputUtil;setCursorParameters(JIDD)V"))
    private void onCursorUnlock(CallbackInfo ci) {
        InputListener.onCursorLockChanged(false, this.getX(), this.getY());
    }
}
