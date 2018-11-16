package org.molgenis.navigator.copy.service;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import com.google.auto.value.AutoValue;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.jobs.Progress;

@AutoValue
abstract class CopyState {

  @Nullable
  abstract Package targetPackage();

  abstract Progress progress();

  abstract List<EntityType> entityTypesInPackages();

  abstract Map<String, Package> copiedPackages();

  abstract Map<String, EntityType> copiedEntityTypes();

  abstract Map<String, Attribute> copiedAttributes();

  abstract Map<String, String> originalIds();

  abstract Map<String, String> referenceDefaultValues();

  public static CopyState create(@Nullable Package targetPackage, Progress progress) {
    return new AutoValue_CopyState(
        targetPackage,
        progress,
        newArrayList(),
        newHashMap(),
        newHashMap(),
        newHashMap(),
        newHashMap(),
        newHashMap());
  }
}
