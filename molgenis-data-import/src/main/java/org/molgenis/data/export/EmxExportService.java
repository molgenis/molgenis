package org.molgenis.data.export;

import java.io.File;
import java.util.List;
import java.util.Optional;
import org.molgenis.jobs.Progress;

public interface EmxExportService {
  void download(
      List<String> entityTypeIds, List<String> packageIds, File file, Optional<Progress> progress);

  void download(List<String> entityTypeIds, List<String> packageIds, File file);
}
