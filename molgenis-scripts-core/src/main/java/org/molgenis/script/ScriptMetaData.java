package org.molgenis.script;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.auth.MolgenisUserMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScriptMetaData extends SystemEntityMetaDataImpl
{
	private ScriptParameterMetaData scriptParameterMetaData;
	private ScriptTypeMetaData scriptTypeMetaData;

	@Override
	public void init()
	{
		setName(Script.ENTITY_NAME);
		addAttribute(Script.NAME, ROLE_ID).setNillable(false).setLabel("Name");
		addAttribute(Script.TYPE).setNillable(false).setLabel("Type").setDataType(MolgenisFieldTypes.XREF)
				.setRefEntity(scriptTypeMetaData);
		addAttribute(Script.CONTENT).setNillable(false).setDataType(MolgenisFieldTypes.SCRIPT).setLabel("Content");
		addAttribute(Script.GENERATE_TOKEN).setDataType(MolgenisFieldTypes.BOOL).setLabel("Generate security token")
				.setDefaultValue("false");
		addAttribute(Script.RESULT_FILE_EXTENSION).setNillable(true).setLabel("Result file extension");
		addAttribute(Script.PARAMETERS).setNillable(true).setLabel("Parameters").setDataType(MolgenisFieldTypes.MREF)
				.setRefEntity(scriptParameterMetaData);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setScriptParameterMetaData(ScriptParameterMetaData scriptParameterMetaData)
	{
		this.scriptParameterMetaData = requireNonNull(scriptParameterMetaData);
	}

	@Autowired
	public void setScriptTypeMetaData(ScriptTypeMetaData scriptTypeMetaData)
	{
		this.scriptTypeMetaData = requireNonNull(scriptTypeMetaData);
	}
}
