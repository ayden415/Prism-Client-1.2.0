package dev.prism.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.prism.core.Config;
import dev.prism.core.HudRenderer;
import dev.prism.core.ZoomController;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
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
        openMenu = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.prism.open_menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        zoom = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.prism.zoom", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, category));

        Game game = new Game();
        Gfx gfx = new Gfx();

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(ID, "hud"), (graphics, deltaTracker) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gui.hud.isHidden()) return; // respect F1 (26.2 moved hideGui into Gui.hud)
            HudRenderer.render(gfx.begin(graphics), game,
                    mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
        });

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openMenu.consumeClick()) {
                if (mc.player != null && Compat.noScreenOpen(mc)) Compat.show(mc, new MenuScreen(openMenu));
            }
            ZOOM.tick(mc.player != null && Compat.noScreenOpen(mc) && zoom.isDown(), FOV);
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> ZOOM.reset(FOV));
    }
}
