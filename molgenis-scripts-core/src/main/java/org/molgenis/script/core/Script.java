package org.molgenis.script.core;

import com.google.common.collect.Lists;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static org.molgenis.script.core.ScriptMetaData.*;

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

	@Nullable
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
		return generateToken != null && generateToken;
	}

	public void setGenerateToken(Boolean generateToken)
	{
		set(GENERATE_TOKEN, generateToken);
	}
}
