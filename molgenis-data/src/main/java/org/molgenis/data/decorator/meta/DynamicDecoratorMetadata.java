package org.molgenis.data.decorator.meta;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.decorator.meta.DecoratorPackage.PACKAGE_DECORATOR;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class DynamicDecoratorMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "DynamicDecorator";
	public static final String DYNAMIC_DECORATOR = PACKAGE_DECORATOR + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";

	private final DecoratorPackage decoratorPackage;

	DynamicDecoratorMetadata(DecoratorPackage decoratorPackage)
	{
		super(SIMPLE_NAME, PACKAGE_DECORATOR);
		this.decoratorPackage = requireNonNull(decoratorPackage);
	}

	@Override
	public void init()
	{
		setLabel("Dynamic Decorator");
		setPackage(decoratorPackage);

		addAttribute(ID, ROLE_ID).setLabel("Identifier");
	}
}
