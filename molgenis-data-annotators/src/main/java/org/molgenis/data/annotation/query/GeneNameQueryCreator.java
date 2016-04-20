package org.molgenis.data.annotation.query;

import static org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.GENE_NAME;
import static org.molgenis.data.support.QueryImpl.EQ;

import java.util.Collection;
import java.util.Collections;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.annotation.entity.QueryCreator;
import org.molgenis.data.annotation.entity.impl.SnpEffAnnotator;
import org.molgenis.data.meta.AttributeMetaData;

public class GeneNameQueryCreator implements QueryCreator
{
	@Override
	public Collection<AttributeMetaData> getRequiredAttributes()
	{
		return Collections.singleton(new AttributeMetaData(GENE_NAME));
	}

	@Override
	public Query<Entity> createQuery(Entity entity)
	{
		Object value = entity.get(SnpEffAnnotator.GENE_NAME);
		return EQ(SnpEffAnnotator.GENE_NAME, value);
	}
}