package org.molgenis.semanticmapper.repository;

import java.util.Collection;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping;
import org.molgenis.semanticmapper.mapping.model.EntityMapping;
import org.molgenis.semanticmapper.meta.AttributeMappingMetadata;
import org.molgenis.semanticmapper.meta.EntityMappingMetadata;

/** O/R Mapping between {@link EntityMappingMetadata} Entities and {@link EntityMapping} s. */
public interface AttributeMappingRepository {
  /**
   * Creates a list of fully reconstructed {@link AttributeMapping}s.
   *
   * @param attributeMappingEntities List of {@link Entity}s with {@link AttributeMappingMetadata}
   *     metadata
   * @param sourceEntityType {@link EntityType} of the source entity of the attribute mapping, used
   *     to look up the {@link Attribute}
   * @param targetEntityType {@link EntityType} of the target entity of the attribute mapping, used
   *     to look up the {@link Attribute}
   * @return a list of {@link AttributeMapping}s.
   */
  List<AttributeMapping> getAttributeMappings(
      List<Entity> attributeMappingEntities,
      @Nullable @CheckForNull EntityType sourceEntityType,
      @Nullable @CheckForNull EntityType targetEntityType);

  /**
   * Inserts or updates a {@link Collection} of {@link AttributeMapping}. Will generate IDs if they
   * are not yet specified.
   *
   * @return a list of Entities that have been added or updated
   */
  List<Entity> upsert(Collection<AttributeMapping> collection);
}
