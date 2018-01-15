package org.molgenis.script.core;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.script.core.ScriptPackage.PACKAGE_SCRIPT;

@Component
public class ScriptMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "Script";
	public static final String SCRIPT = PACKAGE_SCRIPT + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String NAME = "name";
	public static final String TYPE = "type";// The ScriptType like r
	public static final String CONTENT = "content";// The freemarker code
	public static final String GENERATE_TOKEN = "generateToken";// If true a security token is generated for the script
	// (available as ${molgenisToken})
	public static final String RESULT_FILE_EXTENSION = "resultFileExtension"; // If the script generates an outputfile,
	// this is it's file extension
	// (outputfile available as
	// ${outputFile})
	public static final String PARAMETERS = "parameters";// The names of the parameters required by this script

	private final ScriptPackage scriptPackage;
	private final ScriptParameterMetaData scriptParameterMetaData;
	private final ScriptTypeMetaData scriptTypeMetaData;

	ScriptMetaData(ScriptPackage scriptPackage, ScriptParameterMetaData scriptParameterMetaData,
			ScriptTypeMetaData scriptTypeMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_SCRIPT);
		this.scriptPackage = requireNonNull(scriptPackage);
		this.scriptParameterMetaData = requireNonNull(scriptParameterMetaData);
		this.scriptTypeMetaData = requireNonNull(scriptTypeMetaData);
	}

	@Override
	public void init()
	{
		setLabel("Script");
		setPackage(scriptPackage);

		addAttribute(NAME, ROLE_ID).setNillable(false).setLabel("Name");
		addAttribute(TYPE).setNillable(false).setLabel("Type").setDataType(XREF).setRefEntity(scriptTypeMetaData);
		addAttribute(CONTENT).setNillable(false).setDataType(AttributeType.SCRIPT).setLabel("Content");
		addAttribute(GENERATE_TOKEN).setDataType(BOOL).setLabel("Generate security token").setDefaultValue("false");
		addAttribute(RESULT_FILE_EXTENSION).setNillable(true).setLabel("Result file extension");
		addAttribute(PARAMETERS).setNillable(true)
								.setLabel("Parameters")
								.setDataType(MREF)
								.setRefEntity(scriptParameterMetaData);
	}
}
