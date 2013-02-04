package org.molgenis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.generators.DataTypeGen;
import org.molgenis.generators.Generator;
import org.molgenis.model.MolgenisModel;
import org.molgenis.model.MolgenisModelException;
import org.molgenis.model.MolgenisModelParser;
import org.molgenis.model.MolgenisModelValidator;
import org.molgenis.model.elements.Entity;
import org.molgenis.model.elements.Model;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

public abstract class GeneratorTestCase
{
	private static final Logger LOG = Logger.getLogger(GeneratorTestCase.class);

	private static final String SRC_PATH = "generated/test/java/";
	private static final String BUILD_PATH = "build/test/classes/";

	private static Model MODEL;
	private static MolgenisOptions MODEL_OPTIONS;

	@BeforeSuite
	public static void setUpBeforeSuite() throws Exception
	{
		// create test model
		Model model = getModel();
		if (model == null || model.getModules() == null || model.getModules().isEmpty())
		{
			throw new MolgenisModelException("model should contain at least one module");
		}
		MODEL = model;
		MODEL_OPTIONS = new MolgenisOptions();
		MODEL_OPTIONS.output_src = SRC_PATH;

		// generate model entities
		List<Entity> entityList = MODEL.getEntities();
		entityList = MolgenisModel.sortEntitiesByDependency(entityList, MODEL);

		DataTypeGen entityGenerator = new DataTypeGen();
		entityGenerator.generate(MODEL, MODEL_OPTIONS);
		LOG.debug("generated model entities");

		// compile model entities
		List<String> compileList = new ArrayList<String>(entityList.size());
		char sep = File.separatorChar;
		for (Entity entity : entityList)
		{
			String entityPath = entity.getModule().getName().replace('.', sep) + sep;
			String generatedJavaFile = SRC_PATH + entityPath + entity.getName() + ".java";
			compileList.add(generatedJavaFile);
		}
		compile(compileList, BUILD_PATH);
	}

	@BeforeClass
	public void setUpBeforeClass() throws Exception
	{
		// generate using test model
		String className = this.getClass().getName().replace('.', File.separatorChar).replace("Test", "");
		String generatedJavaFileName = SRC_PATH + className + ".java";

		getGenerator().generate(MODEL, MODEL_OPTIONS, generatedJavaFileName);
		LOG.debug("generated: " + generatedJavaFileName);

		// compile generated java file
		compile(Arrays.asList(generatedJavaFileName), BUILD_PATH);
		LOG.debug("compiled: " + generatedJavaFileName);
	}

	protected abstract Generator getGenerator() throws MolgenisModelException;

	private static void compile(List<String> javaPaths, String outputPath) throws IOException
	{
		// compile generate file
		JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();

		File generatedClassFolder = new File(outputPath);
		boolean created = generatedClassFolder.mkdirs();
		if (!created && !generatedClassFolder.exists())
		{
			throw new IOException("could not create " + generatedClassFolder);
		}
		String classPath = System.getProperty("java.class.path") + ";src" + ";" + outputPath;
		System.out.println(classPath);

		List<String> compilerArgs = new ArrayList<String>();
		compilerArgs.add("-cp");
		compilerArgs.add(classPath);
		compilerArgs.add("-d");
		compilerArgs.add(generatedClassFolder.getPath());
		compilerArgs.add("-Xlint");
		compilerArgs.add("-g");
		compilerArgs.addAll(javaPaths);

		int returnCode = javaCompiler.run(null, null, null, compilerArgs.toArray(new String[0]));
		if (returnCode != 0) throw new IOException("compilation failed: " + javaPaths);
	}

	private static Model getModel() throws MolgenisModelException, DatabaseException
	{
		String xml = "<molgenis name=\"org.molgenis\">\n"
				+ "	<module name=\"model\">\n"
				+ "		<description>Model to test generated code</description>\n"
				+ "		<entity name=\"Autoid\" abstract=\"true\" system=\"true\">\n"
				+ "			<field name=\"id\" type=\"autoid\" hidden=\"true\" />\n"
				+ "		</entity>\n"
				+ "		<entity name=\"Identifiable\" implements=\"Autoid\" abstract=\"true\" system=\"true\" xref_label=\"Identifier\">\n"
				+ "			<field name=\"Identifier\" type=\"string\" />\n"
				+ "			<field name=\"Name\" type=\"string\" description=\"assign name\" />\n"
				+ "			<unique fields=\"Identifier\" />\n"
				+ "			<unique fields=\"Name,Identifier\" />\n"
				+ "		</entity>\n"
				+ "		<entity name=\"Characteristic\" implements=\"Identifiable\" xref_label=\"Identifier\">\n"
				+ "			<field name=\"description\" type=\"text\" nillable=\"true\" />\n"
				+ "		</entity>\n"
				+ "		<entity name=\"Feature\" extends=\"Characteristic\">\n"
				+ "			<field name=\"dataType\" type=\"enum\" default=\"string\" enum_options=\"[xref,string,nominal,ordinal,date,datetime,int,code,image,decimal,bool,file,log,data,exe]\" />\n"
				+ "		</entity>\n" + "		<entity name=\"Category\" implements=\"Autoid\">\n"
				+ "			<field name=\"feature\" type=\"xref\" xref_entity=\"Feature\" />\n" + "		</entity>\n"
				+ "	</module>\n" + "</molgenis>";

		// TODO programmatically define model instead of parsing from XML
		// generate model
		Model model = MolgenisModelParser.parseDbSchema(xml);
		// validate model & resolve model dependencies
		MolgenisModelValidator.validate(model, new MolgenisOptions());
		return model;
	}
}
