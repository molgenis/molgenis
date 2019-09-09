package org.molgenis.data.file;

import static java.io.File.separator;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.io.Files;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FileStoreTest {

  private FileStore fileStore;

  @BeforeEach
  void beforeMethod() throws IOException {
    File tempDir = Files.createTempDir();
    fileStore = new FileStore(tempDir.getCanonicalPath());
  }

  @Test
  void testConstructor() {
    assertThrows(IllegalArgumentException.class, () -> new FileStore(null));
  }

  @Test
  void testCreateDirectory() {
    assertTrue(fileStore.createDirectory("testDir"));
    assertTrue(fileStore.getFileUnchecked("testDir").isDirectory());
    fileStore.delete("testDir");
  }

  @Test
  void testStore() throws IOException {
    File file = fileStore.store(new ByteArrayInputStream(new byte[] {1, 2, 3}), "bytes.bin");
    assertArrayEquals(FileUtils.readFileToByteArray(file), new byte[] {1, 2, 3});
  }

  @Test
  void testMoveTopLevelDir() throws IOException {
    assertTrue(fileStore.createDirectory("testDir1"));
    fileStore.store(
        new ByteArrayInputStream(new byte[] {1, 2, 3}), "testDir1" + separator + "bytes.bin");
    fileStore.move("testDir1", "testDir2");
    File file = fileStore.getFileUnchecked("testDir2" + separator + "bytes.bin");
    assertArrayEquals(FileUtils.readFileToByteArray(file), new byte[] {1, 2, 3});
  }

  @Test
  void testMoveSubLevelDir() throws IOException {
    assertTrue(fileStore.createDirectory("testDir1" + separator + "testDir2"));
    assertTrue(fileStore.createDirectory("testDir2"));
    fileStore.store(
        new ByteArrayInputStream(new byte[] {1, 2, 3}),
        "testDir1" + separator + "testDir2" + separator + "bytes.bin");
    fileStore.move("testDir1" + separator + "testDir2", "testDir2" + separator + "testDir3");
    File file =
        fileStore.getFileUnchecked("testDir2" + separator + "testDir3" + separator + "bytes.bin");
    assertArrayEquals(FileUtils.readFileToByteArray(file), new byte[] {1, 2, 3});
  }

  @Test
  void testGetFileUnchecked() throws IOException {
    String fileName = "bytes.bin";
    File file = fileStore.store(new ByteArrayInputStream(new byte[] {1, 2, 3}), fileName);
    assertEquals(fileStore.getFileUnchecked(fileName).getAbsolutePath(), file.getAbsolutePath());
  }

  @Test
  void testGetFileUncheckedDirectoryTraversal() {
    String fileName = "../../bytes.bin";
    assertThrows(
        UncheckedIOException.class,
        () -> fileStore.store(new ByteArrayInputStream(new byte[] {1, 2, 3}), fileName));
  }

  @Test
  void testGetFileExists() throws IOException {
    String fileName = "bytes.bin";
    fileStore.store(new ByteArrayInputStream(new byte[] {1, 2, 3}), fileName);
    assertTrue(fileStore.getFile(fileName).exists());
  }

  @Test
  void testGetFileNotExists() {
    String fileName = "unknownFile";
    assertThrows(FileNotFoundException.class, () -> fileStore.getFile(fileName));
  }

  @Test
  void testGetFileIsNotFile() {
    String dirName = "directory";
    fileStore.createDirectory(dirName);
    assertThrows(IOException.class, () -> fileStore.getFile(dirName));
  }
}
