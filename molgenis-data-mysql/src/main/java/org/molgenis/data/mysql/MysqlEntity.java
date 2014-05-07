package org.molgenis.data.mysql;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;

public class MysqlEntity extends MapEntity
{
	EntityMetaData metaData;
	MysqlRepositoryCollection repositoryCollection;

	public MysqlEntity(EntityMetaData metaData, MysqlRepositoryCollection repositoryCollection)
	{
		assert metaData != null;
		assert repositoryCollection != null;

		this.metaData = metaData;
		this.repositoryCollection = repositoryCollection;
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
			EntityMetaData ref = amd.getRefEntity();
			MysqlRepository r = (MysqlRepository) repositoryCollection.getRepositoryByEntityName(ref.getName());
			return r.findOne(new QueryImpl().eq(ref.getIdAttribute().getName(), get(attributeName)));
		}

		// else throw exception
		throw new IllegalArgumentException(attributeName + " is not an xref");
	}

	@Override
	public Iterable<Entity> getEntities(String attributeName)
	{
		System.out.println("trying getEntities(" + attributeName + ")");
		if (repositoryCollection == null) throw new RuntimeException(
				"getEntities() failed: repositoryCollection not set");

		// if mref, resolve
		AttributeMetaData amd = metaData.getAttribute(attributeName);
		if (get(attributeName) != null && amd.getDataType() instanceof MrefField)
		{
			EntityMetaData ref = amd.getRefEntity();
			MysqlRepository r = (MysqlRepository) repositoryCollection.getRepositoryByEntityName(ref.getName());
			return r.findAll(new QueryImpl().in(ref.getIdAttribute().getName(), getList(attributeName)));
		}
		List<Entity> result = new ArrayList<Entity>();
        System.out.println("getEntities(" + attributeName + "): found "+result.size());
        return result;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return metaData;
	}
}
