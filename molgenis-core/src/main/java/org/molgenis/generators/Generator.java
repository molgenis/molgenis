package org.molgenis.generators;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.molgenis.MolgenisOptions;
import org.molgenis.Version;
import org.molgenis.model.elements.Model;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Template;

public abstract class Generator
{
	protected final String APP_DIR = "app";

	public abstract void generate(Model model, MolgenisOptions options) throws Exception;

	// TODO make abstract (not practical to do at the moment) 
	public void generate(Model model, MolgenisOptions options, String generatedPath) throws Exception
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Create a template for the generators to use.
	 * 
	 * @param path
	 * @throws Exception
	 */
	public Template createTemplate(String path) throws Exception
	{
		freemarker.template.Configuration cfg = new freemarker.template.Configuration();
		cfg.setObjectWrapper(new freemarker.template.DefaultObjectWrapper());

		ClassTemplateLoader loader1 = new ClassTemplateLoader(getClass(), "");
		ClassTemplateLoader loader2 = new ClassTemplateLoader(GeneratorHelper.class, "");
		TemplateLoader[] loaders = new TemplateLoader[]
		{ loader1, loader2 };
		MultiTemplateLoader mLoader = new MultiTemplateLoader(loaders);
		// cfg.setClassForTemplateLoading( this.getClass(), "" ); // NOTE:
		// without
		cfg.setTemplateLoader(mLoader);

		// the '/' on
		// either end
		return cfg.getTemplate(path);
	}

	/**
	 * Create default template arguments such as date, version etc.
	 */
	public Map<String, Object> createTemplateArguments(MolgenisOptions options)
	{
		Map<String, Object> args = new TreeMap<String, Object>();
		// args.put("stringtools", new StringTools());
		Calendar calendar = Calendar.getInstance();
		args.put("year", calendar.get(Calendar.YEAR));
		DateFormat formatter = new SimpleDateFormat("MMMM d, yyyy, HH:mm:ss", Locale.US);
		args.put("datetime", formatter.format(new Date()));
		formatter = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
		args.put("date", formatter.format(new Date()));
		// args.put( "date", calendar.get( Calendar.YEAR ) + "/" +
		// (calendar.get( Calendar.MONTH ) + 1) + "/" + calendar.get(
		// Calendar.DAY_OF_MONTH ) );
		args.put("generator", this.getClass().getName());
		args.put("version", Version.convertToString());
		args.put("helper", new GeneratorHelper(options));
		return args;
	}

	/**
	 * Path for the whole project
	 * 
	 */
	public String getProjectPath(MolgenisOptions options)
	{
		return options.output_src;
	}

	/**
	 * Path for the web sites
	 * 
	 */
	public String getWebserverPath(MolgenisOptions options)
	{
		return options.output_web;
	}

	/**
	 * Generate the path for the generated source code. Depends on wether the
	 * result is a war or a jar file.
	 * 
	 * @param options
	 * @return path string
	 */
	public String getSourcePath(MolgenisOptions options)
	{
		return options.output_src;
	}

	public String getPythonSourcePath(MolgenisOptions options)
	{
		return options.output_python;
	}

	public String getCPPSourcePath(MolgenisOptions options)
	{
		return options.output_cpp;
	}

	public String getHandWrittenPath(MolgenisOptions options)
	{
		return options.output_hand;
	}

	/**
	 * Generate the path for the generated documentation code.
	 * 
	 * @param options
	 * @return path string
	 */
	public String getDocumentationPath(MolgenisOptions options)
	{
		return options.output_doc;
	}

	/**
	 * Generate the path for the generated sql code.
	 * 
	 * @param options
	 * @return path string
	 */
	public String getSqlPath(MolgenisOptions options)
	{
		return options.output_sql;
	}

	/**
	 * Extension varies based on output type, i.e., .java, R, sql etc. Should
	 * include the leading "."
	 */
	public String getExtension()
	{
		return ".java";
	}

	public abstract String getDescription();
}
