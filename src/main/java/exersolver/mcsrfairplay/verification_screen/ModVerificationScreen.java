package exersolver.mcsrfairplay.verification_screen;

import exersolver.mcsrfairplay.ModHashing;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringRenderable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.*;

public class ModVerificationScreen extends VerificationScreen {
    private ModListWidget list;
    private boolean hasScrolledToBottom;

    public ModVerificationScreen(Screen parent) {
        super(parent);
    }

    @Override
    protected void init() {
        Map<Text, List<List<StringRenderable>>> hashes = new LinkedHashMap<>();

        List<ModContainer> mods = new ArrayList<>(FabricLoader.getInstance().getAllMods());
        mods.sort(Comparator.comparing(mod -> mod.getMetadata().getName()));
        for (ModContainer mod : mods) {
            String id = mod.getMetadata().getId();
            String modHash = ModHashing.MOD_HASHES.get(id);
            String fileHash = ModHashing.FILE_HASHES.get(id);

            Text text = new LiteralText(mod.getMetadata().getName()).append(
                    new LiteralText(" (" + id + "-" + mod.getMetadata().getVersion().getFriendlyString() + ")").formatted(Formatting.GRAY)
            );

            List<List<StringRenderable>> lists = new ArrayList<>();
            lists.add(this.textRenderer.wrapLines(StringRenderable.plain(modHash), this.width - 30));
            if (fileHash != null) {
                lists.add(this.textRenderer.wrapLines(StringRenderable.plain(fileHash), this.width - 30));
            }

            hashes.put(text, lists);
        }

        this.list = this.addChild(new ModListWidget(
                this.client, this.width, this.height, 32, this.height - 32, hashes,
                3 + hashes.values().stream().mapToInt(lists -> 14 + lists.stream().mapToInt(list -> list.size() * 10 + 2).sum() + 3).sum()
        ));

        super.init();
        this.next.setMessage(new TranslatableText("mcsrfairplay.gui.verification.please_scroll"));
    }

    @Override
    public void tick() {
        this.list.updateLastScrollAmount();
        if (!this.hasScrolledToBottom && this.list.isScrolledToBottom()) {
            this.next.setMessage(NEXT);
            this.hasScrolledToBottom = true;
        }
        super.tick();
    }

    @Override
    protected boolean shouldActivateNext() {
        return this.hasScrolledToBottom && super.shouldActivateNext();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.list.render(matrices, mouseX, mouseY, delta);
        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        MinecraftClient.getInstance().openScreen(new F3VerificationScreen(this.parent));
    }
}
