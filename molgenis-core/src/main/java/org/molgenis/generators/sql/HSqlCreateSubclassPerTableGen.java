package org.molgenis.generators.sql;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.molgenis.MolgenisOptions;
import org.molgenis.generators.Generator;
import org.molgenis.model.MolgenisModel;
import org.molgenis.model.elements.Entity;
import org.molgenis.model.elements.Model;

import freemarker.template.Template;

public class HSqlCreateSubclassPerTableGen extends Generator
{
	private static final Logger logger = Logger.getLogger(HSqlCreateSubclassPerTableGen.class);

	@Override
	public void generate(Model model, MolgenisOptions options) throws Exception
	{
		// create an hsqldb connection
		BasicDataSource data_src = new BasicDataSource();
		data_src.setDriverClassName(options.db_driver);
		data_src.setUsername(options.db_user);
		data_src.setPassword(options.db_password);
		data_src.setUrl(options.db_uri); // a path within the src folder?
		data_src.setMaxIdle(10);
		data_src.setMaxWait(1000);

		Connection conn = data_src.getConnection();
		Statement stmt = null;

		// remove existing database

		// create generator
		Template template = this.createTemplate(this.getClass().getSimpleName() + ".hsql.ftl");
		Map<String, Object> templateArgs = createTemplateArguments(options);

		// Output file for debug
		File target = new File(this.getSqlPath(options) + "/create_tables.sql");
		boolean created = target.getParentFile().mkdirs();
		if (!created && !target.getParentFile().exists())
		{
			throw new IOException("could not create " + target.getParentFile());
		}

		OutputStream targetOut = new FileOutputStream(target);
		List<Entity> sortedlist = model.getEntities();
		sortedlist = MolgenisModel.sortEntitiesByDependency(sortedlist, model);

		// create arguments
		templateArgs.put("entities", sortedlist);
		templateArgs.put("model", model);

		// generate
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try
		{
			template.process(templateArgs, new OutputStreamWriter(out, Charset.forName("UTF-8")));
			// Write to file for debug
			targetOut.write(out.toByteArray());

			// Update the Hsql database
			stmt = conn.createStatement();
			stmt.executeUpdate(out.toString("UTF-8"));
		}
		catch (Exception e)
		{
			logger.debug("Something wrong with Code:" + out.toString("UTF-8") + " \n Error:" + e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}

		for (Entity e : sortedlist)
		{
			logger.debug("Created hsql table for : " + e.getName());
		}
		stmt.executeUpdate("SHUTDOWN");
		targetOut.close();
		stmt.close();
		conn.close();
	}

	@Override
	public String getDescription()
	{
		return "Generate a hsql database using connection settings";
	}
}
