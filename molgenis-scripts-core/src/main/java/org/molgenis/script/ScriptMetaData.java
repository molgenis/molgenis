package org.molgenis.script;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;
import static org.molgenis.script.ScriptPackage.PACKAGE_SCRIPT;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScriptMetaData extends SystemEntityMetaDataImpl
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
	private ScriptParameterMetaData scriptParameterMetaData;
	private ScriptTypeMetaData scriptTypeMetaData;

	@Autowired
	ScriptMetaData(ScriptPackage scriptPackage)
	{
		super(SIMPLE_NAME, PACKAGE_SCRIPT);
		this.scriptPackage = requireNonNull(scriptPackage);
	}

	@Override
	public void init()
	{
		setPackage(scriptPackage);

		addAttribute(NAME, ROLE_ID).setNillable(false).setLabel("Name");
		addAttribute(TYPE).setNillable(false).setLabel("Type").setDataType(MolgenisFieldTypes.XREF)
				.setRefEntity(scriptTypeMetaData);
		addAttribute(CONTENT).setNillable(false).setDataType(MolgenisFieldTypes.SCRIPT).setLabel("Content");
		addAttribute(GENERATE_TOKEN).setDataType(MolgenisFieldTypes.BOOL).setLabel("Generate security token")
				.setDefaultValue("false");
		addAttribute(RESULT_FILE_EXTENSION).setNillable(true).setLabel("Result file extension");
		addAttribute(PARAMETERS).setNillable(true).setLabel("Parameters").setDataType(MolgenisFieldTypes.MREF)
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
