package org.molgenis.data.export;

import java.nio.file.Path;
import java.util.List;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.jobs.Progress;

public interface EmxExportService {
  void export(List<EntityType> entityTypes, List<Package> packages, Path path, Progress progress);
}
