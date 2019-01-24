package org.molgenis.data.rest.v2;

import static com.google.common.collect.Streams.stream;

import java.util.List;
import java.util.stream.Collectors;
import org.molgenis.data.DataService;
import org.molgenis.data.Sort;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;

/**
 * Do not use this.
 *
 * <p>This is not REST-functionality, it should not be necessary to do this call. You should solve
 * the problems with not having every categorical options in the frontend. The backend should be
 * perform in a sufficient way (see performance tests)`.
 *
 * @deprecated please use the new implemented sortAttribute for categoricals
 */
@Deprecated
public class CategoricalUtils {

  private CategoricalUtils() {}

  @Deprecated
  public static List<CategoricalOptionV2> getCategoricalOptionsForRefEntity(
      DataService dataService, EntityType refEntity, String language) {
    Sort sortOrder =
        stream(refEntity.getAttributes())
            .filter(attribute -> attribute.isVisible() && attribute.isUnique())
            .map(sortAttr -> new Sort(sortAttr.getName()))
            .findFirst()
            .orElse(null);

    Attribute labelAttribute = refEntity.getLabelAttribute(language);
    return dataService
        .findAll(refEntity.getId(), new QueryImpl<>().sort(sortOrder))
        .map(
            entity ->
                new CategoricalOptionV2(
                    entity.getIdValue(), entity.getString(labelAttribute.getName())))
        .collect(Collectors.toList());
  }
}
