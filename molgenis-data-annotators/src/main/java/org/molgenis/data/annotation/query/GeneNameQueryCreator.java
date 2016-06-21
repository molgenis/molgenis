package org.molgenis.data.annotation.query;

import static java.util.Collections.singleton;
import static org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.GENE_NAME;
import static org.molgenis.data.support.QueryImpl.EQ;
import static org.molgenis.util.ApplicationContextProvider.getApplicationContext;

import java.util.Collection;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.annotation.entity.QueryCreator;
import org.molgenis.data.annotation.entity.impl.SnpEffAnnotator;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;

public class GeneNameQueryCreator implements QueryCreator
{
	@Override
	public Collection<AttributeMetaData> getRequiredAttributes()
	{
		AttributeMetaDataFactory attrMetaFactory = getApplicationContext().getBean(AttributeMetaDataFactory.class);
		return singleton(attrMetaFactory.create().setName(GENE_NAME));
	}

	@Override
	public Query<Entity> createQuery(Entity entity)
	{
		Object value = entity.get(SnpEffAnnotator.GENE_NAME);
		return EQ(SnpEffAnnotator.GENE_NAME, value);
	}
}