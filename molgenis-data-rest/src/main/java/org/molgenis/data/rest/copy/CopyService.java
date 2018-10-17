package org.molgenis.data.rest.copy;

import java.util.stream.Stream;
import org.molgenis.data.resource.Resource;
import org.molgenis.jobs.Progress;

public interface CopyService {

  String copy(Stream<Resource> resources, String newLocationId, Progress progress);
}
