package org.molgenis.api.metadata.v3;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
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
   * @param attributeId attribute identifier
   * @param entityTypeId
   * @throws UnknownAttributeException if no attribute exists for the given identifier
   * @throws UnknownEntityTypeException if no entityType exists for the given identifier
   */
  Attribute findAttribute(String entityTypeId, String attributeId);

  Void deleteAttribute(String entityTypeId, String attributeId);

  Void deleteAttributes(String entityTypeId, List<String> attributeIds);

  /**
   * @param entityTypeId entity type identifier
   * @throws UnknownEntityTypeException if no entity type exists for the given identifier
   */
  Void deleteEntityType(String entityTypeId);

  /**
   * @param entityTypeIds entity type identifiers
   * @throws UnknownEntityTypeException if no entity type exists for a given identifier
   */
  Void deleteEntityTypes(List<String> entityTypeIds);

  /**
   * Updates entity type.
   *
   * @param entityType updated entity type
   */
  Void updateEntityType(EntityType entityType);
}
