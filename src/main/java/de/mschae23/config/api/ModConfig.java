/*
 * Copyright (C) 2024  mschae23
 *
 * This file is part of Codec config API.
 *
 * Codec config API is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.mschae23.config.api;

import java.util.function.IntFunction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

/**
 * Represents a mod's config at a specific version.
 *
 * @param <C> the config type with the latest version
 */
public interface ModConfig<C extends ModConfig<C>> {
    /**
     * Returns the {@link ModConfig.Type} for this config's version.
     *
     * @return the {@code Type} instance
     */
    Type<C, ?> type();

    /**
     * {@return the config version}
     */
    default int version() {
        return type().version();
    }

    /**
     * Updates this config to the latest version.
     *
     * @return the updated config
     */
    C latest();

    /**
     * {@return Whether the config should be automatically updated}
     */
    boolean shouldUpdate();

    /**
     * Creates a {@code Codec} for the mod's config. It will have a {@code version} field that will be used
     * to decide which {@code ModConfig} type to deserialize into, using {@code getType}.
     *
     * @param latestVersion the latest config version
     * @param getType       a function that returns a {@code Type<C, ?>} instance based on the config version
     * @param <C>           the config type with version {@code latestVersion}
     * @return the created {@code Codec}
     */
    static <C extends ModConfig<C>> Codec<ModConfig<C>> createCodec(int latestVersion, IntFunction<Type<C, ?>> getType) {
        return Type.createCodec(latestVersion, getType).dispatch("version", ModConfig::type, Type::codec);
    }

    /**
     * {@code ModConfig.Type} contains metadata like the config version and codec. There should only be one instance
     * of this per config version.
     *
     * @param version the config version
     * @param codec   the {@code MapCodec} used to serialize and deserialize configs in this version
     * @param <C>     the config type with the latest version
     * @param <T>     the config type with version {@code version}
     */
    record Type<C extends ModConfig<C>, T extends ModConfig<C>>(int version, MapCodec<? extends T> codec) {
        /**
         * Creates a {@code Codec} for {@code ModConfig.Type}. It will serialize as an integer in the range {@code 1}
         * and {@code latestVersion} (inclusive). To decide which {@code Type} instance should be returned, the
         * {@code getType} parameter will be called with this integer on deserialization.
         *
         * @param latestVersion the latest config version
         * @param getType       a function that returns a {@code Type<C, ?>} instance based on the config version
         * @param <C>           the config type with version {@code latestVersion}
         * @return the created {@code Codec}
         */
        public static <C extends ModConfig<C>> Codec<Type<C, ?>> createCodec(int latestVersion, IntFunction<Type<C, ?>> getType) {
            return Codec.intRange(1, latestVersion).xmap(getType::apply, Type::version);
        }
    }
}
