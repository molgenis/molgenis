package org.molgenis.navigator;

import java.util.List;
import javax.annotation.Nullable;
import org.molgenis.jobs.model.JobExecution;

public interface NavigatorService {
  /**
   * @param folderId <tt>null</tt> implies the root folder
   * @return folder or <tt>null</tt> if folderId is <tt>null</tt>
   */
  @Nullable
  Folder getFolder(@Nullable String folderId);

  /** @param folderId <tt>null</tt> implies the root folder */
  List<Resource> getResources(@Nullable String folderId);

  List<Resource> findResources(String query);

  /** @param targetFolderId <tt>null</tt> implies the root folder */
  void moveResources(List<ResourceIdentifier> resources, @Nullable String targetFolderId);

  /** @param targetFolderId <tt>null</tt> implies the root folder */
  JobExecution copyResources(List<ResourceIdentifier> resources, @Nullable String targetFolderId);

  JobExecution downloadResources(List<ResourceIdentifier> resources);

  void deleteResources(List<ResourceIdentifier> resources);

  void updateResource(Resource resource);
}
