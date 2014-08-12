package org.molgenis.script;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.RandomStringUtils;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.util.FileStore;
import org.springframework.util.StringUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class Script extends MapEntity
{
	private static final long serialVersionUID = 2462642767382046869L;
	public static final String ENTITY_NAME = "Script";
	public static final String NAME = "name";
	public static final String TYPE = "type";
	public static final String CONTENT = "content";
	public static final String GENERATE_TOKEN = "generateToken";
	public static final String RESULT_FILE_EXTENSION = "resultFileExtension";
	public static final String PARAMETERS = "parameters";
	public static final EntityMetaData META_DATA = new ScriptMetaData();
	private static final Charset CHARSET = Charset.forName("utf-8");

	public Script()
	{
		super(NAME);
	}

	public String getName()
	{
		return getString(NAME);
	}

	public void setName(String name)
	{
		set(NAME, name);
	}

	public String getType()
	{
		return getString(TYPE);
	}

	public void setType(String type)
	{
		set(TYPE, type);
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

	public List<String> getParameters()
	{
		return getList(PARAMETERS);
	}

	public Boolean isGenerateToken()
	{
		return getBoolean(GENERATE_TOKEN);
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

	public File generateScript(FileStore fileStore, String fileExtension, Map<String, Object> parameterValues)
	{
		String name = getName();
		if (name.endsWith(fileExtension))
		{
			name = StringUtils.stripFilenameExtension(name);
		}

		name = name + "_" + RandomStringUtils.randomAlphanumeric(10) + "." + fileExtension;
		File rScriptFile = fileStore.getFile(name);

		Writer w = null;
		try
		{
			Template template = new Template(name + ".ftl", new StringReader(getContent()), new Configuration());
			w = new FileWriterWithEncoding(rScriptFile, CHARSET);
			template.process(parameterValues, w);
		}
		catch (TemplateException | IOException e)
		{
			throw new GenerateScriptException("Error processing parameters for script [" + getName() + "]. "
					+ e.getMessage());
		}
		finally
		{
			IOUtils.closeQuietly(w);
		}

		return rScriptFile;
	}
}
