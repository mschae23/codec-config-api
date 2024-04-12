package de.martenschaefer.config.impl;

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
import java.util.function.Function;
import net.fabricmc.loader.api.FabricLoader;
import de.martenschaefer.config.api.ModConfig;
import de.martenschaefer.config.api.exception.ConfigException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class ConfigUtil {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ConfigUtil() {
    }

    @SuppressWarnings("deprecation")
    public static <C extends ModConfig<C>> ModConfig<C> decodeConfig(InputStream input, Codec<ModConfig<C>> codec, DynamicOps<JsonElement> ops) throws IOException, ConfigException {
        try (InputStreamReader reader = new InputStreamReader(new BufferedInputStream(input))) {
            JsonElement element = new JsonParser().parse(reader); // Using this for 1.17 compatibility, would be JsonReader.parseReader in 1.18+

            Either<ModConfig<C>, DataResult.PartialResult<ModConfig<C>>> result = codec.parse(ops, element).get();

            return result.map(Function.identity(), partialResult -> {
                throw new RuntimeException(new ConfigException("Error decoding config: " + partialResult.message()));
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof ConfigException configException) {
                throw configException;
            } else {
                throw e;
            }
        }
    }

    public static <C extends ModConfig<C>> void encodeConfig(Writer writer, Codec<ModConfig<C>> codec, ModConfig<C> config, DynamicOps<JsonElement> ops) throws IOException, ConfigException {
        try {
            DataResult<JsonElement> result = codec.encodeStart(ops, config);

            JsonElement element = result.get().map(Function.identity(), partialResult -> {
                throw new RuntimeException(new ConfigException("Error encoding config: " + partialResult.message()));
            });

            String json = GSON.toJson(element);
            writer.append(json);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof ConfigException configException) {
                throw configException;
            } else {
                throw e;
            }
        }
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
        } else {
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
