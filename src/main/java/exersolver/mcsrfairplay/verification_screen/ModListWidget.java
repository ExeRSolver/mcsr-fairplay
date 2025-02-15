package exersolver.mcsrfairplay.verification_screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringRenderable;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ModListWidget extends ElementListWidget<ModListWidget.Entry> {
    private double lastScrollAmount = 0.0;

    public ModListWidget(MinecraftClient minecraftClient, int width, int height, int top, int bottom, Map<Text, List<List<StringRenderable>>> hashes, int totalHeight) {
        super(minecraftClient, width, height, top, bottom, totalHeight);
        // we cheat the list by only giving it a single entry,
        // so we can implement our own spacing instead of a fixed itemHeight
        this.addEntry(new Entry(hashes));
    }

    public void updateLastScrollAmount() {
        this.lastScrollAmount = Math.max(this.lastScrollAmount, this.getScrollAmount());
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
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        // don't consider itemHeight for this calculation
        this.setScrollAmount(this.getScrollAmount() - amount * 15);
        return true;
    }

    @Override
    public void setScrollAmount(double amount) {
        super.setScrollAmount(Math.min(amount, this.lastScrollAmount + this.height / 4.0));
    }

    public static class Entry extends ElementListWidget.Entry<Entry> {
        private final Map<Text, List<List<StringRenderable>>> hashes;

        public Entry(Map<Text, List<List<StringRenderable>>> hashes) {
            this.hashes = hashes;
        }

        @Override
        public List<? extends Element> children() {
            return Collections.emptyList();
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            y += 3;
            for (Map.Entry<Text, List<List<StringRenderable>>> entry : this.hashes.entrySet()) {
                textRenderer.draw(matrices, entry.getKey(), 10, y, 0xFFFFFF);
                y += 14;

                for (List<StringRenderable> list : entry.getValue()) {
                    for (StringRenderable line : list) {
                        textRenderer.draw(matrices, line, 15, y, 0xFFFFFF);
                        y += 10;
                    }
                    y += 2;
                }
                y += 3;
            }
        }
    }
}
