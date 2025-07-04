package io.github.unjoinable.vanille.datapack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.unjoinable.vanille.utils.FileUtils;
import net.minestom.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Handles downloading and loading Minecraft client assets and datapacks.
 */
public class DatapackLoader {
    private static final File FILE = new File("cache/client.jar");
    private static final Logger logger = LoggerFactory.getLogger(DatapackLoader.class);
    private static final String MANIFEST = "https://launchermeta.mojang.com/mc/game/version_manifest.json";

    /**
     * Initializes the datapack loader and starts downloading vanilla assets.
     */
    public DatapackLoader() {
        logger.info("Attempting to load vanilla assets");
        downloadClientJar(MinecraftServer.VERSION_NAME, FILE).thenAccept(jarFile -> {
            logger.info("Vanilla assets loaded successfully for version {}", MinecraftServer.VERSION_NAME);
            scanVanillaDatapackFiles(jarFile);
        });
    }

    /**
     * Downloads the Minecraft version manifest from Mojang.
     * @return CompletableFuture containing the version manifest JSON
     */
    private CompletableFuture<JsonObject> loadManifest() {
        logger.info("Fetching version manifest...");
        return FileUtils.downloadAndParseJson(MANIFEST)
                .thenApply(manifest -> {
                    logger.info("Successfully fetched version manifest.");
                    return manifest;
                });
    }

    /**
     * Finds and returns version metadata for a specific Minecraft version.
     * @param versionId the Minecraft version ID to find
     * @return CompletableFuture containing the version metadata JSON
     * @throws IllegalArgumentException if version ID is not found
     */
    public CompletableFuture<JsonObject> loadVersionMeta(String versionId) {
        return loadManifest().thenApply(manifest -> {
            for (JsonElement el : manifest.getAsJsonArray("versions")) {
                JsonObject obj = el.getAsJsonObject();
                if (versionId.equals(obj.get("id").getAsString())) {
                    logger.info("Found version entry for '{}'", versionId);
                    return obj;
                }
            }
            logger.error("Version ID not found in manifest: {}", versionId);
            throw new IllegalArgumentException("Version ID not found in manifest: " + versionId);
        });
    }

    /**
     * Downloads the complete version JSON for a specific Minecraft version.
     * @param versionId the Minecraft version ID
     * @return CompletableFuture containing the complete version JSON
     */
    public CompletableFuture<JsonObject> loadVersionJson(String versionId) {
        return loadVersionMeta(versionId).thenCompose(versionMeta -> {
            String url = versionMeta.get("url").getAsString();
            logger.info("Fetching version JSON from {}", url);
            return FileUtils.downloadAndParseJson(url)
                    .thenApply(versionJson -> {
                        logger.info("Successfully fetched version JSON for '{}'", versionId);
                        return versionJson;
                    });
        });
    }

    /**
     * Extracts the client JAR download URL for a specific Minecraft version.
     * @param versionId the Minecraft version ID
     * @return CompletableFuture containing the client JAR download URL
     */
    public CompletableFuture<String> loadClientJarUrl(String versionId) {
        return loadVersionJson(versionId)
                .thenApply(DatapackLoader::getClientJarUrl);
    }

    /**
     * Downloads the Minecraft client JAR for a specific version.
     * @param versionId the Minecraft version ID
     * @param targetFile the file location to save the downloaded JAR
     * @return CompletableFuture containing the downloaded File
     */
    public CompletableFuture<File> downloadClientJar(String versionId, File targetFile) {
        return loadClientJarUrl(versionId)
                .thenCompose(jarUrl -> {
                    logger.info("Downloading client jar from {} to {}", jarUrl, targetFile.getAbsolutePath());
                    return FileUtils.downloadFileToLocation(jarUrl, targetFile)
                            .thenApply(file -> {
                                logger.info("Client jar downloaded to {}", file.getAbsolutePath());
                                return file;
                            });
                });
    }

    /**
     * Extracts the client JAR URL from version JSON.
     * @param versionJson the version JSON object
     * @return the client JAR download URL
     */
    private static String getClientJarUrl(JsonObject versionJson) {
        return versionJson
                .getAsJsonObject("downloads")
                .getAsJsonObject("client")
                .get("url")
                .getAsString();
    }

    /**
     * Scans JAR file for vanilla Minecraft datapack files grouped by type asynchronously.
     * @param jarFile the client JAR file to scan
     * @return CompletableFuture containing map of datapack type to file entries, empty if unavailable
     */
    private static CompletableFuture<Map<String, List<ZipEntry>>> scanVanillaDatapackFiles(File jarFile) {
        return CompletableFuture.supplyAsync(() -> {
            if (!jarFile.exists() || !jarFile.canRead()) {
                logger.warn("Client JAR not accessible: {}", jarFile.getAbsolutePath());
                return Map.of();
            }
            
            try (ZipFile zip = new ZipFile(jarFile)) {
                return zip.stream()
                        .filter(entry -> entry.getName().startsWith("data/minecraft/") && !entry.isDirectory())
                        .collect(Collectors.groupingBy(entry -> {
                            String path = entry.getName();
                            String[] parts = path.split("/");
                            return parts.length > 2 ? parts[2] : "unknown";
                        }));
            } catch (IOException e) {
                logger.error("Failed to scan client JAR: {}", jarFile.getAbsolutePath(), e);
                return Map.of();
            }
        });
    }
}