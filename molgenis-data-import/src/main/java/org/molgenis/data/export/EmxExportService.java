package org.molgenis.data.export;

import java.io.File;
import java.util.List;
import java.util.Optional;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.jobs.Progress;

public interface EmxExportService {
  void download(
      List<EntityType> entityTypes, List<Package> packages, File file, Optional<Progress> progress);

  void download(List<EntityType> entityTypes, List<Package> packages, File file);
}
