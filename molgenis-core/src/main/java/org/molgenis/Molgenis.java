package org.molgenis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.molgenis.MolgenisOptions.MapperImplementation;
import org.molgenis.fieldtypes.BoolField;
import org.molgenis.fieldtypes.DateField;
import org.molgenis.fieldtypes.DatetimeField;
import org.molgenis.fieldtypes.DecimalField;
import org.molgenis.fieldtypes.EnumField;
import org.molgenis.fieldtypes.FileField;
import org.molgenis.fieldtypes.HyperlinkField;
import org.molgenis.fieldtypes.ImageField;
import org.molgenis.fieldtypes.IntField;
import org.molgenis.fieldtypes.LongField;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.StringField;
import org.molgenis.fieldtypes.TextField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.generators.DataTypeGen;
import org.molgenis.generators.Generator;
import org.molgenis.generators.R.RApiGen;
import org.molgenis.generators.R.REntityGen;
import org.molgenis.generators.R.RMatrixGen;
import org.molgenis.generators.cpp.CPPCassette;
import org.molgenis.generators.csv.CsvEntityExporterGen;
import org.molgenis.generators.db.DatabaseFactoryGen;
import org.molgenis.generators.db.EntitiesImporterGen;
import org.molgenis.generators.db.EntityImporterGen;
import org.molgenis.generators.db.FillMetadataGen;
import org.molgenis.generators.db.JDBCDatabaseGen;
import org.molgenis.generators.db.JDBCMetaDatabaseGen;
import org.molgenis.generators.db.JpaDatabaseGen;
import org.molgenis.generators.db.JpaMapperGen;
import org.molgenis.generators.db.MapperDecoratorGen;
import org.molgenis.generators.db.MapperSecurityDecoratorGen;
import org.molgenis.generators.db.MultiqueryMapperGen;
import org.molgenis.generators.db.PStatementMapperGen;
import org.molgenis.generators.db.PersistenceGen;
import org.molgenis.generators.doc.DotDocGen;
import org.molgenis.generators.doc.DotDocMinimalGen;
import org.molgenis.generators.doc.DotDocModuleDependencyGen;
import org.molgenis.generators.doc.FileFormatDocGen;
import org.molgenis.generators.doc.ObjectModelDocGen;
import org.molgenis.generators.excel.ExcelEntityExporterGen;
import org.molgenis.generators.excel.ImportWizardExcelPrognosisGen;
import org.molgenis.generators.python.PythonDataTypeGen;
import org.molgenis.generators.server.FrontControllerGen;
import org.molgenis.generators.server.MolgenisContextListenerGen;
import org.molgenis.generators.server.MolgenisGuiServiceGen;
import org.molgenis.generators.server.MolgenisResourceCopyGen;
import org.molgenis.generators.server.RdfApiGen;
import org.molgenis.generators.server.RestApiGen;
import org.molgenis.generators.server.SoapApiGen;
import org.molgenis.generators.server.UsedMolgenisOptionsGen;
import org.molgenis.generators.sql.CountPerEntityGen;
import org.molgenis.generators.sql.CountPerTableGen;
import org.molgenis.generators.sql.DerbyCreateSubclassPerTableGen;
import org.molgenis.generators.sql.FillMetadataTablesGen;
import org.molgenis.generators.sql.HSqlCreateSubclassPerTableGen;
import org.molgenis.generators.sql.MySqlAlterSubclassPerTableGen;
import org.molgenis.generators.sql.MySqlCreateSubclassPerTableGen;
import org.molgenis.generators.sql.OracleCreateSubclassPerTableGen;
import org.molgenis.generators.sql.PSqlCreateSubclassPerTableGen;
import org.molgenis.generators.ui.EasyPluginControllerGen;
import org.molgenis.generators.ui.FormControllerGen;
import org.molgenis.generators.ui.HtmlFormGen;
import org.molgenis.generators.ui.MenuControllerGen;
import org.molgenis.generators.ui.PluginControllerGen;
import org.molgenis.model.MolgenisModel;
import org.molgenis.model.elements.Model;
import org.molgenis.util.cmdline.CmdLineException;

/**
 * MOLGENIS generator. Run this to fire up all the generators. Optionally add
 * {@link org.molgenis.MolgenisOptions}
 * 
 * @author Morris Swertz
 */
public class Molgenis
{
	public static void main(String[] args)
	{
		try
		{
			if (args.length == 2)
			{
				new Molgenis(args[0], args[1]).generate();
			}
			else if (args.length == 3)
			{
				if (args[2].equals("--updatedb"))
				{
					new Molgenis(args[0], args[1]).updateDb(false);
				}
				else if (args[2].equals("--updatedbfillmeta"))
				{
					new Molgenis(args[0], args[1]).updateDb(true);
				}
				else
				{
					throw new Exception("Bad second argument: use either --updatedb or --updatedbfillmeta");
				}
			}
			else
			{
				throw new Exception(
						"You have to provide the molgenis.properties file as first argument to generate Molgenis.\n"
								+ "Alternatively, add the additional argument --updatedb OR --updatedbfillmeta to perform the update database action.\n"
								+ "The --updatedbfillmeta will also insert the metadata into the database.\n"
								+ "Your arguments:\n" + Arrays.toString(args));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	protected transient static final Logger logger = Logger.getLogger("MOLGENIS");
	MolgenisOptions options = null;
	Model model = null;
	List<Generator> generators = new ArrayList<Generator>();

	public List<Generator> getGenerators()
	{
		return generators;
	}

	public void setGenerators(List<Generator> generators)
	{
		this.generators = generators;
	}

	public Molgenis(String propertiesFile, Class<? extends Generator>... generatorsToUse) throws Exception
	{
		this(new MolgenisOptions(propertiesFile), null, generatorsToUse);
	}

	public Molgenis(String propertiesFile, String outputPath, Class<? extends Generator>... generatorsToUse)
			throws Exception
	{
		this(new MolgenisOptions(propertiesFile), outputPath, generatorsToUse);
	}

	public Molgenis(String propertiesFile) throws Exception
	{
		this(new MolgenisOptions(propertiesFile), null, new Class[]
		{});
	}

	public Molgenis(String propertiesFile, String outputPath) throws Exception
	{
		this(new MolgenisOptions(propertiesFile), outputPath, new Class[]
		{});
	}

	public Molgenis()
	{
	}

	public void init(String propertiesFile, String outputPath, Class<? extends Generator>... generatorsToUse)
			throws Exception
	{
		new Molgenis(new MolgenisOptions(propertiesFile), outputPath, generatorsToUse);
	}

	public <E extends Generator> Molgenis(MolgenisOptions options, Class<? extends Generator>... generatorsToUse)
			throws Exception
	{
		this(options, null, generatorsToUse);
	}

	/**
	 * Construct a MOLGENIS generator
	 * 
	 * @param options
	 *            with generator settings
	 * @param generatorsToUse
	 *            optional list of generator classes to include
	 * @throws Exception
	 */
	public <E extends Generator> Molgenis(MolgenisOptions options, String outputPath,
			Class<? extends Generator>... generatorsToUse) throws Exception
	{
		BasicConfigurator.configure();

		this.loadFieldTypes();

		this.options = options;

		Logger.getLogger("freemarker.cache").setLevel(Level.INFO);
		logger.info("\nMOLGENIS version " + org.molgenis.Version.convertToString());
		logger.info("working dir: " + System.getProperty("user.dir"));

		// clean options
		if (outputPath != null)
		{
			// workaround for java string escaping bug in freemarker (see
			// UsedMolgenisOptionsGen.ftl)
			outputPath = outputPath.replace('\\', '/');
			if (!outputPath.endsWith("/")) outputPath = outputPath + "/";
		}

		options.output_src = outputPath != null ? outputPath + options.output_src : options.output_src;
		if (!options.output_src.endsWith("/")) options.output_src = options.output_src.endsWith("/") + "/";
		options.output_python = outputPath != null ? outputPath + options.output_python : options.output_python;
		if (!options.output_python.endsWith("/")) options.output_python = options.output_python + "/";
		options.output_cpp = outputPath != null ? outputPath + options.output_cpp : options.output_cpp;
		if (!options.output_cpp.endsWith("/")) options.output_cpp = options.output_cpp + "/";
		options.output_hand = outputPath != null ? outputPath + options.output_hand : options.output_hand;
		if (!options.output_hand.endsWith("/")) options.output_hand = options.output_hand + "/";
		options.output_sql = outputPath != null ? outputPath + options.output_sql : options.output_sql;
		if (!options.output_sql.endsWith("/")) options.output_sql = options.output_sql + "/";
		options.output_web = outputPath != null ? outputPath + options.output_web : options.output_web;
		if (!options.output_web.endsWith("/")) options.output_web = options.output_web + "/";
		options.output_doc = outputPath != null ? outputPath + options.output_doc : options.output_doc;
		if (!options.output_doc.endsWith("/")) options.output_doc = options.output_doc + "/";

		// USED MOLGENIS OPTIONS
		generators.add(new UsedMolgenisOptionsGen());

		// COPY resources
		if (options.copy_resources)
		{
			generators.add(new MolgenisResourceCopyGen());
		}

		// DOCUMENTATION
		if (options.generate_doc)
		{
			generators.add(new DotDocGen());
			generators.add(new FileFormatDocGen());
			generators.add(new DotDocMinimalGen());
			generators.add(new ObjectModelDocGen());
			generators.add(new DotDocModuleDependencyGen());
		}
		else
		{
			logger.info("Skipping documentation ....");
		}

		if (options.generate_cpp)
		{
			generators.add(new CPPCassette());
		}

		if (options.generate_sql)
		{
			if (options.mapper_implementation.equals(MapperImplementation.JPA))
			{
				System.out.println("--------------JPAGEN--------------");
				generators.add(new JpaDatabaseGen());
				generators.add(new DataTypeGen());
				generators.add(new JpaMapperGen());
				generators.add(new JDBCMetaDatabaseGen());

				if (options.generate_persistence)
				{
					generators.add(new PersistenceGen());
				}
			}
			else
			{
				// DATABASE
				// mysql.org
				if (options.db_driver.equals("com.mysql.jdbc.Driver"))
				{
					generators.add(new MySqlCreateSubclassPerTableGen());
					generators.add(new MySqlAlterSubclassPerTableGen());
					// use multiquery optimization
					if (options.mapper_implementation.equals(MapperImplementation.MULTIQUERY))
					{
						generators.add(new JDBCDatabaseGen());
						generators.add(new DataTypeGen());
						generators.add(new MultiqueryMapperGen());
					}
					else if (options.mapper_implementation.equals(MapperImplementation.PREPARED_STATEMENT))
					{
						generators.add(new JDBCDatabaseGen());
						generators.add(new DataTypeGen());
						generators.add(new PStatementMapperGen());
					}
				} // hsqldb.org
				else if (options.db_driver.equals("oracle.jdbc.driver.OracleDriver"))
				{
					generators.add(new OracleCreateSubclassPerTableGen());
					generators.add(new JDBCDatabaseGen());
					generators.add(new DataTypeGen());
					generators.add(new PStatementMapperGen());
				}
				else if (options.db_driver.equals("org.hsqldb.jdbcDriver"))
				{
					logger.info("HsqlDB generators ....");
					generators.add(new JDBCDatabaseGen());
					generators.add(new DataTypeGen());
					generators.add(new HSqlCreateSubclassPerTableGen());
					generators.add(new PStatementMapperGen());
				} // postgresql
				else if (options.db_driver.equals("org.postgresql.Driver"))
				{
					generators.add(new PSqlCreateSubclassPerTableGen());
					generators.add(new PStatementMapperGen());
				} // h2database.com, branch of hsqldb?
				else if (options.db_driver.equals("org.h2.Driver"))
				{
					generators.add(new HSqlCreateSubclassPerTableGen());
					generators.add(new PStatementMapperGen());
				} // derby, not functional ignore.
				else if (options.db_driver.equals("org.apache.derby.jdbc.EmbeddedDriver"))
				{
					generators.add(new DerbyCreateSubclassPerTableGen());
				}
				else
				{
					logger.warn("Unknown database driver " + options.db_driver);
				}

				// test
				generators.add(new JDBCMetaDatabaseGen());
				// SQL
				generators.add(new CountPerEntityGen());
				generators.add(new CountPerTableGen());
				generators.add(new FillMetadataTablesGen());
			}

			generators.add(new EntityImporterGen());
			generators.add(new EntitiesImporterGen());
			generators.add(new FillMetadataGen());

			// authorization
			if (!options.auth_loginclass.endsWith("SimpleLogin"))
			{
				generators.add(new MapperSecurityDecoratorGen());
			}

			// decorators
			if (options.generate_decorators)
			{
				generators.add(new MapperDecoratorGen());
			}

			// DatabaseFactory
			generators.add(new DatabaseFactoryGen());
		}
		else
		{
			logger.info("SEVERE: Skipping ALL SQL ....");
		}

		if (options.generate_Python)
		{
			generators.add(new PythonDataTypeGen());
		}
		else
		{
			logger.info("Skipping Python interface ....");
		}

		// CSV
		if (options.generate_csv)
		{
			generators.add(new CsvEntityExporterGen());
		}
		else
		{
			logger.info("Skipping CSV importers ....");
		}

		// R
		if (options.generate_R)
		{
			generators.add(new REntityGen());
			generators.add(new RMatrixGen());
			generators.add(new RApiGen());
		}
		else
		{
			logger.info("Skipping R interface ....");
		}

		// always generate frontcontroller
		generators.add(new FrontControllerGen());

		// also generate context
		generators.add(new MolgenisContextListenerGen());

		// optional: the GUI
		if (options.generate_gui)
		{
			generators.add(new MolgenisGuiServiceGen());
		}

		// HTML
		if (options.generate_html)
		{
			generators.add(new HtmlFormGen());
			generators.add(new FormControllerGen());
			generators.add(new MenuControllerGen());
		}
		else
		{
			logger.info("Skipping HTML (HTML,Form,Menu,Tree) ....");
		}

		// SCREEN PLUGIN
		if (options.generate_plugins)
		{
			generators.add(new EasyPluginControllerGen());
		}
		else
		{
			logger.info("Skipping generation of plugins ....");
		}

		// plugin controllers - always need these to map plugins in the GUI
		generators.add(new PluginControllerGen());

		// SOAP
		if (options.generate_soap)
		{
			generators.add(new SoapApiGen());
		}
		else
		{
			logger.info("Skipping SOAP API ....");
		}

		if (options.generate_rest)
		{
			generators.add(new RestApiGen());
		}
		else
		{
			logger.info("Skipping SOAP API ....");
		}

		if (options.generate_rdf)
		{
			generators.add(new RdfApiGen());
		}
		else
		{
			logger.info("Skipping SOAP API ....");
		}

		// Excel
		if (options.generate_ExcelImport)
		{
			generators.add(new ExcelEntityExporterGen());
			generators.add(new ImportWizardExcelPrognosisGen());
		}
		else
		{
			logger.info("Skipping Excel importer ....");
		}
		// RDF

		// FIXME add more generators
		// FIXME use configuration to add the generators

		// clean out generators
		List<Generator> use = new ArrayList<Generator>();
		if (!ArrayUtils.isEmpty(generatorsToUse))
		{
			for (Class<? extends Generator> c : generatorsToUse)
			{
				use.add(c.newInstance());
			}
			generators = use;
		}

		logger.debug("\nUsing generators:\n" + toString());

		// parsing model
		model = MolgenisModel.parse(options);
	}

	private void loadFieldTypes()
	{
		MolgenisFieldTypes.addType(new BoolField());
		MolgenisFieldTypes.addType(new DateField());
		MolgenisFieldTypes.addType(new DatetimeField());
		MolgenisFieldTypes.addType(new DecimalField());
		MolgenisFieldTypes.addType(new EnumField());
		MolgenisFieldTypes.addType(new FileField());
		MolgenisFieldTypes.addType(new ImageField());
		MolgenisFieldTypes.addType(new HyperlinkField());
		MolgenisFieldTypes.addType(new LongField());
		MolgenisFieldTypes.addType(new MrefField());
		MolgenisFieldTypes.addType(new StringField());
		MolgenisFieldTypes.addType(new TextField());
		MolgenisFieldTypes.addType(new XrefField());
		MolgenisFieldTypes.addType(new IntField());
	}

	/**
	 * Apply all generators on the model
	 * 
	 * @param model
	 */
	public void generate() throws Exception
	{
		logger.info("generating ....");
		logger.info("\nUsing options:\n" + options.toString());

		File generatedFolder = new File(options.output_dir);
		if (generatedFolder.exists() && options.delete_generated_folder)
		{
			logger.info("removing previous generated folder " + generatedFolder);
			deleteContentOfDirectory(new File(options.output_src));
			deleteContentOfDirectory(new File(options.output_sql));
		}

		List<Thread> threads = new ArrayList<Thread>();
		for (final Generator g : generators)
		{
			Runnable runnable = new Runnable()
			{

				@Override
				public void run()
				{
					try
					{
						g.generate(model, options);
					}
					catch (Exception e)
					{
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				}
			};
			// executor.execute(runnable);
			Thread thread = new Thread(runnable);
			thread.start();
			threads.add(thread);
		}

		// wait for all threads to complete
		for (Thread thread : threads)
		{
			try
			{
				thread.join();
			}
			catch (InterruptedException ignore)
			{
			}
		}

		logger.info("Generation completed at " + new Date());
	}

	/**
	 * Deletes the content of directory (path), excluding hidden files like .svn
	 * 
	 * @param path
	 *            of directory to delete
	 * @return if and only if the content of directory (path) is successfully
	 *         deleted; false otherwise
	 */
	static public boolean deleteContentOfDirectory(File path)
	{
		boolean result = true;
		if (path.exists())
		{
			File[] files = path.listFiles();
			for (File f : files)
			{
				if (!f.isHidden())
				{
					if (f.isDirectory())
					{
						result &= deleteContentOfDirectory(f);
						boolean ok = f.delete();
						if (!ok) logger.warn("file delete failed: " + f.getName());
					}
					else
					{
						result &= f.delete();
					}
				}
			}
		}
		return result;

	}

	/**
	 * Load the generated SQL into the database.
	 * 
	 * Warning: this will overwrite any existing data in the database!.
	 * 
	 * @throws SQLException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws CmdLineException
	 */
	public void updateDb() throws SQLException, FileNotFoundException, IOException
	{
		updateDb(false);
	}

	public void updateDb(boolean filldb) throws SQLException, FileNotFoundException, IOException
	{

		boolean ask = false;

		// ask for confirmation that the database can be updated
		// TODO: Use or throw away! Make a decision.
		while (ask)
		{
			logger.info("Are you sure that you want overwrite database " + options.db_uri
					+ "?\n All existing data will be overwritten. \nAnswer 'y' or 'n'.\n");
			StringBuilder answerBuilder = new StringBuilder();
			int c;
			while ((c = System.in.read()) != 13)
			{
				answerBuilder.append((char) c);
			}
			String answer = answerBuilder.toString().trim();
			if (answer.trim().equals("y"))
			{
				ask = false;
			}
			else if (answer.equals("n"))
			{
				logger.info("MOLGENIS database update canceled.\n");
				return;
			}
			else
			{
				logger.info("You must answer 'y' or 'n'.");
			}
		}

		// start loading
		BasicDataSource data_src = new BasicDataSource();
		Statement stmt = null;
		Connection conn = null;
		try
		{
			data_src = new BasicDataSource();
			data_src.setDriverClassName(options.db_driver);
			data_src.setUsername(options.db_user);
			data_src.setPassword(options.db_password);
			data_src.setUrl(options.db_uri);

			conn = data_src.getConnection();
			String create_tables_file_str = "create_tables.sql";

			// READ THE FILE
			StringBuilder create_tables_sqlBuilder = new StringBuilder();
			try
			{
				BufferedReader in = new BufferedReader(new InputStreamReader(Thread.currentThread()
						.getContextClassLoader().getResourceAsStream(create_tables_file_str), Charset.forName("UTF-8")));

				try
				{
					String line;
					while ((line = in.readLine()) != null)
					{
						create_tables_sqlBuilder.append(line).append('\n');
					}
				}
				finally
				{
					IOUtils.closeQuietly(in);
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			if (filldb && StringUtils.isNotEmpty(this.options.getAuthLoginclass()))
			{
				String insert_metadata_file = "insert_metadata.sql";
				logger.debug("using file " + insert_metadata_file);

				// READ THE FILE
				try
				{
					BufferedReader in = new BufferedReader(new InputStreamReader(Thread.currentThread()
							.getContextClassLoader().getResourceAsStream(insert_metadata_file),
							Charset.forName("UTF-8")));
					try
					{
						String line;
						while ((line = in.readLine()) != null)
						{
							create_tables_sqlBuilder.append(line).append('\n');
						}
					}
					finally
					{
						IOUtils.closeQuietly(in);
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}

			stmt = conn.createStatement();
			boolean error = false;
			logger.info("Updating database....");
			int i = 0;

			String create_tables_sql = create_tables_sqlBuilder.toString();
			for (String command : create_tables_sql.split(";"))
			{
				if (command.trim().length() > 0)
				{
					try
					{
						logger.debug(command.trim() + ";");
						stmt.executeUpdate(command.trim());

						if (i++ % 10 == 0)
						{
							logger.debug(".");
						}
					}
					catch (Exception e)
					{
						error = true;
						logger.error("\nERROR executing command: " + command + ";\n" + e.getMessage());
					}

				}
			}

			if (error)
			{
				logger.debug("Errors occurred. Make sure you provided sufficient rights! Inside mysql paste the following, assuming your database is called 'molgenis':"
						+ "\ncreate database molgenis; "
						+ "\ngrant all privileges on molgenis.* to molgenis@localhost "
						+ "identified by 'molgenis';"
						+ "\nflush privileges;" + "\nuse molgenis;");
			}

			logger.info("MOLGENIS database updated succesfully");
		}
		finally
		{
			try
			{
				if (stmt != null) stmt.close();
			}
			finally
			{
				if (conn != null) conn.close();
			}
		}
	}

	/**
	 * Report current settings of the generator.
	 */
	@Override
	public final String toString()
	{
		StringBuffer result = new StringBuffer();

		// get name, description and padding
		Map<String, String> map = new LinkedHashMap<String, String>();
		int padding = 0;
		for (Generator g : generators)
		{
			// get the name (without common path)
			String generatorName = null;
			if (g.getClass().getName().indexOf(this.getClass().getPackage().getName()) == 0)
			{
				generatorName = g.getClass().getName().substring(this.getClass().getPackage().getName().length() + 1);
			}
			else
			{
				generatorName = g.getClass().getName();
			}

			// calculate the padding
			padding = Math.max(padding, generatorName.length());

			// add to map
			map.put(generatorName, g.getDescription());
		}

		// print
		for (Map.Entry<String, String> entry : map.entrySet())
		{
			// create padding
			String spaces = "";
			for (int i = entry.getKey().toString().length(); i < padding; i++)
			{
				spaces += " ";
			}
			result.append(entry.getKey() + spaces + " #" + entry.getValue() + "\n");
		}
		return result.toString();
	}

	public MolgenisOptions getMolgenisOptions()
	{
		return this.options;
	}
}