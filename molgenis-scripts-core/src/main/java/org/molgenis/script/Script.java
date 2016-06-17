package org.molgenis.script;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.molgenis.script.ScriptMetaData.CONTENT;
import static org.molgenis.script.ScriptMetaData.GENERATE_TOKEN;
import static org.molgenis.script.ScriptMetaData.NAME;
import static org.molgenis.script.ScriptMetaData.PARAMETERS;
import static org.molgenis.script.ScriptMetaData.RESULT_FILE_EXTENSION;
import static org.molgenis.script.ScriptMetaData.TYPE;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.RandomStringUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.support.StaticEntity;
import org.molgenis.file.FileStore;

import com.google.common.collect.Lists;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class Script extends StaticEntity
{
	public Script(Entity entity)
	{
		super(entity);
	}

	public Script(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public Script(String name, EntityMetaData entityMeta)
	{
		super(entityMeta);
		setName(name);
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
			w = new FileWriterWithEncoding(rScriptFile, UTF_8);
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
