package de.martenschaefer.config.api;

import java.util.function.IntFunction;
import com.mojang.serialization.Codec;

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

    record Type<C extends ModConfig<C>, T extends C>(int version, Codec<? extends T> codec) {
        public static <C extends ModConfig<C>> Codec<Type<C, ?>> createCodec(int latestVersion, IntFunction<Type<C, ?>> getType) {
            return Codec.intRange(1, latestVersion).xmap(getType::apply, Type::version);
        }
    }
}
