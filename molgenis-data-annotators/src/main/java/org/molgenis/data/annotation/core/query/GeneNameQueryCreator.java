package org.molgenis.data.annotation.core.query;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.annotation.core.effects.EffectsMetaData;
import org.molgenis.data.annotation.core.entity.QueryCreator;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static java.util.Collections.singleton;
import static org.molgenis.data.support.QueryImpl.EQ;

@Component
public class GeneNameQueryCreator implements QueryCreator
{
	@Autowired
	AttributeFactory attributeFactory;

	@Override
	public Collection<Attribute> getRequiredAttributes()
	{
		return singleton(attributeFactory.create().setName(EffectsMetaData.GENE_NAME));
	}

	@Override
	public Query<Entity> createQuery(Entity entity)
	{
		Object value = entity.get(EffectsMetaData.GENE_NAME);
		return EQ(EffectsMetaData.GENE_NAME, value);
	}
}