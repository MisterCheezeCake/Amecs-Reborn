/*
 * Copyright 2020-2023 Siphalor, 2025 MisterCheezeCake
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
package de.siphalor.amecs.mixin.compat;

import com.blamejared.controlling.client.NewKeyBindsScreen;
import com.llamalad7.mixinextras.sugar.Local;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyModifier;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author MisterCheezeCake
 */
@IfModLoaded("controlling")
@Mixin(NewKeyBindsScreen.class)
public abstract class MixinControllingKeybindsScreen extends KeybindsScreen {

    @Shadow public abstract ControlsListWidget getKeyBindsList();

    public MixinControllingKeybindsScreen(Screen parent, GameOptions gameOptions) {
        super(parent, gameOptions);
    }

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lcom/blamejared/controlling/platform/IPlatformHelper;handleKeyPress(Lcom/blamejared/controlling/client/NewKeyBindsScreen;Lnet/minecraft/client/option/GameOptions;III)V"), cancellable = true)
    public void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
       assert this.selectedKeyBinding != null;
        if (selectedKeyBinding.isUnbound()) {
            selectedKeyBinding.setBoundKey(InputUtil.fromKeyCode(keyCode, scanCode));
        } else {
            InputUtil.Key mainKey = ((IKeyBinding) selectedKeyBinding).amecs$getBoundKey();
            KeyModifiers keyModifiers = ((IKeyBinding) selectedKeyBinding).amecs$getKeyModifiers();
            KeyModifier mainKeyModifier = KeyModifier.fromKey(mainKey);
            KeyModifier keyModifier = KeyModifier.fromKeyCode(keyCode);
            if (mainKeyModifier != KeyModifier.NONE && keyModifier == KeyModifier.NONE) {
                keyModifiers.set(mainKeyModifier, true);
                selectedKeyBinding.setBoundKey(InputUtil.fromKeyCode(keyCode, scanCode));
                return;
            } else {
                keyModifiers.set(keyModifier, true);
                keyModifiers.cleanup(selectedKeyBinding);
            }
        }

        this.lastKeyCodeUpdateTime = Util.getMeasuringTimeMs();
        this.getKeyBindsList().update();
        cir.setReturnValue(true);
    }
    @Inject(method = "lambda$new$9", at = @At(value = "INVOKE", target = "Lcom/blamejared/controlling/platform/IPlatformHelper;setToDefault(Lnet/minecraft/client/option/GameOptions;Lnet/minecraft/client/option/KeyBinding;)V"))
    public void onSetToDefault(ButtonWidget btn, CallbackInfo ci, @Local KeyBinding keyBinding) {
        if (keyBinding instanceof AmecsKeyBinding) {
            ((AmecsKeyBinding) keyBinding).resetKeyBinding();
        } else {
            ((IKeyBinding) keyBinding).amecs$getKeyModifiers().unset();
        }
    }
}
