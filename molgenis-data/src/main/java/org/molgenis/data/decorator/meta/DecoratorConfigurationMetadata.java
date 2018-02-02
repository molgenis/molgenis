package org.molgenis.data.decorator.meta;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.decorator.meta.DecoratorPackage.PACKAGE_DECORATOR;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class DecoratorConfigurationMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "DecoratorConfiguration";
	public static final String DECORATOR_CONFIGURATION = PACKAGE_DECORATOR + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String ENTITY_TYPE_ID = "entityTypeId";
	public static final String DYNAMIC_DECORATORS = "dynamicDecorators";

	private final DecoratorPackage decoratorPackage;
	private final DynamicDecoratorMetadata dynamicDecoratorMetadata;

	DecoratorConfigurationMetadata(DecoratorPackage decoratorPackage, DynamicDecoratorMetadata dynamicDecoratorMetadata)
	{
		super(SIMPLE_NAME, PACKAGE_DECORATOR);
		this.decoratorPackage = requireNonNull(decoratorPackage);
		this.dynamicDecoratorMetadata = requireNonNull(dynamicDecoratorMetadata);
	}

	@Override
	public void init()
	{
		setLabel("Decorator Configuration");
		setPackage(decoratorPackage);
		setDescription(
				"Configuration entity to configure which dynamic decorators should be applied to an entity type.");

		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false).setLabel("Identifier");
		addAttribute(ENTITY_TYPE_ID).setNillable(false).setUnique(true).setLabel("Entity Type Identifier");
		addAttribute(DYNAMIC_DECORATORS).setNillable(true)
										.setDataType(AttributeType.MREF)
										.setRefEntity(dynamicDecoratorMetadata)
										.setLabel("Decorators");
	}
}
