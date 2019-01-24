package org.molgenis.data.file;

import static java.io.File.separator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    FileUtils.deleteDirectory(getFileUnchecked(dirName));
  }

  public File store(InputStream is, String fileName) throws IOException {
    File file = new File(storageDir + separator + fileName);
    try (FileOutputStream fos = new FileOutputStream(file)) {
      IOUtils.copy(is, fos);
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

  /**
   * Returns a {@link File} for the given filename in the store.
   *
   * @throws FileNotFoundException if no file with the given filename exists
   * @throws IOException if the given filename does not refer to a file
   */
  public File getFile(String fileName) throws IOException {
    String pathname = storageDir + separator + fileName;

    File file = new File(pathname);
    if (!file.exists()) {
      throw new FileNotFoundException(pathname);
    }
    if (!file.isFile()) {
      throw new IOException('\'' + pathname + '\'' + " is not a file");
    }
    return file;
  }

  /**
   * Returns a {@link File} for the given filename in the store. The filename may denote a
   * nonexistent file in the store or refer to a directory.
   *
   * @see #getFile(String)
   */
  public File getFileUnchecked(String fileName) {
    return new File(storageDir + separator + fileName);
  }

  /** @throws UncheckedIOException if the file with given name could not be deleted */
  public void delete(String fileName) {
    Path path = Paths.get(storageDir + separator + fileName);
    try {
      Files.delete(path);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public String getStorageDir() {
    return storageDir;
  }

  public void writeToFile(InputStream inputStream, String fileName) throws IOException {
    FileUtils.copyInputStreamToFile(inputStream, getFileUnchecked(fileName));
  }
}
