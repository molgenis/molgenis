package org.molgenis.navigator;

import java.util.List;
import org.molgenis.jobs.model.JobExecution;

public interface NavigatorService {
  void moveResources(List<Resource> resources, String targetFolderId);

  // TODO return CopyJobExecution once available
  JobExecution copyResources(List<Resource> resources, String targetFolderId);

  void deleteResources(List<Resource> resources);

  void updateResource(Resource resource);
}
