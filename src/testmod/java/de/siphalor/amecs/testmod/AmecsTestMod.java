package de.siphalor.amecs.testmod;

import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyModifiers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class AmecsTestMod implements ClientModInitializer {
    public static final String MOD_ID = "amecstestmod";

    @Override
    public void onInitializeClient() {
        KeyBinding kbd = KeyBindingHelper.registerKeyBinding(new AmecsKeyBinding(Identifier.of(MOD_ID, "kbd"), InputUtil.Type.KEYSYM, 86, "key.categories.movement", new KeyModifiers(true, true, false, false)));

        KeyBindingHelper.registerKeyBinding(new TestPriorityKeybinding(Identifier.of("amecs-testmod", "priority"), InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.categories.misc", new KeyModifiers(), () -> {
            System.out.println("priority");
            return true;
        }, () -> {
            System.out.println("priority release");
            return true;
        }));
    }
}
