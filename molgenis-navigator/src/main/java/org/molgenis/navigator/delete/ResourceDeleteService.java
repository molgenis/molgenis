package org.molgenis.navigator.delete;

import java.util.List;
import org.molgenis.jobs.Progress;
import org.molgenis.navigator.model.ResourceIdentifier;

public interface ResourceDeleteService {

  Void delete(List<ResourceIdentifier> resources, Progress progress);
}
