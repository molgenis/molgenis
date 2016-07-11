package org.molgenis.script;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.script.ScriptPackage.PACKAGE_SCRIPT;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScriptTypeMetaData extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "ScriptType";
	public static final String SCRIPT_TYPE = PACKAGE_SCRIPT + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String NAME = "name";

	private final ScriptPackage scriptPackage;

	@Autowired
	ScriptTypeMetaData(ScriptPackage scriptPackage)
	{
		super(SIMPLE_NAME, PACKAGE_SCRIPT);
		this.scriptPackage = requireNonNull(scriptPackage);
	}

	@Override
	public void init()
	{
		setPackage(scriptPackage);

		addAttribute(NAME, ROLE_ID).setNillable(false);
	}
}
