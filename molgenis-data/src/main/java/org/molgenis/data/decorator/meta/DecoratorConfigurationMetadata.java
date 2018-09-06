package org.molgenis.data.decorator.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.decorator.meta.DecoratorPackage.PACKAGE_DECORATOR;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

@Component
public class DecoratorConfigurationMetadata extends SystemEntityType {
  private static final String SIMPLE_NAME = "DecoratorConfiguration";
  public static final String DECORATOR_CONFIGURATION =
      PACKAGE_DECORATOR + PACKAGE_SEPARATOR + SIMPLE_NAME;

  public static final String ID = "id";
  public static final String ENTITY_TYPE_ID = "entityTypeId";
  public static final String PARAMETERS = "parameters";

  private final DecoratorPackage decoratorPackage;
  private final DecoratorParametersMetadata decoratorParametersMetadata;

  DecoratorConfigurationMetadata(
      DecoratorPackage decoratorPackage, DecoratorParametersMetadata decoratorParametersMetadata) {
    super(SIMPLE_NAME, PACKAGE_DECORATOR);
    this.decoratorPackage = requireNonNull(decoratorPackage);
    this.decoratorParametersMetadata = requireNonNull(decoratorParametersMetadata);
  }

  @Override
  public void init() {
    setLabel("Decorator Configuration");
    setPackage(decoratorPackage);
    setDescription(
        "Configuration entity to configure which dynamic decorators should be applied to an entity type.");

    addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false).setLabel("Identifier");
    addAttribute(ENTITY_TYPE_ID, ROLE_LABEL)
        .setNillable(false)
        .setUnique(true)
        .setLabel("Entity Type Identifier");
    addAttribute(PARAMETERS)
        .setNillable(false)
        .setDataType(AttributeType.MREF)
        .setRefEntity(decoratorParametersMetadata)
        .setLabel("Decorator Parameters");
  }
}
