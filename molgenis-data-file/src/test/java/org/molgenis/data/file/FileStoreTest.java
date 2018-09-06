package org.molgenis.data.file;

import static java.io.File.separator;

import com.google.common.io.Files;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
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
    Assert.assertTrue(fileStore.createDirectory("testDir"));
    Assert.assertTrue(fileStore.getFile("testDir").isDirectory());
    fileStore.delete("testDir");
  }

  @Test
  public void testStore() throws IOException {
    File file = fileStore.store(new ByteArrayInputStream(new byte[] {1, 2, 3}), "bytes.bin");
    Assert.assertEquals(FileUtils.readFileToByteArray(file), new byte[] {1, 2, 3});
  }

  @Test
  public void testMoveTopLevelDir() throws IOException {
    Assert.assertTrue(fileStore.createDirectory("testDir1"));
    fileStore.store(
        new ByteArrayInputStream(new byte[] {1, 2, 3}), "testDir1" + separator + "bytes.bin");
    fileStore.move("testDir1", "testDir2");
    File file = fileStore.getFile("testDir2" + separator + "bytes.bin");
    Assert.assertEquals(FileUtils.readFileToByteArray(file), new byte[] {1, 2, 3});
  }

  @Test
  public void testMoveSubLevelDir() throws IOException {
    Assert.assertTrue(fileStore.createDirectory("testDir1" + separator + "testDir2"));
    Assert.assertTrue(fileStore.createDirectory("testDir2"));
    fileStore.store(
        new ByteArrayInputStream(new byte[] {1, 2, 3}),
        "testDir1" + separator + "testDir2" + separator + "bytes.bin");
    fileStore.move("testDir1" + separator + "testDir2", "testDir2" + separator + "testDir3");
    File file = fileStore.getFile("testDir2" + separator + "testDir3" + separator + "bytes.bin");
    Assert.assertEquals(FileUtils.readFileToByteArray(file), new byte[] {1, 2, 3});
  }

  @Test
  public void testGetFile() throws IOException {
    String fileName = "bytes.bin";
    File file = fileStore.store(new ByteArrayInputStream(new byte[] {1, 2, 3}), fileName);
    Assert.assertEquals(fileStore.getFile(fileName).getAbsolutePath(), file.getAbsolutePath());
  }
}
