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

import de.siphalor.amecs.impl.KeyModifierOptions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Siphalor
 */
@SuppressWarnings("WeakerAccess")
@Environment(EnvType.CLIENT)
@Mixin(GameOptions.class)
public class MixinGameOptions {
	@Shadow
	@Final
	public KeyBinding[] allKeys;

	@Inject(method = "write", at = @At("RETURN"))
	public void write(CallbackInfo callbackInfo) {
		KeyModifierOptions.write(allKeys);
	}

	@Inject(method = "load", at = @At("RETURN"))
	public void load(CallbackInfo callbackInfo) {
		KeyModifierOptions.read();
	}
}
