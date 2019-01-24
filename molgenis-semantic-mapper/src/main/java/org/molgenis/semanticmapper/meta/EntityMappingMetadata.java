package org.molgenis.semanticmapper.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.semanticmapper.meta.MapperPackage.PACKAGE_MAPPER;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

@Component
public class EntityMappingMetadata extends SystemEntityType {
  private static final String SIMPLE_NAME = "EntityMapping";
  public static final String ENTITY_MAPPING = PACKAGE_MAPPER + PACKAGE_SEPARATOR + SIMPLE_NAME;

  public static final String IDENTIFIER = "identifier";
  public static final String SOURCE_ENTITY_TYPE = "sourceEntityType";
  public static final String TARGET_ENTITY_TYPE = "targetEntityType";
  public static final String ATTRIBUTE_MAPPINGS = "attributeMappings";

  private final MapperPackage mapperPackage;
  private final AttributeMappingMetadata attributeMappingMetaData;

  public EntityMappingMetadata(
      MapperPackage mapperPackage, AttributeMappingMetadata attributeMappingMetaData) {
    super(SIMPLE_NAME, PACKAGE_MAPPER);
    this.mapperPackage = requireNonNull(mapperPackage);
    this.attributeMappingMetaData = requireNonNull(attributeMappingMetaData);
  }

  @Override
  public void init() {
    setLabel("Entity mapping");
    setPackage(mapperPackage);

    addAttribute(IDENTIFIER, ROLE_ID);
    addAttribute(SOURCE_ENTITY_TYPE);
    addAttribute(TARGET_ENTITY_TYPE);
    addAttribute(ATTRIBUTE_MAPPINGS)
        .setDataType(MREF)
        .setRefEntity(attributeMappingMetaData)
        .setCascadeDelete(true);
  }
}
