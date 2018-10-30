package org.molgenis.data.rest.copy;

import java.util.List;
import org.molgenis.data.resource.Resource;
import org.molgenis.jobs.Progress;

public interface CopyService {

  String copy(List<Resource> resources, String newLocationId, Progress progress);
}
