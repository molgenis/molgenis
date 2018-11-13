package org.molgenis.navigator.copy.service;

import java.util.List;
import org.molgenis.jobs.Progress;
import org.molgenis.navigator.model.ResourceIdentifier;

public interface CopyService {

  String copy(List<ResourceIdentifier> resources, String newLocationId, Progress progress);
}
