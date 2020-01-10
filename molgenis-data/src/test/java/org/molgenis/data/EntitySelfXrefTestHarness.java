package org.molgenis.data;

import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;

import javax.annotation.PostConstruct;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntitySelfXrefTestHarness {

  public static final String ATTR_ID = "id_attr";
  public static final String ATTR_XREF = "xref_attr";
  public static final String ATTR_STRING = "string_attr";

  @Autowired private EntityTypeFactory entityTypeFactory;

  @Autowired private AttributeFactory attributeFactory;

  @PostConstruct
  public void postConstruct() {}

  public EntityType createDynamicEntityType() {
    return entityTypeFactory
        .create("SelfRef")
        .setLabel("SelfRef")
        .setBackend("PostgreSQL")
        .addAttribute(createAttribute(ATTR_ID, STRING), ROLE_ID)
        .addAttribute(createAttribute(ATTR_STRING, STRING).setNillable(false), ROLE_LABEL);
  }

  public void addSelfReference(EntityType entityType) {
    entityType.addAttribute(createAttribute(ATTR_XREF, XREF).setRefEntity(entityType));
  }

  private Attribute createAttribute(String name, AttributeType dataType) {
    return attributeFactory.create().setName(name).setDataType(dataType);
  }
}
