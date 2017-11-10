package org.molgenis.data.decorator.meta;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.system.model.RootSystemPackage;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class DynamicDecoratorMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "DynamicDecorator";
	public static final String DYNAMIC_DECORATOR = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String NAME = "name";

	private final RootSystemPackage rootSystemPackage;

	DynamicDecoratorMetadata(RootSystemPackage rootSystemPackage)
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
		this.rootSystemPackage = requireNonNull(rootSystemPackage);
	}

	@Override
	public void init()
	{
		setLabel("Dynamic Decorator");
		setPackage(rootSystemPackage);

		addAttribute(NAME, ROLE_ID);
	}
}
