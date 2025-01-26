package exersolver.mcsrfairplay.verification_screen;

import exersolver.mcsrfairplay.MCSRFairplay;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ModListWidget extends ElementListWidget<ModListWidget.Entry> {

    public ModListWidget(MinecraftClient minecraftClient, int width, int height, int top, int bottom, int itemHeight) {
        super(minecraftClient, width, height, top, bottom, itemHeight);

        List<ModContainer> mods = new ArrayList<>(FabricLoader.getInstance().getAllMods());
        mods.sort(Comparator.comparing(mod -> mod.getMetadata().getName()));
        for (ModContainer mod : mods) {
            this.addEntry(new Entry(mod.getMetadata()));
        }
    }

    public boolean isScrolledToBottom() {
        return this.getRowTop(this.children().size() - 1) + this.itemHeight <= this.bottom;
    }

    @Override
    public int getRowWidth() {
        return this.width - 50;
    }

    @Override
    protected int getRowLeft() {
        return 25;
    }

    @Override
    protected int getScrollbarPositionX() {
        return this.width - 6;
    }

    @Override
    protected int getMaxPosition() {
        return super.getMaxPosition();
    }

    @Override
    public Optional<Element> hoveredElement(double mouseX, double mouseY) {
        return super.hoveredElement(mouseX, mouseY);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        return super.charTyped(chr, keyCode);
    }

    @Override
    public void setInitialFocus(@Nullable Element element) {
        super.setInitialFocus(element);
    }

    @Override
    public void focusOn(@Nullable Element element) {
        super.focusOn(element);
    }

    public static class Entry extends ElementListWidget.Entry<Entry> {
        private final Text name;
        private final Text hash;

        public Entry(ModMetadata mod) {
            this.name = new LiteralText(mod.getName()).append(new LiteralText(" (" + mod.getId() + "-" + mod.getVersion().getFriendlyString() + ")").formatted(Formatting.GRAY));
            this.hash = new LiteralText(String.valueOf(MCSRFairplay.MOD_HASHES.getInt(mod.getId())));
        }

        @Override
        public List<? extends Element> children() {
            return Collections.emptyList();
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            textRenderer.draw(
                    matrices,
                    this.name,
                    x,
                    y + 3,
                    0xFFFFFF
            );
            textRenderer.draw(
                    matrices,
                    this.hash,
                    x + entryWidth - textRenderer.getWidth(this.hash),
                    y + 3,
                    0xFFFFFF
            );
        }
    }
}
