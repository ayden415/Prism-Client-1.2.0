package dev.prism.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.prism.core.Config;
import dev.prism.core.HudRenderer;
import dev.prism.core.ZoomController;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class PrismClient implements ClientModInitializer {
    public static final String ID = "prism";

    public static KeyMapping openMenu;
    public static KeyMapping zoom;

    private static final ZoomController ZOOM = new ZoomController();
    private static final ZoomController.Fov FOV = new ZoomController.Fov() {
        @Override public int get() { return Minecraft.getInstance().options.fov().get(); }

        @Override public void set(int value) { Minecraft.getInstance().options.fov().set(value); }
    };

    @Override
    public void onInitializeClient() {
        Config.init(FabricLoader.getInstance().getConfigDir().resolve("prism-client.properties"));

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(ID, "main"));

        // The keybind that opens the menu. Right Shift by default, rebindable in Options > Controls.
        openMenu = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.prism.open_menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        zoom = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.prism.zoom", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, category));

        Game game = new Game();
        Gfx gfx = new Gfx();

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(ID, "hud"), (graphics, deltaTracker) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.options.hideGui) return; // respect F1
            HudRenderer.render(gfx.begin(graphics), game,
                    mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
        });

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openMenu.consumeClick()) {
                if (mc.player != null && mc.screen == null) mc.setScreen(new MenuScreen(openMenu));
            }
            ZOOM.tick(mc.player != null && mc.screen == null && zoom.isDown(), FOV);
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> ZOOM.reset(FOV));
    }
}
