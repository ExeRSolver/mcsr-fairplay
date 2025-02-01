package exersolver.mcsrfairplay.verification_screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringRenderable;

import java.util.Collections;
import java.util.List;

public class ZipFilesExplanationListWidget extends ElementListWidget<ZipFilesExplanationListWidget.Entry> {

    public ZipFilesExplanationListWidget(MinecraftClient minecraftClient, int width, int height, int top, int bottom, List<List<StringRenderable>> lines, int totalHeight) {
        super(minecraftClient, width, height, top, bottom, totalHeight);
        // we cheat the list by only giving it a single entry,
        // so we can implement our own spacing instead of a fixed itemHeight
        this.addEntry(new Entry(lines));
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
        if (this.height < this.itemHeight) {
            // don't render scrollbar if there is nothing to scroll
            return this.width;
        }
        return this.width - 6;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        // don't consider itemHeight for this calculation
        this.setScrollAmount(this.getScrollAmount() - amount * 15);
        return true;
    }

    public static class Entry extends ElementListWidget.Entry<Entry> {
        private final List<List<StringRenderable>> lines;

        public Entry(List<List<StringRenderable>> lines) {
            this.lines = lines;
        }

        @Override
        public List<? extends Element> children() {
            return Collections.emptyList();
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            y += 3;
            for (List<StringRenderable> line : this.lines) {
                for (StringRenderable string : line) {
                    textRenderer.draw(matrices, string, 10, y, 0xFFFFFF);
                    y += 10;
                }
                y += 3;
            }
        }
    }
}
