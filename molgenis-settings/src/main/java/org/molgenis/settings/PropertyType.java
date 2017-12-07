package org.molgenis.settings;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.MetaPackage;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.*;
import static org.molgenis.data.meta.model.MetaPackage.PACKAGE_META;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class PropertyType extends SystemEntityType
{
	private static final String SIMPLE_NAME = "Property";

	@SuppressWarnings("unused")
	private static final String PROPERTY_TYPE = PACKAGE_META + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String KEY = "key";
	public static final String VALUE = "value";

	private final MetaPackage metaPackage;

	public PropertyType(MetaPackage metaPackage)
	{
		super(SIMPLE_NAME, PACKAGE_META);
		this.metaPackage = requireNonNull(metaPackage);
	}

	@Override
	public void init()
	{
		setLabel("Property");
		setDescription("Abstract class for key/value properties.");
		setPackage(metaPackage);
		setAbstract(true);
		addAttribute(KEY, ROLE_ID, ROLE_LABEL, ROLE_LOOKUP).setDescription("The key of the property");
		addAttribute(VALUE).setDescription("The value of the property");
	}
}