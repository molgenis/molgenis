package org.molgenis.data.file;

import static java.io.File.separator;
import static org.testng.Assert.assertTrue;

import com.google.common.io.Files;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FileStoreTest {

  private FileStore fileStore;

  @BeforeMethod
  public void beforeMethod() throws IOException {
    File tempDir = Files.createTempDir();
    fileStore = new FileStore(tempDir.getCanonicalPath());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructor() {
    new FileStore(null);
  }

  @Test
  public void testCreateDirectory() {
    assertTrue(fileStore.createDirectory("testDir"));
    assertTrue(fileStore.getFileUnchecked("testDir").isDirectory());
    fileStore.delete("testDir");
  }

  @Test
  public void testStore() throws IOException {
    File file = fileStore.store(new ByteArrayInputStream(new byte[] {1, 2, 3}), "bytes.bin");
    Assert.assertEquals(FileUtils.readFileToByteArray(file), new byte[] {1, 2, 3});
  }

  @Test
  public void testMoveTopLevelDir() throws IOException {
    assertTrue(fileStore.createDirectory("testDir1"));
    fileStore.store(
        new ByteArrayInputStream(new byte[] {1, 2, 3}), "testDir1" + separator + "bytes.bin");
    fileStore.move("testDir1", "testDir2");
    File file = fileStore.getFileUnchecked("testDir2" + separator + "bytes.bin");
    Assert.assertEquals(FileUtils.readFileToByteArray(file), new byte[] {1, 2, 3});
  }

  @Test
  public void testMoveSubLevelDir() throws IOException {
    assertTrue(fileStore.createDirectory("testDir1" + separator + "testDir2"));
    assertTrue(fileStore.createDirectory("testDir2"));
    fileStore.store(
        new ByteArrayInputStream(new byte[] {1, 2, 3}),
        "testDir1" + separator + "testDir2" + separator + "bytes.bin");
    fileStore.move("testDir1" + separator + "testDir2", "testDir2" + separator + "testDir3");
    File file =
        fileStore.getFileUnchecked("testDir2" + separator + "testDir3" + separator + "bytes.bin");
    Assert.assertEquals(FileUtils.readFileToByteArray(file), new byte[] {1, 2, 3});
  }

  @Test
  public void testGetFileUnchecked() throws IOException {
    String fileName = "bytes.bin";
    File file = fileStore.store(new ByteArrayInputStream(new byte[] {1, 2, 3}), fileName);
    Assert.assertEquals(
        fileStore.getFileUnchecked(fileName).getAbsolutePath(), file.getAbsolutePath());
  }

  @Test(expectedExceptions = UncheckedIOException.class)
  public void testGetFileUncheckedDirectoryTraversal() throws IOException {
    String fileName = "../../bytes.bin";
    File file = fileStore.store(new ByteArrayInputStream(new byte[] {1, 2, 3}), fileName);
    Assert.assertEquals(
        fileStore.getFileUnchecked(fileName).getAbsolutePath(), file.getAbsolutePath());
  }

  @Test
  public void testGetFileExists() throws IOException {
    String fileName = "bytes.bin";
    fileStore.store(new ByteArrayInputStream(new byte[] {1, 2, 3}), fileName);
    assertTrue(fileStore.getFile(fileName).exists());
  }

  @Test(expectedExceptions = FileNotFoundException.class)
  public void testGetFileNotExists() throws IOException {
    String fileName = "unknownFile";
    fileStore.getFile(fileName);
  }

  @Test(expectedExceptions = IOException.class)
  public void testGetFileIsNotFile() throws IOException {
    String dirName = "directory";
    fileStore.createDirectory(dirName);
    fileStore.getFile(dirName);
  }
}
