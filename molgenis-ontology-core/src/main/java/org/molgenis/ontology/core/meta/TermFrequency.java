package org.molgenis.ontology.core.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

public class TermFrequency extends StaticEntity
{
	public TermFrequency(Entity entity)
	{
		super(entity);
	}

	public TermFrequency(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public TermFrequency(String id, EntityMetaData entityMeta)
	{
		super(entityMeta);
		setId(id);
	}

	public String getId()
	{
		return getString(TermFrequencyMetaData.ID);
	}

	public void setId(String id)
	{
		set(TermFrequencyMetaData.ID, id);
	}

	public String getTerm()
	{
		return getString(TermFrequencyMetaData.TERM);
	}

	public void setTerm(String term)
	{
		set(TermFrequencyMetaData.TERM, term);
	}

	public Double getFrequency()
	{
		return getDouble(TermFrequencyMetaData.FREQUENCY);
	}

	public void setFrequency(Double frequency)
	{
		set(TermFrequencyMetaData.FREQUENCY, frequency);
	}

	public Integer getOccurrence()
	{
		return getInt(TermFrequencyMetaData.OCCURRENCE);
	}

	public void setOccurrence(Integer occurrence)
	{
		set(TermFrequencyMetaData.OCCURRENCE, occurrence);
	}
}
