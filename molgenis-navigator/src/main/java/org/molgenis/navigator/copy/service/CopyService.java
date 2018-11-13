package org.molgenis.navigator.copy.service;

import java.util.List;
import org.molgenis.jobs.Progress;
import org.molgenis.navigator.Resource;

public interface CopyService {

  String copy(List<Resource> resources, String newLocationId, Progress progress);
}
