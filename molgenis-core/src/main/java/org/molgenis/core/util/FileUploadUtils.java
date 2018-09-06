package org.molgenis.core.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.http.Part;
import org.apache.commons.io.FileUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

public class FileUploadUtils {
  private FileUploadUtils() {}

  /**
   * Saves an uploaded file to a tempfile with prefix 'molgenis-', keeps the original file extension
   *
   * @return the saved temp file or null is no file selected
   */
  public static File saveToTempFile(Part part) throws IOException {
    String filename = getOriginalFileName(part);
    if (filename == null) {
      return null;
    }

    File file = File.createTempFile("molgenis-", "." + StringUtils.getFilenameExtension(filename));
    FileCopyUtils.copy(part.getInputStream(), new FileOutputStream(file));

    return file;
  }

  /** Save an Uploaded file to the temp folder keeping it original name */
  public static File saveToTempFolder(Part part) throws IOException {
    String filename = getOriginalFileName(part);
    if (filename == null) {
      return null;
    }

    File file = new File(FileUtils.getTempDirectory(), filename);
    FileCopyUtils.copy(part.getInputStream(), new FileOutputStream(file));

    return file;
  }

  /**
   * Get the filename of an uploaded file
   *
   * @return the filename or null if not present
   */
  public static String getOriginalFileName(Part part) {
    String contentDisposition = part.getHeader("content-disposition");
    if (contentDisposition != null) {
      for (String cd : contentDisposition.split(";")) {

        if (cd.trim().startsWith("filename")) {
          String path = cd.substring(cd.indexOf('=') + 1).replaceAll("\"", "").trim();
          Path filename = Paths.get(path).getFileName();
          return StringUtils.hasText(filename.toString()) ? filename.toString() : null;
        }
      }
    }

    return null;
  }
}
