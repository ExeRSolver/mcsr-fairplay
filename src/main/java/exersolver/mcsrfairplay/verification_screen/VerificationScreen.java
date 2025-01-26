package exersolver.mcsrfairplay.verification_screen;

import exersolver.mcsrfairplay.ModHashing;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringRenderable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.*;

public class VerificationScreen extends Screen {
    private final Screen parent;

    private VerificationListWidget list;
    private ButtonWidget done;
    private boolean hasScrolledToBottom;

    public VerificationScreen(Screen parent) {
        super(new TranslatableText("mcsrfairplay.gui.verification.title"));
        this.parent = parent;
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

        this.list = this.addChild(new VerificationListWidget(
                this.client, this.width, this.height, 32, this.height - 32, hashes,
                3 + hashes.values().stream().mapToInt(lists -> 14 + lists.stream().mapToInt(list -> list.size() * 10 + 2).sum() + 3).sum()
        ));

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
        this.list.updateLastScrollAmount();
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
