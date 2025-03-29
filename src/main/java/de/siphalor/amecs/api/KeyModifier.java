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

package de.siphalor.amecs.api;

import de.siphalor.amecs.Amecs;
import org.apache.commons.lang3.ArrayUtils;

import de.siphalor.amecs.impl.ModifierPrefixTextProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.InputUtil;

/**
 * @author Klotzi111, Siphalor
 */
@SuppressWarnings("WeakerAccess")
@Environment(EnvType.CLIENT)
public enum KeyModifier {
	// the order of the enums makes a difference when generating the shown name in the gui
	// with this order the old text order is preserved. But now the id values do not increment nicely. But changing them would eliminate
	// backward compatibility with the old save format
	NONE("none", -1),
	ALT("alt", 0, 342, 346),
	SHIFT("shift", 2, 340, 344),
	CONTROL("control", 1, 341, 345),
	SUPER("super", 3, 343, 347);


	// using this array for the values because it is faster than calling values() every time
	public static final KeyModifier[] VALUES = KeyModifier.values();

	public final String stringID;
	public final String name;
	public final int id;
	public final ModifierPrefixTextProvider textProvider;
	// these keyCodes are all from Type: InputUtil.Type.KEYSYM
	final int[] keyCodes;

	KeyModifier(String stringID, int id, int... keyCodes) {
		this.stringID = stringID;
		this.id = id;
		textProvider = new ModifierPrefixTextProvider(this);
		this.keyCodes = keyCodes;
		boolean isMac = Amecs.isMac();
		this.name = switch (this) {
            case NONE -> "none";
            case ALT -> isMac ? "option" : "alt";
			case SHIFT -> "shift";
			case CONTROL -> "control";
			case SUPER -> isMac ? "command" : "super";
		};
	}

	public static KeyModifier fromKeyCode(int keyCode) {
		for (KeyModifier keyModifier : VALUES) {
			if (keyModifier == NONE) {
				continue;
			}
			if (keyModifier.matches(keyCode)) {
				return keyModifier;
			}
		}
		return NONE;
	}

	public static KeyModifier fromKey(InputUtil.Key key) {
		if (key == null || key.getCategory() != InputUtil.Type.KEYSYM) {
			return NONE;
		}
		return fromKeyCode(key.getCode());
	}

	public boolean matches(int keyCode) {
		return ArrayUtils.contains(keyCodes, keyCode);
	}

	public String getTranslationKey() {
		return Amecs.KEY_PREFIX+  ".modifier." + stringID;
	}

	public static int getModifierCount() {
		return VALUES.length - 1; // remove 1 for NONE
	}
}
