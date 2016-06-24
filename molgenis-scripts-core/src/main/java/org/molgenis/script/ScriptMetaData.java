package org.molgenis.script;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class ScriptMetaData extends DefaultEntityMetaData
{
	public ScriptMetaData()
	{
		super(Script.ENTITY_NAME, Script.class);
		addAttribute(Script.NAME, ROLE_ID).setNillable(false).setLabel("Name");
		addAttribute(Script.TYPE).setNillable(false).setLabel("Type").setDataType(MolgenisFieldTypes.XREF)
				.setRefEntity(ScriptType.META_DATA);
		addAttribute(Script.CONTENT).setNillable(false).setDataType(MolgenisFieldTypes.SCRIPT).setLabel("Content");
		addAttribute(Script.GENERATE_TOKEN).setDataType(MolgenisFieldTypes.BOOL).setLabel("Generate security token")
				.setDefaultValue("false");
		addAttribute(Script.RESULT_FILE_EXTENSION).setNillable(true).setLabel("Result file extension");
		addAttribute(Script.PARAMETERS).setNillable(true).setLabel("Parameters").setDataType(MolgenisFieldTypes.MREF)
				.setRefEntity(ScriptParameter.META_DATA);
	}
}
