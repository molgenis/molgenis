package org.molgenis.script;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.RandomStringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.file.FileStore;

import com.google.common.collect.Lists;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class Script extends DefaultEntity
{
	private static final long serialVersionUID = 2462642767382046869L;
	public static final String ENTITY_NAME = "Script";
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
															// (excluding 'molgenisToken' and 'outputFile'
	public static final EntityMetaData META_DATA = new ScriptMetaData();
	private static final Charset CHARSET = Charset.forName("utf-8");

	public Script(DataService dataService)
	{
		super(META_DATA, dataService);
	}

	public String getName()
	{
		return getString(NAME);
	}

	public void setName(String name)
	{
		set(NAME, name);
	}

	public ScriptType getType()
	{
		return getEntity(TYPE, ScriptType.class);
	}

	public String getContent()
	{
		return getString(CONTENT);
	}

	public void setContent(String content)
	{
		set(CONTENT, content);
	}

	public String getResultFileExtension()
	{
		return getString(RESULT_FILE_EXTENSION);
	}

	public void setResultFileExtension(String resultFileExtension)
	{
		set(RESULT_FILE_EXTENSION, resultFileExtension);
	}

	public List<ScriptParameter> getParameters()
	{
		Iterable<ScriptParameter> params = getEntities(PARAMETERS, ScriptParameter.class);
		if (params == null) return Collections.emptyList();
		return Lists.newArrayList(params);
	}

	public boolean isGenerateToken()
	{
		Boolean generateToken = getBoolean(GENERATE_TOKEN);
		return generateToken != null && generateToken.booleanValue();
	}

	public void setGenerateToken(Boolean generateToken)
	{
		set(GENERATE_TOKEN, generateToken);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return META_DATA;
	}

	public String generateScript(Map<String, Object> parameterValues)
	{
		StringWriter stringWriter = new StringWriter();
		String script;
		try
		{
			generateScript(parameterValues, stringWriter);
			script = stringWriter.toString();
		}
		finally
		{
			try
			{
				stringWriter.close();
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		return script;
	}

	public File generateScript(FileStore fileStore, String fileExtension, Map<String, Object> parameterValues)
	{
		String name = RandomStringUtils.randomAlphanumeric(10) + "." + fileExtension;
		File rScriptFile = fileStore.getFile(name);

		Writer w = null;
		try
		{
			w = new FileWriterWithEncoding(rScriptFile, CHARSET);
			generateScript(parameterValues, w);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			IOUtils.closeQuietly(w);
		}

		return rScriptFile;
	}

	private void generateScript(Map<String, Object> parameterValues, Writer writer)
	{
		try
		{
			Template template = new Template(null, new StringReader(getContent()), new Configuration());
			template.process(parameterValues, writer);
		}
		catch (TemplateException | IOException e)
		{
			throw new GenerateScriptException(
					"Error processing parameters for script [" + getName() + "]. " + e.getMessage());
		}
	}
}
