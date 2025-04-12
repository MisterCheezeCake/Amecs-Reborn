package de.siphalor.amecs.impl;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import de.siphalor.amecs.Amecs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
/*
 * Copyright 2025 MisterCheezeCake
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
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Options for Amecs that are not KeyModifiers
 *
 * @author MisterCheezeCake
 */
@Environment(EnvType.CLIENT)
public class MetaOptions {

    private static final Path OPTIONS_PATH = FabricLoader.getInstance().getConfigDir().resolve("amecs-meta.json");
    private static final File OPTIONS_FILE = OPTIONS_PATH.toFile();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static OptionsSerializable instance;

    public static void load(boolean attempt2) {
        if (!OPTIONS_FILE.exists()) {
            save();
            if (!attempt2) load(true);
        }
        try {
            var content = Files.readString(OPTIONS_PATH);
            var options = GSON.fromJson(content, OptionsSerializable.class);
            if (options == null) {
                Amecs.LOGGER.error("Failed to load meta config, using default and trying to save it");
                instance = new OptionsSerializable();
                save();
            } else {
                instance = options;
            }
        } catch (Exception e) {
            Amecs.LOGGER.error("Failed to load meta config", e);
            instance = new OptionsSerializable();
            if (!attempt2) {
                try {
                    Files.delete(OPTIONS_PATH);
                } catch (IOException ioException) {
                    Amecs.LOGGER.error("Failed to delete corrupted meta config", ioException);
                }
                load(true);
            }
        }
    }

    public static void save() {
        var toSave = get();
        try {
            if (!OPTIONS_FILE.exists()) {
                OPTIONS_FILE.createNewFile();
            }
            var toWrite = toSave.toString();
            FileWriter writer = new FileWriter(OPTIONS_FILE);
            writer.write(toWrite);
            writer.close();
        } catch (Exception e) {
            Amecs.LOGGER.error("Failed to save meta config", e);
        }

    }

    public static OptionsSerializable get() {
        return instance == null ? (instance = new OptionsSerializable()) : instance;
    }

    public static void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
            dispatcher.register(ClientCommandManager.literal("amecs").executes(context -> {
                context.getSource().getClient().send(() -> context.getSource().getClient().setScreen(new MetaOptionsScreen(null)));
                return 1;
            }));


        });
    }


    public static class OptionsSerializable {
        @Expose
        public boolean searchBar = true;

        @Expose
        public boolean skinKeybinds = true;

        @Expose
        public boolean autoJumpKeybind = true;

        @Expose
        public boolean escapeKeybind = true;

        @Override
        public String toString() {
            return GSON.toJson(this);
        }
    }

    public static class MetaOptionsScreen extends Screen {

        private static final Text COLON_SPACE = Text.literal(": ");
        private static final Text ON = ScreenTexts.ON.copy().formatted(Formatting.GREEN);
        private static final Text OFF = ScreenTexts.OFF.copy().formatted(Formatting.RED);

        private static final Text HEADER = Text.translatable("amecs.meta.options.header").styled(it -> it.withBold(true).withColor(Formatting.GOLD));
        private static final Text RESET_NOTICE = Text.translatable("amecs.meta.options.resetNotice")
                .styled(it -> it.withColor(Formatting.RED));
        private final Screen parent;

        private static final int BUTTON_WIDTH = 200;
        private static final int BUTTON_HEIGHT = 20;

        public MetaOptionsScreen(Screen parent) {
            super(Text.translatable("amecs.meta.options.title"));
            this.parent = parent;
        }

        private static Text buttonTextFor(String key) {
            Text base = Text.translatable("amecs.meta.options." + key);
            boolean val;
            switch (key) {
                case "searchBar" -> val = get().searchBar;
                case "skinKeybinds" -> val = get().skinKeybinds;
                case "autoJumpKeybind" -> val = get().autoJumpKeybind;
                case "escapeKeybind" -> val = get().escapeKeybind;
                default -> throw new IllegalArgumentException("Unknown key: " + key);
            }
            return Text.empty().append(base).append(COLON_SPACE).append(val ? ON : OFF);

        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
            context.drawCenteredTextWithShadow(this.textRenderer, HEADER, this.width / 2, 10, 0);
            context.drawCenteredTextWithShadow(this.textRenderer, RESET_NOTICE, this.width / 2, 60, 0);
        }

        @Override
        protected void init() {
            super.init();
            int buttonX = (this.width / 2) - 100;
            this.addDrawableChild(ButtonWidget.builder(buttonTextFor("searchBar"), button -> {
                OptionsSerializable opts = get();
                opts.searchBar = !opts.searchBar;
                button.setMessage(buttonTextFor("searchBar"));
            }).dimensions(buttonX, 30, BUTTON_WIDTH, BUTTON_HEIGHT).build());

            this.addDrawableChild(ButtonWidget.builder(buttonTextFor("skinKeybinds"), button -> {
                OptionsSerializable opts = get();
                opts.skinKeybinds = !opts.skinKeybinds;
                button.setMessage(buttonTextFor("skinKeybinds"));
            }).dimensions(buttonX, 80, BUTTON_WIDTH, BUTTON_HEIGHT).build());

            this.addDrawableChild(ButtonWidget.builder(buttonTextFor("autoJumpKeybind"), button -> {
                OptionsSerializable opts = get();
                opts.autoJumpKeybind = !opts.autoJumpKeybind;
                button.setMessage(buttonTextFor("autoJumpKeybind"));
            }).dimensions(buttonX, 110, BUTTON_WIDTH, BUTTON_HEIGHT).build());

            this.addDrawableChild(ButtonWidget.builder(buttonTextFor("escapeKeybind"), button -> {
                OptionsSerializable opts = get();
                opts.escapeKeybind = !opts.escapeKeybind;
                button.setMessage(buttonTextFor("escapeKeybind"));
            }).dimensions(buttonX, 140, BUTTON_WIDTH, BUTTON_HEIGHT).build());

            this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> {
                this.close();
            }).dimensions(buttonX, this.height - 30, 200, 20).build());


        }

        @Override
        public void close() {
            save();
            MinecraftClient.getInstance().setScreen(parent);
        }
    }
}
