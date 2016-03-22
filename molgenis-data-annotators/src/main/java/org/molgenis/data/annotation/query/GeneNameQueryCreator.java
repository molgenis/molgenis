package org.molgenis.data.annotation.query;

import static org.molgenis.data.support.QueryImpl.EQ;

import java.util.Collection;
import java.util.Collections;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.annotation.entity.QueryCreator;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.VcfEffectsMetaData;

public class GeneNameQueryCreator implements QueryCreator
{
	@Override
	public Collection<AttributeMetaData> getRequiredAttributes()
	{
		return Collections.singleton(new DefaultAttributeMetaData(VcfEffectsMetaData.GENE_NAME));
	}

	@Override
	public Query createQuery(Entity entity)
	{
		Object value = entity.get(VcfEffectsMetaData.GENE_NAME);
		return EQ(VcfEffectsMetaData.GENE_NAME, value);
	}
}