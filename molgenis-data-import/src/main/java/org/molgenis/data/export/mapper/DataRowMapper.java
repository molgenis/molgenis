package org.molgenis.data.export.mapper;

import static com.google.common.collect.Lists.newArrayList;
import static org.molgenis.data.util.EntityTypeUtils.isMultipleReferenceType;
import static org.molgenis.data.util.EntityTypeUtils.isSingleReferenceType;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;

public class DataRowMapper {

  private DataRowMapper() {}

  public static List<Object> mapDataRow(Entity entity) {
    List<Object> dataRow = newArrayList();
    for (Attribute attribute : entity.getEntityType().getAttributes()) {
      if (attribute.getDataType() != AttributeType.COMPOUND) {
        if (isSingleReferenceType(attribute)) {
          Entity value = entity.getEntity(attribute.getName());
          dataRow.add(value != null ? value.getIdValue() : null);
        } else if (isMultipleReferenceType(attribute)) {
          Iterable<Entity> values = entity.getEntities(attribute.getName());
          List<Object> ids =
              StreamSupport.stream(values.spliterator(), false)
                  .map(Entity::getIdValue)
                  .collect(Collectors.toList());
          dataRow.add(org.apache.logging.log4j.util.Strings.join(ids, ','));
        } else {
          Object value = entity.get(attribute.getName());
          dataRow.add(value);
        }
      }
    }
    return dataRow;
  }
}
