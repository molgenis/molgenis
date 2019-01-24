package org.molgenis.data.decorator.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.decorator.meta.DecoratorPackage.PACKAGE_DECORATOR;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

@Component
public class DecoratorParametersMetadata extends SystemEntityType {
  private static final String SIMPLE_NAME = "DecoratorParameters";
  public static final String DECORATOR_PARAMETERS =
      PACKAGE_DECORATOR + PACKAGE_SEPARATOR + SIMPLE_NAME;

  public static final String ID = "id";
  public static final String DECORATOR = "decorator";
  public static final String PARAMETERS = "parameters";

  private final DecoratorPackage decoratorPackage;
  private final DynamicDecoratorMetadata dynamicDecoratorMetadata;

  public DecoratorParametersMetadata(
      DecoratorPackage decoratorPackage, DynamicDecoratorMetadata dynamicDecoratorMetadata) {
    super(SIMPLE_NAME, PACKAGE_DECORATOR);
    this.decoratorPackage = requireNonNull(decoratorPackage);
    this.dynamicDecoratorMetadata = requireNonNull(dynamicDecoratorMetadata);
  }

  @Override
  public void init() {
    setPackage(decoratorPackage);

    setLabel("Decorator Parameters");

    addAttribute(ID, ROLE_ID).setLabel("Identifier");
    addAttribute(DECORATOR)
        .setDataType(XREF)
        .setRefEntity(dynamicDecoratorMetadata)
        .setNillable(false)
        .setLabel("Decorator");
    addAttribute(PARAMETERS)
        .setDataType(TEXT)
        .setNillable(true)
        .setLabel("Parameters")
        .setDescription("Decorator parameters in JSON");
  }
}
