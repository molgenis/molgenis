package org.molgenis.util.file;

import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zeroturnaround.zip.commons.FileUtils;

public class ZipFileUtilTest {
  private File tempDir;

  @BeforeMethod
  public void beforeMethod() {
    tempDir = Files.createTempDir();
  }

  @AfterMethod
  public void afterMethod() throws IOException {
    FileUtils.deleteDirectory(tempDir);
  }

  @Test(expectedExceptions = UnzipException.class)
  public void testUnzipInvalid() {
    InputStream is = ZipFileUtil.class.getResourceAsStream("flip.zip");
    ZipFileUtil.unzip(is, tempDir);
  }

  @Test
  public void testUnzip() {
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
  public void testUnzipSkipHidden() throws IOException {
    InputStream is = ZipFileUtil.class.getResourceAsStream("test.zip");
    Path tempDirPath = tempDir.toPath();
    Path targetPath = tempDirPath.resolve("test.zip");
    java.nio.file.Files.copy(is, targetPath);

    List<File> files = ZipFileUtil.unzipSkipHidden(targetPath.toFile());

    Path visiblePath = tempDirPath.resolve(Paths.get("subdir", "visible.txt"));
    assertEquals(files, Collections.singletonList(visiblePath.toFile()));

    assertEquals(md5Hash(visiblePath), "aff776838092862d398b58e380901753");
  }

  @Test(expectedExceptions = UnzipException.class)
  public void testUnzipSkipHiddenInvalid() throws IOException {
    InputStream is = ZipFileUtil.class.getResourceAsStream("flip.zip");
    Path tempDirPath = tempDir.toPath();
    Path targetPath = tempDirPath.resolve("flip.zip");
    java.nio.file.Files.copy(is, targetPath);

    ZipFileUtil.unzipSkipHidden(targetPath.toFile());
  }

  private String md5Hash(Path path) {
    try (FileInputStream fis = new FileInputStream(path.toFile())) {
      return org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
    } catch (IOException ex) {
      return ex.toString();
    }
  }
}
