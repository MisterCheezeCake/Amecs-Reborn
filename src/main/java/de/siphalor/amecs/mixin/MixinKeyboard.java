package de.siphalor.amecs.mixin;

import de.siphalor.amecs.Amecs;
import de.siphalor.amecs.api.KeyModifier;
import de.siphalor.amecs.impl.KeyBindingManager;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Siphalor
 */
@Mixin(Keyboard.class)
public abstract class MixinKeyboard {
	@ModifyVariable(
			method = "onKey",
			argsOnly = true,
			ordinal = 0,
			at = @At(value = "FIELD", target = "Lnet/minecraft/client/Keyboard;debugCrashStartTime:J")
	)
	public int modifyPressedKey(int key, long window, int key_, int scancode) {
		if (Amecs.ESCAPE_KEYBINDING != null && Amecs.ESCAPE_KEYBINDING.matchesKey(key, scancode)) {
			return GLFW.GLFW_KEY_ESCAPE;
		}
		return key;
	}

	@Inject(method = "onKey", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;", ordinal = 0, shift = At.Shift.BEFORE), cancellable = true)
	private void onKeyPriority(long window, int key, int scanCode, int action, int modifiers, CallbackInfo callbackInfo) {
		if (action == 1) {
			if (KeyBindingManager.onKeyPressedPriority(InputUtil.fromKeyCode(key, scanCode))) {
				callbackInfo.cancel();
			}
		} else if (action == 0) {
			if (KeyBindingManager.onKeyReleasedPriority(InputUtil.fromKeyCode(key, scanCode))) {
				callbackInfo.cancel();
			}
		}
	}

	@Inject(method = "onKey", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Keyboard;debugCrashStartTime:J", ordinal = 0))
	private void onKey(long window, int key, int scanCode, int action, int modifiers, CallbackInfo callbackInfo) {
		// Key released
		if (action == 0 && MinecraftClient.getInstance().currentScreen instanceof KeybindsScreen screen) {
            screen.selectedKeyBinding = null;
			screen.lastKeyCodeUpdateTime = Util.getMeasuringTimeMs();
		}

		Amecs.CURRENT_MODIFIERS.set(KeyModifier.fromKeyCode(InputUtil.fromKeyCode(key, scanCode).getCode()), action != 0);
	}
}
