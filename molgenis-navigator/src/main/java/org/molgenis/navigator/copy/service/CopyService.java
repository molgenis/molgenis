package org.molgenis.navigator.copy.service;

import java.util.List;
import javax.annotation.Nullable;
import org.molgenis.jobs.Progress;
import org.molgenis.navigator.model.ResourceIdentifier;

public interface CopyService {

  Void copy(
      List<ResourceIdentifier> resources, @Nullable String targetPackageId, Progress progress);
}
