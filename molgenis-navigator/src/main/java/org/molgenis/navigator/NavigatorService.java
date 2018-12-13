package org.molgenis.navigator;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.validation.constraints.NotEmpty;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.navigator.model.Resource;
import org.molgenis.navigator.model.ResourceIdentifier;

public interface NavigatorService {
  /**
   * @param folderId <tt>null</tt> implies the root folder
   * @return folder or <tt>null</tt> if folderId is <tt>null</tt>
   */
  @CheckForNull
  Folder getFolder(@CheckForNull String folderId);

  /** @param folderId <tt>null</tt> implies the root folder */
  List<Resource> getResources(@CheckForNull String folderId);

  List<Resource> findResources(String query);

  /** @param targetFolderId <tt>null</tt> implies the root folder */
  void moveResources(List<ResourceIdentifier> resources, @CheckForNull String targetFolderId);

  /** @param targetFolderId <tt>null</tt> implies the root folder */
  JobExecution copyResources(
      @NotEmpty List<ResourceIdentifier> resources, @CheckForNull String targetFolderId);

  JobExecution downloadResources(@NotEmpty List<ResourceIdentifier> resources);

  JobExecution deleteResources(@NotEmpty List<ResourceIdentifier> resources);

  void updateResource(Resource resource);
}
