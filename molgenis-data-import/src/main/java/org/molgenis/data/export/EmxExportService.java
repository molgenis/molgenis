package org.molgenis.data.export;

import java.io.File;
import java.util.List;

public interface EmxExportService {
  void download(List<String> entityTypeIds, List<String> packageIds, File file);
}
