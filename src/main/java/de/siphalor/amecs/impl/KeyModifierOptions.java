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

package de.siphalor.amecs.impl;

import de.siphalor.amecs.Amecs;
import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.jetbrains.annotations.ApiStatus;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Siphalor
 */
@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public class KeyModifierOptions {
	private static final String KEY_MODIFIERS_PREFIX = "key_modifiers_";
	/**
	 * Called amecsapi options for legacy compat reasons
	 * TODO: Migrate this, change name at least. JSON format? Use the SMKB one?
	 */
	private static final File optionsFile = new File(MinecraftClient.getInstance().runDirectory, "options." + "amecsapi" + ".txt");

	private KeyModifierOptions() {}

	public static void write(KeyBinding[] allKeyBindings) {
		List<KeyBinding> bindingsWithChangedModifiers = new ArrayList<>(allKeyBindings.length);
		for (KeyBinding keyBinding : allKeyBindings) {
			if (!KeyBindingUtils.getDefaultModifiers(keyBinding).equals(KeyBindingUtils.getBoundModifiers(keyBinding))) {
				bindingsWithChangedModifiers.add(keyBinding);
			}
		}

		if (bindingsWithChangedModifiers.isEmpty()) {
			if (optionsFile.exists()) {
				try {
					Files.delete(optionsFile.toPath());
				} catch (IOException e) {
					Amecs.LOGGER.error("Failed to cleanup Amecs modifier file - weird.", e);
				}
			}
			return;
		}

		try (PrintWriter writer = new PrintWriter(new FileOutputStream(optionsFile))) {
			KeyModifiers modifiers;
			for (KeyBinding binding : bindingsWithChangedModifiers) {
				modifiers = KeyBindingUtils.getBoundModifiers(binding);
				writer.println(KEY_MODIFIERS_PREFIX + binding.getTranslationKey() + ":" + modifiers.serializeValue());
			}
		} catch (FileNotFoundException e) {
			Amecs.LOGGER.error("Failed to save Amecs modifiers to options file", e);
		}
	}

	public static void read() {
		if (!optionsFile.exists()) {
			return;
		}
		try (BufferedReader reader = new BufferedReader(new FileReader(optionsFile))) {
			String line;
			while ((line = reader.readLine()) != null) {
				readLine(line);
			}
		} catch (IOException e) {
			Amecs.LOGGER.error("Failed to load Amecs modifier options file", e);
		}
	}

	private static void readLine(String line) {
		try {
			int colon = line.indexOf(':');
			if (colon <= 0) {
				Amecs.LOGGER.warn("Invalid line in Amecs modifier options file: {}", line);
				return;
			}
			String id = line.substring(0, colon);
			if (!id.startsWith(KEY_MODIFIERS_PREFIX)) {
				Amecs.LOGGER.warn("Invalid entry in Amecs modifier options file: {}", id);
				return;
			}
			id = id.substring(KEY_MODIFIERS_PREFIX.length());
			KeyBinding keyBinding = KeyBindingUtils.getIdToKeyBindingMap().get(id);
			if (keyBinding == null) {
				Amecs.LOGGER.warn("Unknown keybinding identifier in Amecs modifier options file: {}", id);
				return;
			}

			KeyModifiers modifiers = new KeyModifiers(KeyModifiers.deserializeValue(line.substring(colon + 1)));
			if (keyBinding.isUnbound()) {
				if (!modifiers.isUnset()) {
					Amecs.LOGGER.warn("Found modifiers for unbound keybinding in Amecs modifier options file. Ignoring them: {}", id);
				}
				return;
			}
			((IKeyBinding) keyBinding).amecs$getKeyModifiers().copyModifiers(modifiers);
		} catch (Throwable e) {
			Amecs.LOGGER.error("Invalid line in Amecs modifier options file: {}", line, e);
		}
	}
}
