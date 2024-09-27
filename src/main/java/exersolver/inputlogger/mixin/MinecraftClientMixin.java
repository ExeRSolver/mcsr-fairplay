package exersolver.inputlogger.mixin;

import exersolver.inputlogger.InputListener;
import exersolver.inputlogger.output.OutputUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
	@Shadow
	@Nullable
	public ClientWorld world;

	@Inject(at = @At("HEAD"), method = "createWorld")
	public void onCreate(String worldName, LevelInfo levelInfo, DynamicRegistryManager.Impl registryTracker, GeneratorOptions generatorOptions, CallbackInfo ci) {
		if (MinecraftClient.getInstance().isOnThread())
			OutputUtils.setFileWriter(worldName);
    }

	@Inject(at = @At("HEAD"), method = "startIntegratedServer(Ljava/lang/String;)V")
	public void onWorldOpen(String worldName, CallbackInfo ci) {
		OutputUtils.setFileWriter(worldName);
	}

	@Inject(at = @At("HEAD"), method = "onWindowFocusChanged(Z)V")
	private void onFocusChanged(boolean focused, CallbackInfo info) {
		InputListener.onFocusChanged(focused);
	}

	@Inject(at = @At("HEAD"), method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V")
	public void disconnect(CallbackInfo ci) {
		if (this.world != null)
			InputListener.closeFileWriter();
	}
}