package org.molgenis.data.export.mapper;

import static java.util.stream.Collectors.joining;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES_ABSTRACT;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES_BACKEND;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES_DESCRIPTION;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES_EXTENDS;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES_LABEL;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES_NAME;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES_PACKAGE;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES_TAGS;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.Tag;

public class EntityTypeMapper {
  public static Map<String, String> ENTITIES_ATTRS;

  static {
    ENTITIES_ATTRS = new LinkedHashMap<>();
    ENTITIES_ATTRS.put(EMX_ENTITIES_NAME, EntityTypeMetadata.ID);
    ENTITIES_ATTRS.put(EMX_ENTITIES_PACKAGE, EntityTypeMetadata.PACKAGE);
    ENTITIES_ATTRS.put(EMX_ENTITIES_LABEL, EntityTypeMetadata.LABEL);
    ENTITIES_ATTRS.put(EMX_ENTITIES_DESCRIPTION, EntityTypeMetadata.DESCRIPTION);
    ENTITIES_ATTRS.put(EMX_ENTITIES_ABSTRACT, EntityTypeMetadata.IS_ABSTRACT);
    ENTITIES_ATTRS.put(EMX_ENTITIES_EXTENDS, EntityTypeMetadata.EXTENDS);
    ENTITIES_ATTRS.put(EMX_ENTITIES_BACKEND, EntityTypeMetadata.BACKEND);
    ENTITIES_ATTRS.put(EMX_ENTITIES_TAGS, EntityTypeMetadata.TAGS);
  }

  private EntityTypeMapper() {}

  public static List<Object> map(EntityType entityType) {
    List<Object> row = Lists.newArrayList();
    for (Entry<String, String> entry : ENTITIES_ATTRS.entrySet()) {
      switch (entry.getKey()) {
        case EMX_ENTITIES_TAGS:
          row.add(getTags(entityType));
          break;
        case EMX_ENTITIES_EXTENDS:
          row.add(entityType.getExtends() != null ? entityType.getExtends().getId() : "");
          break;
        case EMX_ENTITIES_PACKAGE:
          row.add(getPackageId(entityType));
          break;
        case EMX_ENTITIES_NAME:
          row.add(getName(entityType));
          break;
        default:
          Object value = entityType.get(entry.getValue());
          row.add(value != null ? value.toString() : "");
          break;
      }
    }
    return row;
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
