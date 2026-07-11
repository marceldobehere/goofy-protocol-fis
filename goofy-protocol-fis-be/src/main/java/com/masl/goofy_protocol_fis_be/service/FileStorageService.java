package com.masl.goofy_protocol_fis_be.service;

import com.masl.goofy_protocol_fis_be.properties.StorageProperties;
import lombok.Getter;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

// TODO: Test
@Service
public class FileStorageService {
    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    @Getter
    private static FileStorageService singleton;

    private final StorageProperties storageProperties;

    private final Path userDbPath;
    private final Path userBucketPath;

    public FileStorageService(StorageProperties storageProperties) throws IOException {
        this.storageProperties = storageProperties;
        if (storageProperties.getUseTempDir()) {
            String tmpLoc = System.getProperty("java.io.tmpdir") + "/goofy-fis-" + UUID.randomUUID();
            userDbPath = Path.of(tmpLoc, storageProperties.getBaseUserDatabasesPath());
            userBucketPath = Path.of(tmpLoc, storageProperties.getBaseUserBucketsPath());
        } else {
            userDbPath = Path.of(storageProperties.getBaseUserDatabasesPath());
            userBucketPath = Path.of(storageProperties.getBaseUserBucketsPath());
        }

        init();
        singleton = this;
        log.info("FileStorageService initialized with userDbPath: {}, userBucketPath: {}", userDbPath, userBucketPath);
    }

    private void init() throws IOException {
        if (storageProperties.getCreateDirectories()) {
            Files.createDirectories(userDbPath);
            Files.createDirectories(userBucketPath);
        } else {
            if (!Files.exists(userDbPath) || !Files.isDirectory(userDbPath))
                throw new IOException("User databases path does not exist or is not a directory: " + userDbPath);
            if (!Files.exists(userBucketPath) || !Files.isDirectory(userBucketPath))
                throw new IOException("User buckets path does not exist or is not a directory: " + userBucketPath);
        }
    }

    public Path getDbFolderPath(String identityUuid) {
        return resolvePathSafely(userDbPath, identityUuid);
    }
    public Path getBucketFolderPath(String identityUuid) {
        return resolvePathSafely(userBucketPath, identityUuid);
    }

    public void createDbFolder(String identityUuid) throws IOException {
        Path path = getDbFolderPath(identityUuid);
        Files.createDirectories(path);
    }
    public void createBucketFolder(String identityUuid) throws IOException {
        Path path = getBucketFolderPath(identityUuid);
        Files.createDirectories(path);
    }

    public void deleteDbFolder(String identityUuid) throws IOException {
        Path path = getDbFolderPath(identityUuid);
        FileUtils.deleteDirectory(path.toFile());
    }
    public void deleteBucketFolder(String identityUuid) throws IOException {
        Path path = getBucketFolderPath(identityUuid);
        FileUtils.deleteDirectory(path.toFile());
    }

    public void zipDbFolder(String identityUuid, ZipOutputStream stream) throws IOException {
        Path path = getDbFolderPath(identityUuid);
        zipPath(path, stream);
    }
    public void zipBucketFolder(String identityUuid, ZipOutputStream stream) throws IOException {
        Path path = getBucketFolderPath(identityUuid);
        zipPath(path, stream);
    }

    public void importDbZip(String identityUuid, ZipInputStream stream) throws IOException {
        Path path = getDbFolderPath(identityUuid);
        FileUtils.deleteDirectory(path.toFile());
        Files.createDirectories(path);
        unzipPath(path, stream);
    }
    public void importBucketZip(String identityUuid, ZipInputStream stream) throws IOException {
        Path path = getBucketFolderPath(identityUuid);
        FileUtils.deleteDirectory(path.toFile());
        Files.createDirectories(path);
        unzipPath(path, stream);
    }



    private Path resolvePathSafely(Path basePath, String subPathStr) {
        Path path = basePath.resolve(subPathStr).normalize();

        if (!path.startsWith(basePath))
            throw new IllegalArgumentException("Invalid path: " + subPathStr);

        return path;
    }

    private void zipPath(Path sourceDirPath, ZipOutputStream zipStream) throws IOException {
        try (zipStream) {
            Files.walk(sourceDirPath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourceDirPath.relativize(path).toString());
                        try {
                            zipStream.putNextEntry(zipEntry);
                            Files.copy(path, zipStream);
                            zipStream.closeEntry();
                        } catch (IOException e) {
                            log.error("Error while zipping file: {}", path, e);
                        }
                    });
        }
    }

    private void unzipPath(Path destDirPath, ZipInputStream zipStream) throws IOException {
        try (zipStream) {
            for (ZipEntry ze; (ze = zipStream.getNextEntry()) != null; ) {
                Path resolvedPath = destDirPath.resolve(ze.getName()).normalize();
                if (!resolvedPath.startsWith(destDirPath)) {
                    log.error("Invalid zip entry: {}. Skipping.", ze.getName());
                    continue;
                }

                if (ze.isDirectory()) {
                    Files.createDirectories(resolvedPath);
                    continue;
                }

                Files.createDirectories(resolvedPath.getParent());
                Files.copy(zipStream, resolvedPath);
            }
        }
    }
}
