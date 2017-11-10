package org.molgenis.data.decorator.meta;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.system.model.RootSystemPackage;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class DecoratorConfigurationMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "DecoratorConfiguration";
	public static final String DECORATOR_CONFIGURATION = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String ENTITY_TYPE_ID = "entityTypeId";
	public static final String DYNAMIC_DECORATORS = "dynamicDecorators";
	public static final String PARAMETERS = "parameters";

	private final RootSystemPackage rootSystemPackage;
	private final DynamicDecoratorMetadata dynamicDecoratorMetadata;

	DecoratorConfigurationMetadata(RootSystemPackage rootSystemPackage,
			DynamicDecoratorMetadata dynamicDecoratorMetadata)
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
		this.rootSystemPackage = requireNonNull(rootSystemPackage);
		this.dynamicDecoratorMetadata = requireNonNull(dynamicDecoratorMetadata);
	}

	@Override
	public void init()
	{
		setLabel("Decorator Configuration");
		setPackage(rootSystemPackage);

		addAttribute(ID, ROLE_ID).setAuto(true).setLabel("Identifier");
		addAttribute(ENTITY_TYPE_ID).setNillable(false).setUnique(true).setLabel("Entity Type Identifier");
		addAttribute(DYNAMIC_DECORATORS).setNillable(true)
										.setDataType(AttributeType.MREF)
										.setRefEntity(dynamicDecoratorMetadata)
										.setLabel("Decorators");
		addAttribute(PARAMETERS).setNillable(true).setLabel("Parameters");
	}
}
