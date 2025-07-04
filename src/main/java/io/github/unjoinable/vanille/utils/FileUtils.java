package io.github.unjoinable.vanille.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

/**
 * Utility class for file operations including downloading JSON and files asynchronously.
 */
public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    private FileUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Downloads JSON content from a URL and parses it asynchronously.
     * @param url the URL to download JSON from
     * @return CompletableFuture containing the parsed JsonObject
     * @throws IllegalStateException if download or JSON parsing fails
     */
    public static CompletableFuture<JsonObject> downloadAndParseJson(String url) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Starting JSON download from: {}", url);
            try (InputStream input = URI.create(url).toURL().openStream();
                 InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                JsonObject result = JsonParser.parseReader(reader).getAsJsonObject();
                logger.debug("Successfully downloaded and parsed JSON from: {}", url);
                return result;
            } catch (Exception e) {
                logger.error("Failed to download JSON from: {}", url, e);
                throw new IllegalStateException("Failed to download JSON from " + url, e);
            }
        });
    }

    /**
     * Downloads a file from a URL and saves it to the specified location asynchronously.
     * @param url the URL to download the file from
     * @param output the target file location where the downloaded file will be saved
     * @return CompletableFuture containing the output File after successful download
     * @throws IllegalStateException if download or file writing fails
     */
    public static CompletableFuture<File> downloadFileToLocation(String url, File output) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Starting file download from: {} to: {}", url, output.getAbsolutePath());
            try (InputStream in = URI.create(url).toURL().openStream()) {
                Files.copy(in, output.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                logger.debug("Successfully downloaded file from: {} to: {}", url, output.getAbsolutePath());
                return output;
            } catch (Exception e) {
                logger.error("Failed to download file from: {} to: {}", url, output.getAbsolutePath(), e);
                throw new IllegalStateException("Failed to download file from " + url, e);
            }
        });
    }
}
