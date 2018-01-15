package org.molgenis.script.core;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.script.core.ScriptPackage.PACKAGE_SCRIPT;

@Component
public class ScriptTypeMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "ScriptType";
	public static final String SCRIPT_TYPE = PACKAGE_SCRIPT + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String NAME = "name";

	private final ScriptPackage scriptPackage;

	ScriptTypeMetaData(ScriptPackage scriptPackage)
	{
		super(SIMPLE_NAME, PACKAGE_SCRIPT);
		this.scriptPackage = requireNonNull(scriptPackage);
	}

	@Override
	public void init()
	{
		setLabel("Script type");
		setPackage(scriptPackage);

		addAttribute(NAME, ROLE_ID).setNillable(false);
	}
}
