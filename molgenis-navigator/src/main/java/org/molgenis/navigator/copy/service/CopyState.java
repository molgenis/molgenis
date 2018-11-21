package org.molgenis.navigator.copy.service;

import com.google.auto.value.AutoValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.jobs.Progress;

@AutoValue
@SuppressWarnings("squid:S1610")
abstract class CopyState {

  @Nullable
  abstract Package targetPackage();

  abstract Progress progress();

  abstract List<EntityType> entityTypesInPackages();

  abstract Map<String, Package> copiedPackages();

  abstract Map<String, EntityType> copiedEntityTypes();

  abstract Map<String, Attribute> copiedAttributes();

  abstract Map<String, String> originalEntityTypeIds();

  abstract Map<String, String> referenceDefaultValues();

  public static CopyState create(@Nullable Package targetPackage, Progress progress) {
    return new AutoValue_CopyState(
        targetPackage,
        progress,
        new ArrayList<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>());
  }
}
