package org.molgenis.data.system.core;

import static org.molgenis.data.meta.system.FreemarkerTemplateMetaData.ID;
import static org.molgenis.data.meta.system.FreemarkerTemplateMetaData.NAME;
import static org.molgenis.data.meta.system.FreemarkerTemplateMetaData.VALUE;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.SystemEntity;
import org.molgenis.data.meta.system.FreemarkerTemplateMetaData;

public class FreemarkerTemplate extends SystemEntity
{
	public FreemarkerTemplate(Entity entity)
	{
		super(entity);
	}

	public FreemarkerTemplate(FreemarkerTemplateMetaData freemarkerTemplateMetaData)
	{
		super(freemarkerTemplateMetaData);
	}

	public FreemarkerTemplate(String id, FreemarkerTemplateMetaData freemarkerTemplateMetaData)
	{
		super(freemarkerTemplateMetaData);
		setId(id);
	}

	public String getId()
	{
		return getString(ID);
	}

	public void setId(String id)
	{
		set(ID, id);
	}

	public String getName()
	{
		return getString(NAME);
	}

	public void setName(String name)
	{
		set(NAME, name);
	}

	public String getValue()
	{
		return getString(VALUE);
	}

	public void setValue(String value)
	{
		set(VALUE, value);
	}
}
