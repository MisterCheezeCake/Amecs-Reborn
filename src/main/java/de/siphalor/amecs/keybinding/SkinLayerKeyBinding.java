package de.siphalor.amecs.keybinding;
/*
 * Copyright 2020-2023 Siphalor
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
import de.siphalor.amecs.Amecs;
import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyModifiers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.Identifier;

/**
 * @author Siphalor
 */
public class SkinLayerKeyBinding extends AmecsKeyBinding {
	private final PlayerModelPart playerModelPart;

	public SkinLayerKeyBinding(Identifier id, InputUtil.Type type, int code, String category, PlayerModelPart playerModelPart) {
		super(id, type, code, category, new KeyModifiers());
		this.playerModelPart = playerModelPart;
	}

	@Override
	public void onPressed() {
		MinecraftClient client = MinecraftClient.getInstance();
		//? if >1.21.1 {
		client.options.setPlayerModelPart(playerModelPart, !client.options.isPlayerModelPartEnabled(playerModelPart));
		//?} else
		/*client.options.togglePlayerModelPart(playerModelPart, !client.options.isPlayerModelPartEnabled(playerModelPart));*/
		Amecs.sendToggleMessage(client.player, client.options.isPlayerModelPartEnabled(playerModelPart), playerModelPart.getOptionName());
	}
}
