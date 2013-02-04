package org.molgenis.generators.sql;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisOptions;
import org.molgenis.generators.Generator;
import org.molgenis.model.elements.Entity;
import org.molgenis.model.elements.Model;

import freemarker.template.Template;

public class DerbyCreateSubclassPerTableGen extends Generator
{
	private static final Logger logger = Logger.getLogger(DerbyCreateSubclassPerTableGen.class);

	@Override
	public void generate(Model model, MolgenisOptions options) throws Exception
	{
		// create an hsqldb connection
		Class.forName(options.db_driver);
		Connection conn = DriverManager.getConnection(options.db_uri + ";create=true");

		Statement stmt = null;
		// create generator
		Template template = this.createTemplate(this.getClass().getSimpleName() + ".hsql.ftl");
		Map<String, Object> templateArgs = createTemplateArguments(options);

		for (Entity entity : model.getEntities())
		{
			// create arguments
			templateArgs.put("entity", entity);
			templateArgs.put("model", model);

			// generate
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			template.process(templateArgs, new OutputStreamWriter(out, Charset.forName("UTF-8")));

			// send to database
			stmt = conn.createStatement();
			stmt.executeUpdate(out.toString("UTF-8"));

			// send to log
			logger.debug("created hsql table: " + out.toString("UTF-8"));
		}

		// shutdown
		DriverManager.getConnection("jdbc:derby:;shutdown=true");

		stmt.close();
		conn.close();

	}

	@Override
	public String getDescription()
	{
		return "Generate a hsql database using connection settings";
	}
}
