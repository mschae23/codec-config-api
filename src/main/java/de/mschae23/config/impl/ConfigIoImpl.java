/*
 * Copyright (C) 2024  mschae23
 *
 * This file is part of Codec config API.
 *
 * Codec config API is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.mschae23.config.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;
import net.fabricmc.loader.api.FabricLoader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import de.mschae23.config.api.ModConfig;
import de.mschae23.config.api.exception.ConfigException;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class ConfigIoImpl {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ConfigIoImpl() {
    }

    public static <C> C decodeConfig(InputStream input, Codec<C> codec, DynamicOps<JsonElement> ops) throws IOException, ConfigException {
        try (InputStreamReader reader = new InputStreamReader(new BufferedInputStream(input))) {
            JsonElement element = JsonParser.parseReader(reader);

            return codec.parse(ops, element).getOrThrow(message ->
                new ConfigException("Error decoding config: " + message));
        }
    }

    public static <C> void encodeConfig(Writer writer, Codec<C> codec, C config, DynamicOps<JsonElement> ops) throws IOException, ConfigException {
        JsonElement element = codec.encodeStart(ops, config).getOrThrow(message ->
            new ConfigException("Error encoding config: " + message));

        String json = GSON.toJson(element);
        writer.append(json);
    }

    public static <C extends ModConfig<C>> C initializeConfig(Path configName, int latestVersion, C latestDefault, Codec<ModConfig<C>> codec, DynamicOps<JsonElement> ops, Consumer<String> logInfo, Consumer<String> logError) {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(configName);
        C latestConfig = latestDefault;

        if (Files.exists(configPath) && Files.isRegularFile(configPath)) {
            try (InputStream input = Files.newInputStream(configPath)) {
                logInfo.accept("Reading config.");

                ModConfig<C> config = decodeConfig(input, codec, ops);
                latestConfig = config.latest();

                if (config.shouldUpdate() && config.version() < latestVersion) {
                    // Default OpenOptions are CREATE, TRUNCATE_EXISTING, and WRITE
                    try (OutputStream output = Files.newOutputStream(configPath);
                         OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(output))) {
                        logInfo.accept("Writing updated config.");

                        encodeConfig(writer, codec, config.latest(), ops);
                    } catch (IOException e) {
                        logError.accept("IO exception while trying to write updated config: " + e.getLocalizedMessage());
                    } catch (ConfigException e) {
                        logError.accept(e.getLocalizedMessage());
                    }
                }
            } catch (IOException e) {
                logError.accept("IO exception while trying to read config: " + e.getLocalizedMessage());
            } catch (ConfigException e) {
                logError.accept(e.getLocalizedMessage());
            }
        } else if (latestVersion != -1) {
            // Write default config if the file doesn't exist, unless latestVersion was set to -1
            try (OutputStream output = Files.newOutputStream(configPath, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                 OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(output))) {
                logInfo.accept("Writing default config.");

                encodeConfig(writer, codec, latestDefault, ops);
            } catch (IOException e) {
                logError.accept("IO exception while trying to write config: " + e.getLocalizedMessage());
            } catch (ConfigException e) {
                logError.accept(e.getLocalizedMessage());
            }
        }

        return latestConfig;
    }
}
