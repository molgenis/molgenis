package org.molgenis.dataexplorer.negotiator.config;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.springframework.stereotype.Component;

@Component
public class NegotiatorEntityConfigMetadata extends SystemEntityType {
  private static final String SIMPLE_NAME = "NegotiatorEntityConfig";

  public static final String NEGOTIATORENTITYCONFIG =
      NegotiatorPackage.PACKAGE_NEGOTIATOR + PACKAGE_SEPARATOR + SIMPLE_NAME;

  private static final String IDENTIFIER = "id";
  public static final String COLLECTION_ID = "collectionId";
  public static final String BIOBANK_ID = "biobankId";
  public static final String ENTITY = "entity";
  public static final String ENABLED_EXPRESSION = "enabledExpression";
  public static final String NEGOTIATOR_CONFIG = "negotiatorConfig";

  private final EntityTypeMetadata entityTypeMetadata;
  private final AttributeMetadata attributeMetadata;
  private final NegotiatorPackage negotiatorPackage;
  private final NegotiatorConfigMetadata negotiatorConfigMetadata;

  public NegotiatorEntityConfigMetadata(
      AttributeMetadata attributeMetadata,
      EntityTypeMetadata entityTypeMetadata,
      NegotiatorPackage negotiatorPackage,
      NegotiatorConfigMetadata negotiatorConfigMetadata) {
    super(SIMPLE_NAME, NegotiatorPackage.PACKAGE_NEGOTIATOR);
    this.entityTypeMetadata = requireNonNull(entityTypeMetadata);
    this.attributeMetadata = requireNonNull(attributeMetadata);
    this.negotiatorPackage = requireNonNull(negotiatorPackage);
    this.negotiatorConfigMetadata = requireNonNull(negotiatorConfigMetadata);
  }

  @Override
  protected void init() {
    setLabel("Negotiator Entity Config");
    setPackage(negotiatorPackage);
    addAttribute(IDENTIFIER, ROLE_ID)
        .setLabel("Identifier")
        .setAuto(false)
        .setNillable(false)
        .setDescription("Identifier for this entity config");
    addAttribute(ENTITY)
        .setDataType(AttributeType.XREF)
        .setRefEntity(entityTypeMetadata)
        .setNillable(false)
        .setLabel("Entity to use in negotiator");
    addAttribute(NEGOTIATOR_CONFIG)
        .setDataType(AttributeType.XREF)
        .setRefEntity(negotiatorConfigMetadata)
        .setNillable(false)
        .setLabel("General negotiator settings");
    addAttribute(COLLECTION_ID)
        .setDataType(AttributeType.XREF)
        .setRefEntity(attributeMetadata)
        .setNillable(false)
        .setLabel("Attribute containing the collection");
    addAttribute(BIOBANK_ID)
        .setDataType(AttributeType.XREF)
        .setRefEntity(attributeMetadata)
        .setNillable(false)
        .setLabel("Attribute containing the biobank");
    addAttribute(ENABLED_EXPRESSION)
        .setDataType(AttributeType.SCRIPT)
        .setNillable(true)
        .setLabel("Negotiator enabled expression")
        .setDescription(
            "Expression to determine if the row is enabled for usage with the negotiator");
  }
}
