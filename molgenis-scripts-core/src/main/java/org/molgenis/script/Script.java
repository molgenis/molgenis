package org.molgenis.script;

import com.google.common.collect.Lists;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.RandomStringUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;
import org.molgenis.file.FileStore;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.molgenis.script.ScriptMetaData.*;

public class Script extends StaticEntity
{
	public Script(Entity entity)
	{
		super(entity);
	}

	public Script(EntityType entityType)
	{
		super(entityType);
	}

	public Script(String name, EntityType entityType)
	{
		super(entityType);
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

	public ScriptType getScriptType()
	{
		return getEntity(TYPE, ScriptType.class);
	}

	public void setScriptType(ScriptType scriptType)
	{
		set(TYPE, scriptType);
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
			Template template = new Template(null, new StringReader(getContent()),
					new Configuration(Configuration.VERSION_2_3_21));
			template.process(parameterValues, writer);
		}
		catch (TemplateException | IOException e)
		{
			throw new GenerateScriptException(
					"Error processing parameters for script [" + getName() + "]. " + e.getMessage());
		}
	}
}
