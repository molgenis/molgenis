package org.molgenis.data.export.mapper;

import static java.util.stream.Collectors.joining;
import static org.molgenis.data.util.EntityTypeUtils.isMultipleReferenceType;
import static org.molgenis.data.util.EntityTypeUtils.isSingleReferenceType;

import com.google.common.collect.Streams;
import java.util.ArrayList;
import java.util.List;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;

public class DataRowMapper {

  private DataRowMapper() {}

  public static List<Object> mapDataRow(Entity entity) {
    List<Object> dataRow = new ArrayList<>();
    for (Attribute attribute : entity.getEntityType().getAttributes()) {
      // MAPPED_BY and expressions
      if (attribute.getDataType() != AttributeType.COMPOUND) {
        if (isSingleReferenceType(attribute)) {
          Entity value = entity.getEntity(attribute.getName());
          dataRow.add(value != null ? value.getIdValue() : null);
        } else if (isMultipleReferenceType(attribute)) {
          Iterable<Entity> values = entity.getEntities(attribute.getName());
          String ids =
              Streams.stream(values)
                  .map(Entity::getIdValue)
                  .map(Object::toString)
                  .collect(joining(","));
          dataRow.add(ids);
        } else {
          Object value = entity.get(attribute.getName());
          dataRow.add(value);
        }
      }
    }
    return dataRow;
  }
}
