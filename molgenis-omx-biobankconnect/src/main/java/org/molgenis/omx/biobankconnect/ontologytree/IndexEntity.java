package org.molgenis.omx.biobankconnect.ontologytree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.AbstractEntity;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchService;

public abstract class IndexEntity extends AbstractEntity
{
	private static final long serialVersionUID = 1L;
	protected final EntityMetaData entityMetaData;
	protected final SearchService searchService;
	protected final Hit hit;

	public IndexEntity(Hit hit, EntityMetaData entityMetaData, SearchService searchService)
	{
		this.entityMetaData = entityMetaData;
		this.searchService = searchService;
		this.hit = hit;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		List<String> attributeNames = new ArrayList<String>();
		for (AttributeMetaData attribute : entityMetaData.getAttributes())
		{
			attributeNames.add(attribute.getName());
		}
		return attributeNames;
	}

	@Override
	public Object getIdValue()
	{
		return hit.getId();
	}

	@Override
	public List<String> getLabelAttributeNames()
	{
		return Arrays.asList(getEntityMetaData().getLabelAttribute().getName());
	}

	@Override
	public void set(String attributeName, Object value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(Entity entity, boolean strict)
	{
		throw new UnsupportedOperationException();
	}
}
