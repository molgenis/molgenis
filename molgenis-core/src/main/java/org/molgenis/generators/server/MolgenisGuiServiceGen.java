package org.molgenis.generators.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisOptions;
import org.molgenis.generators.Generator;
import org.molgenis.model.elements.Model;

import freemarker.template.Template;

public class MolgenisGuiServiceGen extends Generator
{
	private static final Logger logger = Logger.getLogger(MolgenisGuiServiceGen.class);

	@Override
	public String getDescription()
	{
		return "Generates the central servlet for MOLGENIS.";
	}

	@Override
	public void generate(Model model, MolgenisOptions options) throws Exception
	{
		Template template = createTemplate(this.getClass().getSimpleName() + ".ftl");
		Map<String, Object> templateArgs = createTemplateArguments(options);

		File target = new File(this.getSourcePath(options) + APP_DIR + "/servlet/GuiService.java");
		boolean created = target.getParentFile().mkdirs();
		if (!created && !target.getParentFile().exists())
		{
			throw new IOException("could not create " + target.getParentFile());
		}

		templateArgs.put("model", model);
		templateArgs.put("package", APP_DIR);
		templateArgs.put("db_filepath", options.db_filepath);
		templateArgs.put("loginclass", options.auth_loginclass);
		templateArgs.put("auth_redirect", options.auth_redirect);
		templateArgs.put("databaseImp",
				options.mapper_implementation.equals(MolgenisOptions.MapperImplementation.JPA) ? "jpa" : "jdbc");
		templateArgs.put("db_mode", options.db_mode);
		templateArgs.put("generate_BOT", options.generate_BOT);
		templateArgs.put("db_driver", options.db_driver);
		templateArgs.put("db_uri", options.db_uri);
		templateArgs.put("db_user", options.db_user);
		templateArgs.put("db_password", options.db_password);

		templateArgs.put("mail_smtp_from", options.mail_smtp_from);
		templateArgs.put("mail_smtp_protocol", options.mail_smtp_protocol);
		templateArgs.put("mail_smtp_hostname", options.mail_smtp_hostname);
		templateArgs.put("mail_smtp_port", options.mail_smtp_port);
		templateArgs.put("mail_smtp_user", options.mail_smtp_user);
		templateArgs.put("mail_smtp_au", options.mail_smtp_au);

		templateArgs.put("linkout_overlay", options.render_decorator);
		templateArgs.put("generate_soap", options.generate_soap);

		OutputStream targetOut = new FileOutputStream(target);
		template.process(templateArgs, new OutputStreamWriter(targetOut, Charset.forName("UTF-8")));
		targetOut.close();

		logger.info("generated " + target);
	}

	public static String backlashReplace(String myStr)
	{
		final StringBuilder result = new StringBuilder();
		final StringCharacterIterator iterator = new StringCharacterIterator(myStr);
		char character = iterator.current();
		while (character != CharacterIterator.DONE)
		{

			if (character == '\\')
			{
				logger.error("HERE IT HAPPENS");
				result.append('/');
			}
			else
			{
				result.append(character);
			}

			character = iterator.next();
		}
		return result.toString();
	}
}
