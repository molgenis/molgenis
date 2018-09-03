package org.molgenis.data.file;

import static java.io.File.separator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class FileStore {
  private final String storageDir;

  public FileStore(String storageDir) {
    if (storageDir == null) throw new IllegalArgumentException("storage dir is null");
    this.storageDir = storageDir;
  }

  public boolean createDirectory(String dirName) {
    return new File(storageDir + separator + dirName).mkdirs();
  }

  public void deleteDirectory(String dirName) throws IOException {
    FileUtils.deleteDirectory(getFile(dirName));
  }

  public File store(InputStream is, String fileName) throws IOException {
    File file = new File(storageDir + separator + fileName);
    FileOutputStream fos = new FileOutputStream(file);
    try {
      IOUtils.copy(is, fos);
    } finally {
      IOUtils.closeQuietly(fos);
    }
    return file;
  }

  /**
   * Move directories in FileStore Pleae provide the path from the relative root of the fileStore
   *
   * <p>So if you want to move a top-level directory the following syntax is sufficient: <code>
   * move("dir1", "dir2");
   * </code> Sub-level directory can be moved by typing: <code>
   * move("dir1/subdir1", "dir2/subdir1");
   * </code> Make sure the top-level directories are existing
   *
   * @param sourceDir directory yo want to move
   * @param targetDir target directory you want to move to
   * @throws IOException
   */
  public void move(String sourceDir, String targetDir) throws IOException {
    Files.move(
        Paths.get(getStorageDir() + File.separator + sourceDir),
        Paths.get(getStorageDir() + File.separator + targetDir));
  }

  public File getFile(String fileName) {
    return new File(storageDir + separator + fileName);
  }

  public boolean delete(String fileName) {
    File file = new File(storageDir + separator + fileName);
    return file.delete();
  }

  public String getStorageDir() {
    return storageDir;
  }

  public void writeToFile(InputStream inputStream, String fileName) throws IOException {
    FileUtils.copyInputStreamToFile(inputStream, getFile(fileName));
  }
}
