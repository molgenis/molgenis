package org.molgenis.util.file;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import org.zeroturnaround.zip.NameMapper;
import org.zeroturnaround.zip.ZipUtil;

public class ZipFileUtil {
  private ZipFileUtil() {}

  public static void unzip(InputStream is, File outputDir) {
    try {
      ZipUtil.unpack(is, outputDir);
    } catch (Exception ex) {
      throw new UnzipException(ex);
    }
  }

  /**
   * Unzips a zipfile into the directory it resides in. Skips paths starting with '.' or '_'
   * (typically hidden paths).
   *
   * @param file the file to unzip
   * @return List of Files that got extracted
   * @throws UnzipException if something went wrong
   */
  public static List<File> unzipSkipHidden(File file) {
    return unzip(file, name -> name.startsWith(".") || name.startsWith("_") ? null : name);
  }

  /**
   * Unzips a zipfile into the directory it resides in.
   *
   * @param file the zipfile to unzip
   * @param nameMapper the {@link NameMapper} to use when unzipping
   * @return List of Files that got extracted
   * @throws UnzipException if something went wrong
   */
  private static List<File> unzip(File file, NameMapper nameMapper) {
    try {
      File parentFile = file.getParentFile();
      TrackingNameMapper trackingNameMapper = new TrackingNameMapper(parentFile, nameMapper);
      ZipUtil.unpack(file, parentFile, trackingNameMapper);
      return trackingNameMapper.getFiles();
    } catch (Exception ex) {
      throw new UnzipException(ex);
    }
  }

  private static class TrackingNameMapper implements NameMapper {
    private final Path parentPath;
    private ImmutableList.Builder<Path> paths = new ImmutableList.Builder<>();
    private NameMapper delegate;

    private TrackingNameMapper(File directory, NameMapper delegate) {
      this(directory.toPath(), delegate);
    }

    private TrackingNameMapper(Path parentPath, NameMapper delegate) {
      this.parentPath = requireNonNull(parentPath);
      this.delegate = requireNonNull(delegate);
    }

    public List<Path> getPaths() {
      return paths.build();
    }

    public List<File> getFiles() {
      return getPaths()
          .stream()
          .map(Path::toFile)
          .filter(file -> !file.isDirectory())
          .collect(toList());
    }

    @Override
    public String map(String name) {
      String result = delegate.map(name);
      if (result != null) {
        paths.add(parentPath.resolve(result));
      }
      return result;
    }
  }
}
