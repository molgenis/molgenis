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

  void moveResources(List<Resource> resources, String targetFolderId);

  // TODO return CopyJobExecution once available
  JobExecution copyResources(List<Resource> resources, String targetFolderId);

  void deleteResources(List<Resource> resources);

  void updateResource(Resource resource);
}
