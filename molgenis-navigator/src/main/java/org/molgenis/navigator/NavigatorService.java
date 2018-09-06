package org.molgenis.navigator;

import java.util.List;

public interface NavigatorService {

  void deleteItems(List<String> packageIds, List<String> entityTypeIds);
}
