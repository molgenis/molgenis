package org.molgenis.util.file;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zeroturnaround.zip.commons.FileUtils;

class ZipFileUtilTest {
  private File tempDir;

  @BeforeEach
  void beforeMethod() {
    tempDir = Files.createTempDir();
  }

  @AfterEach
  void afterMethod() throws IOException {
    FileUtils.deleteDirectory(tempDir);
  }

  @Test
  void testUnzipInvalid() {
    InputStream is = ZipFileUtil.class.getResourceAsStream("flip.zip");
    assertThrows(UnzipException.class, () -> ZipFileUtil.unzip(is, tempDir));
  }

  @Test
  void testUnzip() {
    InputStream is = ZipFileUtil.class.getResourceAsStream("emx-csv.zip");
    ZipFileUtil.unzip(is, tempDir);
    Path tempDirPath = tempDir.toPath();
    Map<Path, String> checksums =
        ImmutableMap.of(
            tempDirPath.resolve("attributes.csv"),
            "4cc30a6b867ce0689f19bdaadcd30dd9",
            tempDirPath.resolve("entities.csv"),
            "8a306d69b8654f54f1e9175ce68b5a6f",
            tempDirPath.resolve("it_csv_hospital.csv"),
            "87824a0ec5312a81e4f9fd8ad51b666b",
            tempDirPath.resolve("it_csv_patients.csv"),
            "497e36e9b850a5a66f292378c68bce15",
            tempDirPath.resolve("packages.csv"),
            "11e879ddd1ebe6376be309acb910d232");
    checksums.forEach(
        (key, value) -> assertEquals(md5Hash(key), value, key + " should have md5 sum " + value));
  }

  @Test
  void testUnzipSkipHidden() throws IOException {
    InputStream is = ZipFileUtil.class.getResourceAsStream("test.zip");
    Path tempDirPath = tempDir.toPath();
    Path targetPath = tempDirPath.resolve("test.zip");
    java.nio.file.Files.copy(is, targetPath);

    List<File> files = ZipFileUtil.unzipSkipHidden(targetPath.toFile());

    Path visiblePath = tempDirPath.resolve(Paths.get("subdir", "visible.txt"));
    assertEquals(singletonList(visiblePath.toFile()), files);

    assertEquals("aff776838092862d398b58e380901753", md5Hash(visiblePath));
  }

  @Test
  void testUnzipSkipHiddenInvalid() throws IOException {
    InputStream is = ZipFileUtil.class.getResourceAsStream("flip.zip");
    Path tempDirPath = tempDir.toPath();
    Path targetPath = tempDirPath.resolve("flip.zip");
    java.nio.file.Files.copy(is, targetPath);

    assertThrows(UnzipException.class, () -> ZipFileUtil.unzipSkipHidden(targetPath.toFile()));
  }

  private String md5Hash(Path path) {
    try (FileInputStream fis = new FileInputStream(path.toFile())) {
      return org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
    } catch (IOException ex) {
      return ex.toString();
    }
  }
}
