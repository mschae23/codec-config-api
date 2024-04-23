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

public interface ModConfig<C extends ModConfig<C>> {
    Type<C, ?> type();

    default int version() {
        return type().version();
    }

    C latest();

    boolean shouldUpdate();

    static <C extends ModConfig<C>> Codec<ModConfig<C>> createCodec(int latestVersion, IntFunction<Type<C, ?>> getType) {
        return Type.createCodec(latestVersion, getType).dispatch("version", ModConfig::type, Type::codec);
    }

    record Type<C extends ModConfig<C>, T extends ModConfig<C>>(int version, MapCodec<? extends T> codec) {
        public static <C extends ModConfig<C>> Codec<Type<C, ?>> createCodec(int latestVersion, IntFunction<Type<C, ?>> getType) {
            return Codec.intRange(1, latestVersion).xmap(getType::apply, Type::version);
        }
    }
}
