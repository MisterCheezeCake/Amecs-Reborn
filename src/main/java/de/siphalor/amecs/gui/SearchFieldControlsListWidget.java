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
package de.siphalor.amecs.gui;

import de.siphalor.amecs.Amecs;
import de.siphalor.amecs.impl.duck.IKeyBindingEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author Siphalor
 */
@Environment(EnvType.CLIENT)
public class SearchFieldControlsListWidget extends ControlsListWidget.Entry {
	protected MinecraftClient minecraft;

	private final TextFieldWidget textFieldWidget;

	private int lastEntryCount = 0;
	private final Set<ControlsListWidget.KeyBindingEntry> entries = new TreeSet<>(Comparator.comparing(o -> ((IKeyBindingEntry) o).amecs$getKeyBinding()));

	public SearchFieldControlsListWidget(MinecraftClient client) {
		minecraft = client;
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		assert minecraft.currentScreen != null;

		textFieldWidget = new TextFieldWidget(textRenderer, minecraft.currentScreen.width / 2 - 125, 0, 250, 20, Text.empty());
		textFieldWidget.setSuggestion(I18n.translate("amecs.search.placeholder"));
		textFieldWidget.setChangedListener(searchText -> {
			ControlsListWidget listWidget = null;
			for (Element child : client.currentScreen.children()) {
				if (child instanceof ControlsListWidget) {
					listWidget = (ControlsListWidget) child;
					break;
				}
			}
			assert listWidget != null;

			if (searchText.isEmpty()) {
				textFieldWidget.setSuggestion(I18n.translate("amecs.search.placeholder"));
			} else {
				textFieldWidget.setSuggestion("");
			}

			searchText = searchText.trim();

			//? if >=1.21.4 {
			listWidget.setScrollY(0);
			//?} else
			/*listWidget.setScrollAmount(0);*/

			List<ControlsListWidget.Entry> children = listWidget.children();
			if (entries.isEmpty()) {
				for (ControlsListWidget.Entry entry : children) {
					if (entry instanceof ControlsListWidget.KeyBindingEntry) {
						entries.add((ControlsListWidget.KeyBindingEntry) entry);
					}
				}
				lastEntryCount = children.size();
			}
			int childrenCount = children.size();
			if (childrenCount != lastEntryCount) {
				Amecs.LOGGER.info("Controls search results changed externally - recompiling the list!");
				try {
					//noinspection JavaReflectionMemberAccess
					Constructor<ControlsListWidget.KeyBindingEntry> c = ControlsListWidget.KeyBindingEntry.class.getDeclaredConstructor(
							ControlsListWidget.class, KeyBinding.class, Text.class
					);
					c.setAccessible(true);
					entries.clear();
					KeyBinding[] keyBindings = client.options.allKeys;
					Arrays.sort(keyBindings);
					String lastCat = null;
					ControlsListWidget.KeyBindingEntry entry;
					lastEntryCount = 1;
					for (KeyBinding keyBinding : keyBindings) {
						if (!Objects.equals(lastCat, keyBinding.getCategory())) {
							lastCat = keyBinding.getCategory();
							children.add(listWidget.new CategoryEntry(Text.translatable(keyBinding.getCategory())));
							lastEntryCount++;
						}
						entry = c.newInstance(listWidget, keyBinding, Text.translatable(keyBinding.getTranslationKey()));
						children.add(entry);
						entries.add(entry);
						lastEntryCount++;
					}
				} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
					Amecs.LOGGER.error("An unexpected exception occured during recompilation of controls list!", e);
				}
			}

			children.clear();

			children.add(this);

			String keyFilter = null;
			int keyDelimiterPos = searchText.indexOf('=');
			if (keyDelimiterPos == 0) {
				keyFilter = searchText.substring(1).trim();
				searchText = null;
			} else if (keyDelimiterPos > 0) {
				keyFilter = searchText.substring(keyDelimiterPos + 1).trim();
				searchText = searchText.substring(0, keyDelimiterPos).trim();
			}

			final boolean nmuk = FabricLoader.getInstance().isModLoaded("nmuk");
			String lastCat = null;
			boolean lastMatched = false;
			boolean includeCat = false;
			lastEntryCount = 1;
			for (ControlsListWidget.KeyBindingEntry entry : entries) {
				KeyBinding binding = ((IKeyBindingEntry) entry).amecs$getKeyBinding();
//				if (nmuk && lastMatched && NMUKProxy.isAlternative(binding)) {
//					children.add(entry);
//					lastEntryCount++;
//					continue;
//				}

				final String cat = binding.getCategory();
				if (!cat.equals(lastCat)) {
					includeCat = StringUtils.containsIgnoreCase(I18n.translate(cat), searchText);
				}
				if (
						(
								includeCat
										|| searchText == null
										|| StringUtils.containsIgnoreCase(I18n.translate(((IKeyBindingEntry) entry).amecs$getKeyBinding().getTranslationKey()), searchText)
						) && Amecs.entryKeyMatches(entry, keyFilter)
				) {
					if (!cat.equals(lastCat)) {
						children.add(listWidget.new CategoryEntry(Text.translatable(cat)));
						lastCat = cat;
						lastEntryCount++;
					}
					children.add(entry);
					lastEntryCount++;
					lastMatched = true;
				} else {
					lastMatched = false;
				}
			}
			if (lastEntryCount <= 1) {
				MutableText noResultsText = Text.translatable(Amecs.KEY_PREFIX + ".search.no_results");
				noResultsText.setStyle(noResultsText.getStyle().withColor(Formatting.GRAY));
				children.add(listWidget.new CategoryEntry(noResultsText));
			}
		});
	}

	@Override
	public List<? extends Element> children() {
		return Collections.singletonList(textFieldWidget);
	}

	@Override
	public boolean mouseClicked(double double_1, double double_2, int int_1) {
		return textFieldWidget.mouseClicked(double_1, double_2, int_1);
	}

	@Override
	public boolean mouseReleased(double double_1, double double_2, int int_1) {
		return textFieldWidget.mouseReleased(double_1, double_2, int_1);
	}

	@Override
	public boolean keyPressed(int int_1, int int_2, int int_3) {
		return textFieldWidget.keyPressed(int_1, int_2, int_3);
	}

	@Override
	public boolean charTyped(char char_1, int int_1) {
		return textFieldWidget.charTyped(char_1, int_1);
	}

	@Override
	public void setFocused(boolean boolean_1) {
		textFieldWidget.setFocused(boolean_1);
	}

	@Override
	public void render(DrawContext drawContext, int var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8, float var9) {
		textFieldWidget.setY(var2);
		textFieldWidget.render(drawContext, var6, var7, var9);
	}

	@Override
	public List<? extends Selectable> selectableChildren() {
		return Collections.singletonList(textFieldWidget);
	}

	@Override
	protected void update() {}

}
