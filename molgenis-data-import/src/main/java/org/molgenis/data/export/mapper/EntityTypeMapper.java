package org.molgenis.data.export.mapper;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES_ABSTRACT;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES_BACKEND;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES_DESCRIPTION;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES_EXTENDS;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES_LABEL;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES_NAME;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES_PACKAGE;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES_TAGS;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.Tag;

public class EntityTypeMapper {
  public static final ImmutableMap<String, String> ENTITIES_ATTRS;

  static {
    ENTITIES_ATTRS =
        ImmutableMap.<String, String>builder()
            .put(EMX_ENTITIES_NAME, EntityTypeMetadata.ID)
            .put(EMX_ENTITIES_PACKAGE, EntityTypeMetadata.PACKAGE)
            .put(EMX_ENTITIES_LABEL, EntityTypeMetadata.LABEL)
            .put(EMX_ENTITIES_DESCRIPTION, EntityTypeMetadata.DESCRIPTION)
            .put(EMX_ENTITIES_ABSTRACT, EntityTypeMetadata.IS_ABSTRACT)
            .put(EMX_ENTITIES_EXTENDS, EntityTypeMetadata.EXTENDS)
            .put(EMX_ENTITIES_BACKEND, EntityTypeMetadata.BACKEND)
            .put(EMX_ENTITIES_TAGS, EntityTypeMetadata.TAGS)
            .build();
  }

  private EntityTypeMapper() {}

  public static List<Object> map(EntityType entityType) {
    List<Object> row = new ArrayList<>(ENTITIES_ATTRS.size());
    for (Entry<String, String> entry : ENTITIES_ATTRS.entrySet()) {
      switch (entry.getKey()) {
        case EMX_ENTITIES_TAGS:
          row.add(getTags(entityType));
          break;
        case EMX_ENTITIES_EXTENDS:
          row.add(entityType.getExtends() != null ? entityType.getExtends().getId() : null);
          break;
        case EMX_ENTITIES_PACKAGE:
          row.add(getPackageId(entityType));
          break;
        case EMX_ENTITIES_NAME:
          row.add(getName(entityType));
          break;
        default:
          Object value = entityType.get(entry.getValue());
          row.add(value != null ? value.toString() : null);
          break;
      }
    }
    return row;
  }

  public static List<Object> getHeaders(EntityType entityType) {
    return Streams.stream(entityType.getAttributes())
        .filter(attr -> attr.getDataType() != AttributeType.COMPOUND)
        .map(Attribute::getName)
        .collect(toList());
  }

  private static String getTags(EntityType entityType) {
    return Streams.stream(entityType.getTags()).map(Tag::getId).collect(joining(","));
  }

  private static String getName(EntityType entityType) {
    String entityName = entityType.getId();
    String packageName = entityType.getPackage() != null ? entityType.getPackage().getId() : "";
    if (!packageName.isEmpty() && entityName.startsWith(packageName)) {
      entityName = entityName.substring(packageName.length() + 1);
    }
    return entityName;
  }

  private static String getPackageId(EntityType entityType) {
    Package pack = entityType.getPackage();
    return pack != null ? pack.getId() : "";
  }
}
