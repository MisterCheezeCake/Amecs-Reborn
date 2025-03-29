/*
 * Copyright 2020-2023 Siphalor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.siphalor.amecs.mixin;

import de.siphalor.amecs.Amecs;
import de.siphalor.amecs.api.KeyBindingUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Siphalor
 */
@Environment(EnvType.CLIENT)
@Mixin(InputUtil.Type.class)
public abstract class MixinInputUtilType {
	@SuppressWarnings("UnresolvedMixinReference")
	@Inject(method = "<clinit>", at = @At("RETURN"))
	private static void onRegisterKeyCodes(CallbackInfo callbackInfo) {
		createScrollKey("mouse.scroll.up", KeyBindingUtils.MOUSE_SCROLL_UP);
		createScrollKey("mouse.scroll.down", KeyBindingUtils.MOUSE_SCROLL_DOWN);
		createScrollKey("mouse.scroll.left", KeyBindingUtils.MOUSE_SCROLL_LEFT);
		createScrollKey("mouse.scroll.right", KeyBindingUtils.MOUSE_SCROLL_RIGHT);
	}

	@Unique
	private static void createScrollKey(String name, int keyCode) {
		String keyName = Amecs.makeKeyID(name);
		InputUtil.Type.mapKey(InputUtil.Type.MOUSE, keyName, keyCode);

		// Legacy compatibility (amecsapi <1.3)
		InputUtil.Key.KEYS.put("amecsapi.key." + name, InputUtil.fromTranslationKey(keyName));
	}
}
