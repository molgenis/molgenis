package org.molgenis.navigator.copy.service;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.jobs.Progress;

class CopyState {

  @Nullable private final Package targetPackage;
  private final Progress progress;
  private final List<EntityType> entityTypesInPackages = new ArrayList<>();
  private final Map<String, Package> copiedPackages = new HashMap<>();
  private final Map<String, EntityType> copiedEntityTypes = new HashMap<>();
  private final Map<String, Attribute> copiedAttributes = new HashMap<>();
  private final Map<String, String> originalEntityTypeIds = new HashMap<>();
  private final Map<String, String> referenceDefaultValues = new HashMap<>();

  private CopyState(@Nullable Package targetPackage, Progress progress) {
    this.targetPackage = targetPackage;
    this.progress = requireNonNull(progress);
  }

  public static CopyState create(@Nullable Package targetPackage, Progress progress) {
    return new CopyState(targetPackage, progress);
  }

  @Nullable
  public Package targetPackage() {
    return targetPackage;
  }

  public Progress progress() {
    return progress;
  }

  public List<EntityType> entityTypesInPackages() {
    return entityTypesInPackages;
  }

  public Map<String, Package> copiedPackages() {
    return copiedPackages;
  }

  public Map<String, EntityType> copiedEntityTypes() {
    return copiedEntityTypes;
  }

  public Map<String, Attribute> copiedAttributes() {
    return copiedAttributes;
  }

  public Map<String, String> originalEntityTypeIds() {
    return originalEntityTypeIds;
  }

  public Map<String, String> referenceDefaultValues() {
    return referenceDefaultValues;
  }
}
