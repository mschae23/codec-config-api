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

package de.mschae23.config.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Path;
import java.util.function.Consumer;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import de.mschae23.config.api.exception.ConfigException;
import de.mschae23.config.impl.ConfigIoImpl;
import org.apache.logging.log4j.Logger;

public final class ConfigIo {
    private ConfigIo() {
    }

    /**
     * <p>If the config file is present, this method will read and decode it. If successful, this config will be returned.</p>
     *
     * <p>If the config file does not exist, {@code latestDefault} will be encoded and written to the file if possible.
     * That default will then be returned.</p>
     *
     * @param configName    path to the config file. This is relative to the config directory, so it should only contain the file name.
     * @param latestDefault the default config. Will be used in case of IO errors, decoding errors, or if the file doesn't exist.
     * @param codec         the {@link Codec} to use. Should be created by {@link ModConfig#createCodec}.
     * @param ops           the {@link DynamicOps} to use. Can usually just be {@link JsonOps#INSTANCE}.
     * @param logger        a {@link Logger} that will be used for log messages.
     * @param <C>           the latest config data type.
     * @return the config of type {@code C}.
     */
    public static <C extends ModConfig<C>> C initializeConfig(Path configName, C latestDefault, Codec<ModConfig<C>> codec, DynamicOps<JsonElement> ops, Logger logger) {
        return initializeConfig(configName, -1, latestDefault, codec, ops, logger);
    }

    /**
     * <p>If the config file is present, this method will read and decode it. If successful, this config will be returned.</p>
     *
     * <p>If the config file does not exist, {@code latestDefault} will be encoded and written to the file if possible.
     * That default will then be returned.</p>
     *
     * @param configName    path to the config file. This is relative to the config directory, so it should only contain the file name.
     * @param latestDefault the default config. Will be used in case of IO errors, decoding errors, or if the file doesn't exist.
     * @param codec         the {@link Codec} to use. Should be created by {@link ModConfig#createCodec}.
     * @param ops           the {@link DynamicOps} to use. Can usually just be {@link JsonOps#INSTANCE}.
     * @param logInfo       a {@link Consumer} that will be used for info log messages.
     * @param logError      a {@link Consumer} that will be used for error log messages.
     * @param <C>           the latest config data type.
     * @return the config of type {@code C}.
     */
    public static <C extends ModConfig<C>> C initializeConfig(Path configName, C latestDefault, Codec<ModConfig<C>> codec, DynamicOps<JsonElement> ops, Consumer<String> logInfo, Consumer<String> logError) {
        return ConfigIoImpl.initializeConfig(configName, -1, latestDefault, codec, ops, logInfo, logError);
    }

    /**
     * <p>If the config file is present, this method will read and decode it. If successful, this config will be returned.
     * In the case that the decoded config file is outdated (uses a version which is lower than {@code latestVersion}),
     * an updated version will be encoded and written to the file.</p>
     *
     * <p>If the config file does not exist, {@code latestDefault} will be encoded and written to the file if possible.
     * That default will then be returned.</p>
     *
     * @param configName    path to the config file. This is relative to the config directory, so it should only contain the file name.
     * @param latestVersion the latest version of the config. Can be set to {@code -1} to disable automatic config file update.
     * @param latestDefault the default config. Will be used in case of IO errors, decoding errors, or if the file doesn't exist.
     * @param codec         the {@link Codec} to use. Should be created by {@link ModConfig#createCodec}.
     * @param ops           the {@link DynamicOps} to use. Can usually just be {@link JsonOps#INSTANCE}.
     * @param logger        a {@link Logger} that will be used for log messages.
     * @param <C>           the latest config data type.
     * @return the config of type {@code C}.
     */
    public static <C extends ModConfig<C>> C initializeConfig(Path configName, int latestVersion, C latestDefault, Codec<ModConfig<C>> codec, DynamicOps<JsonElement> ops, Logger logger) {
        return initializeConfig(configName, latestVersion, latestDefault, codec, ops, logger::info, logger::error);
    }

    /**
     * <p>If the config file is present, this method will read and decode it. If successful, this config will be returned.
     * In the case that the decoded config file is outdated (uses a version which is lower than {@code latestVersion}),
     * an updated version will be encoded and written to the file.</p>
     *
     * <p>If the config file does not exist, {@code latestDefault} will be encoded and written to the file if possible.
     * That default will then be returned.</p>
     *
     * @param configName    path to the config file. This is relative to the config directory, so it should only contain the file name.
     * @param latestVersion the latest version of the config. Can be set to {@code -1} to disable automatic config file update.
     * @param latestDefault the default config. Will be used in case of IO errors, decoding errors, or if the file doesn't exist.
     * @param codec         the {@link Codec} to use. Should be created by {@link ModConfig#createCodec}.
     * @param ops           the {@link DynamicOps} to use. Can usually just be {@link JsonOps#INSTANCE}.
     * @param logInfo       a {@link Consumer} that will be used for info log messages.
     * @param logError      a {@link Consumer} that will be used for error log messages.
     * @param <C>           the latest config data type.
     * @return the config of type {@code C}.
     */
    public static <C extends ModConfig<C>> C initializeConfig(Path configName, int latestVersion, C latestDefault, Codec<ModConfig<C>> codec, DynamicOps<JsonElement> ops, Consumer<String> logInfo, Consumer<String> logError) {
        return ConfigIoImpl.initializeConfig(configName, latestVersion, latestDefault, codec, ops, logInfo, logError);
    }

    /**
     * Decodes a config from an {@link InputStream} using the provided {@code Codec} and
     * {@code DynamicOps<JsonElement>}.
     *
     * @param input the {@code InputStream} to read from.
     * @param codec the {@code Codec} that will decode the input.
     * @param ops   the {@link DynamicOps} to use. Can usually just be {@link JsonOps#INSTANCE}.
     * @param <C>   the config data type.
     * @return the decoded config of type {@code ModConfig<C>}
     * @throws IOException     if there is a problem reading or parsing the input
     * @throws ConfigException if there is a problem decoding the config
     */
    public static <C extends ModConfig<C>> ModConfig<C> decodeConfig(InputStream input, Codec<ModConfig<C>> codec, DynamicOps<JsonElement> ops) throws IOException, ConfigException {
        return ConfigIoImpl.decodeConfig(input, codec, ops);
    }

    /**
     * Encodes a config into a {@link Writer} using the provided {@code Codec} and
     * {@code DynamicOps<JsonElement>}.
     *
     * @param writer the {@code Writer} to write to.
     * @param codec  the {@code Codec} that will encode the input.
     * @param config the config to encode
     * @param ops    the {@link DynamicOps} to use. Can usually just be {@link JsonOps#INSTANCE}.
     * @param <C>    the config data type.
     * @throws IOException     if there is a problem writing to the output
     * @throws ConfigException if there is a problem encoding the config
     */
    public static <C extends ModConfig<C>> void encodeConfig(Writer writer, Codec<ModConfig<C>> codec, ModConfig<C> config, DynamicOps<JsonElement> ops) throws IOException, ConfigException {
        ConfigIoImpl.encodeConfig(writer, codec, config, ops);
    }
}
