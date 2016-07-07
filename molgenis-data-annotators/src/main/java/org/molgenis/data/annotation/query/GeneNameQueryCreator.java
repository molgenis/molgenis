package org.molgenis.data.annotation.query;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.annotation.entity.QueryCreator;
import org.molgenis.data.annotation.meta.effects.EffectsMetaData;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static java.util.Collections.singleton;
import static org.molgenis.data.support.QueryImpl.EQ;

@Component
public class GeneNameQueryCreator implements QueryCreator
{
	@Autowired
	AttributeMetaDataFactory attributeMetaDataFactory;

	@Override
	public Collection<AttributeMetaData> getRequiredAttributes()
	{
		return singleton(attributeMetaDataFactory.create().setName(EffectsMetaData.GENE_NAME));
	}

	@Override
	public Query<Entity> createQuery(Entity entity)
	{
		Object value = entity.get(EffectsMetaData.GENE_NAME);
		return EQ(EffectsMetaData.GENE_NAME, value);
	}
}