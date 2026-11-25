package dev.prism.client;

import dev.prism.core.MenuUI;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Thin Screen wrapper: all layout, animation and logic lives in the shared MenuUI. */
public final class MenuScreen extends Screen {
    private final MenuUI ui;
    private final KeyMapping openKey;
    private final Gfx gfx = new Gfx();

    public MenuScreen(KeyMapping openKey) {
        super(Component.literal("Prism"));
        this.openKey = openKey;
        this.ui = new MenuUI(() -> super.onClose(), () -> openKey.getTranslatedKeyMessage().getString());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        ui.render(gfx.begin(graphics), mouseX, mouseY, this.width, this.height);
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override public void onClose() { ui.requestClose(); }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return ui.mouseClicked(event.x(), event.y(), event.button());
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) { return ui.mouseReleased(); }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        return ui.mouseDragged(event.x(), event.y());
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        return ui.mouseScrolled(scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return ui.keyPressed(event.key(), openKey.matches(event)) || super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return ui.charTyped(event.codepoint()) || super.charTyped(event);
    }
}
