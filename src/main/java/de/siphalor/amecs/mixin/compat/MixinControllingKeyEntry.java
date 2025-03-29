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

import com.blamejared.controlling.client.NewKeyBindsList;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import de.siphalor.amecs.Amecs;
import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import de.siphalor.amecs.impl.duck.IKeyBindingEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author MisterCheezeCake
 */
@IfModLoaded("controlling")
@Mixin(NewKeyBindsList.KeyEntry.class)
public abstract class MixinControllingKeyEntry implements IKeyBindingEntry {

    @Unique
    private static final String DESCRIPTION_SUFFIX = "." + Amecs.KEY_PREFIX + ".description";


    @Shadow @Final private KeyBinding key;
    @Shadow @Final private ButtonWidget btnChangeKeyBinding;
    @Unique
    private List<Text> description;

    @Inject(method = "lambda$new$0(Lnet/minecraft/client/option/KeyBinding;Lnet/minecraft/client/gui/widget/ButtonWidget;)V", at = @At("HEAD"))
    public void onEditButtonClicked(KeyBinding keyBinding, ButtonWidget buttonWidget, CallbackInfo callbackInfo) {
        ((IKeyBinding) key).amecs$getKeyModifiers().unset();
        key.setBoundKey(InputUtil.UNKNOWN_KEY);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(NewKeyBindsList this$0, KeyBinding key, Text keyDesc, CallbackInfo ci) {
        String descriptionKey = key.getTranslationKey() + DESCRIPTION_SUFFIX;
        if (I18n.hasTranslation(descriptionKey)) {
            String[] lines = StringUtils.split(I18n.translate(descriptionKey), '\n');
            description = new ArrayList<>(lines.length);
            for (String line : lines) {
                description.add(Text.literal(line));
            }
        } else {
            description = null;
        }
    }

    @Inject(
            method = "lambda$new$2",
            at = @At(value = "INVOKE", target = "Lcom/blamejared/controlling/client/NewKeyBindsList;update()V")
    )
    public void onResetButtonClicked(KeyBinding keyBinding, ButtonWidget buttonWidget, CallbackInfo callbackInfo) {
        ((IKeyBinding) key).amecs$getKeyModifiers().unset();
        if (key instanceof AmecsKeyBinding)
            ((AmecsKeyBinding) key).resetKeyBinding();
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void onRendered(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float delta, CallbackInfo callbackInfo) {
        if (description != null && mouseY >= y && mouseY < y + entryHeight && mouseX < btnChangeKeyBinding.getX()) {
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, description, mouseX, mouseY);
        }
    }

    @Override
    public KeyBinding amecs$getKeyBinding() {
        return key;
    }

}
