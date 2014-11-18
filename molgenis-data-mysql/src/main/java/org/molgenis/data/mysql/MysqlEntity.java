package org.molgenis.data.mysql;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Queryable;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class MysqlEntity extends MapEntity
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(MysqlEntity.class);

	private final EntityMetaData metaData;
	private final MysqlRepositoryCollection repositoryCollection;

	public MysqlEntity(EntityMetaData metaData, MysqlRepositoryCollection repositoryCollection)
	{
		assert metaData != null;
		assert repositoryCollection != null;

		this.metaData = metaData;
		this.repositoryCollection = repositoryCollection;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void set(String attributeName, Object value)
	{
		final AttributeMetaData amd = metaData.getAttribute(attributeName);
		if (amd.getDataType() instanceof XrefField && value instanceof Entity)
		{
			Entity e = (Entity) value;
			super.set(attributeName, e.get(amd.getRefEntity().getIdAttribute().getName()));
		}
		else if (amd.getDataType() instanceof MrefField && value instanceof Iterable<?>)
		{
			super.set(attributeName, Iterables.transform((Iterable<Entity>) value, new Function<Object, Object>()
			{
				@Override
				public Object apply(Object input)
				{
					if (input instanceof Entity)
					{
						return ((Entity) input).get(amd.getRefEntity().getIdAttribute().getName());
					}
					else
					{
						return input;
					}
				}
			}));
		}
		else
		{
			super.set(attributeName, value);
		}
	}

	@Override
	public String getIdAttributeName()
	{
		return metaData.getIdAttribute().getName();
	}

	@Override
	public Entity getEntity(String attributeName)
	{
		if (repositoryCollection == null) throw new RuntimeException(
				"getEntities() failed: repositoryCollection not set");

		// if xref, resolve
		AttributeMetaData amd = metaData.getAttribute(attributeName);
		if (amd.getDataType() instanceof XrefField)
		{
			Object obj = get(attributeName);
			if (obj == null)
			{
				return null;
			}

			EntityMetaData ref = amd.getRefEntity();
			Queryable r = repositoryCollection.getUndecoratedRepository(ref.getName());
			if (r == null)
			{
				throw new UnknownEntityException("Unknown entity [" + ref.getName() + "]");
			}

			return r.findOne(new QueryImpl().eq(ref.getIdAttribute().getName(), obj));
		}

		// else throw exception
		throw new IllegalArgumentException(attributeName + " is not an xref");
	}

	@Override
	public Iterable<Entity> getEntities(String attributeName)
	{
		logger.debug("trying getEntities(" + attributeName + ")");
		if (repositoryCollection == null) throw new RuntimeException(
				"getEntities() failed: repositoryCollection not set");

		// if mref, resolve
		AttributeMetaData amd = metaData.getAttribute(attributeName);
		if (get(attributeName) != null && amd.getDataType() instanceof MrefField)
		{
			EntityMetaData ref = amd.getRefEntity();
			Queryable r = repositoryCollection.getUndecoratedRepository(ref.getName());
			return r.findAll(new QueryImpl().in(ref.getIdAttribute().getName(), getList(attributeName)));
		}
		List<Entity> result = new ArrayList<Entity>();
		logger.debug("getEntities(" + attributeName + "): found " + result.size());
		return result;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return metaData;
	}
}
