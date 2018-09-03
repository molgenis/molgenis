package org.molgenis.data.file.util;

import static org.apache.commons.io.FilenameUtils.getBaseName;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FileExtensionUtils {
  private FileExtensionUtils() {}

  public static String findExtensionFromPossibilities(String fileName, Set<String> fileExtensions) {
    String name = fileName.toLowerCase();
    List<String> possibleExtensions = new ArrayList<>();
    for (String extension : fileExtensions) {
      if (name.endsWith('.' + extension)) {
        possibleExtensions.add(extension);
      }
    }

    String longestExtension = null;
    for (String possibleExtension : possibleExtensions) {
      if (null == longestExtension) {
        longestExtension = possibleExtension;
        continue;
      } else {
        if (longestExtension.length() < possibleExtension.length())
          longestExtension = possibleExtension;
      }
    }

    return longestExtension;
  }

  public static String getFileNameWithoutExtension(String filename) {
    return getBaseName(filename);
  }
}
