package org.molgenis.api.metadata.v3;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecution;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecution;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Sort;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

public interface MetadataApiService {

  void createEntityType(EntityType entityType);

  /**
   * @param entityTypeId entity type identifier
   * @throws UnknownEntityTypeException if not entity type exists for the given identifier
   */
  EntityType findEntityType(String entityTypeId);

  EntityTypes findEntityTypes(@Nullable @CheckForNull Query query, Sort sort, int size, int page);

  Attributes findAttributes(
      String entityTypeId, @Nullable @CheckForNull Query query, Sort sort, int size, int page);

  /**
   * @param entityTypeId identifier of the EntityType
   * @param attributeId identifier of the Attribute
   * @throws UnknownAttributeException if no Attribute exists for the given identifier
   * @throws UnknownEntityTypeException if no EntityType exists for the given identifier
   */
  Attribute findAttribute(String entityTypeId, String attributeId);

  /**
   * Deletes an Attribute asynchronously.
   *
   * @param entityTypeId identifier of the Attribute's EntityType
   * @param attributeId identifier of the Attribute to delete
   * @throws UnknownEntityTypeException if no EntityType exists for the given identifier
   * @throws UnknownAttributeException if no Attribute exists for the given identifier or if the
   *     Attribute does not belong to the given EntityType
   * @return a JobExecution describing the status of the job
   */
  MetadataUpsertJobExecution deleteAttributeAsync(String entityTypeId, String attributeId);

  /**
   * Deletes one or more Attributes asynchronously based on a query.
   *
   * @param entityTypeId identifier of the Attribute's EntityType
   * @param query a query that selects attributes
   * @throws UnknownEntityTypeException if no EntityType exists for the given identifier
   * @return a JobExecution describing the status of the job
   */
  MetadataUpsertJobExecution deleteAttributesAsync(String entityTypeId, Query query);

  /**
   * Deletes an EntityType asynchronously.
   *
   * @param entityTypeId identifier of the Attribute's EntityType
   * @throws UnknownEntityTypeException if no EntityType exists for the given identifier
   * @return a JobExecution describing the status of the job
   */
  MetadataDeleteJobExecution deleteEntityTypeAsync(String entityTypeId);

  /**
   * Deletes one or more EntityTypes asynchronously based on a query.
   *
   * @return a JobExecution describing the status of the job
   */
  MetadataDeleteJobExecution deleteEntityTypesAsync(Query query);

  /**
   * Updates an EntityType asynchronously.
   *
   * @param entityType updated EntityType
   * @return a JobExecution describing the status of the job
   */
  MetadataUpsertJobExecution updateEntityTypeAsync(EntityType entityType);
}
