package org.molgenis.data.mysql;

import static org.molgenis.data.mysql.EntityMetaDataMetaData.ABSTRACT;
import static org.molgenis.data.mysql.EntityMetaDataMetaData.DESCRIPTION;
import static org.molgenis.data.mysql.EntityMetaDataMetaData.EXTENDS;
import static org.molgenis.data.mysql.EntityMetaDataMetaData.ID_ATTRIBUTE;
import static org.molgenis.data.mysql.EntityMetaDataMetaData.LABEL;
import static org.molgenis.data.mysql.EntityMetaDataMetaData.NAME;

import java.util.List;

import javax.sql.DataSource;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.EntityMetaDataRepository;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;

import com.google.common.collect.Lists;

public class MysqlEntityMetaDataRepository extends MysqlRepository implements EntityMetaDataRepository
{
	public static final EntityMetaDataMetaData META_DATA = new EntityMetaDataMetaData();

	public MysqlEntityMetaDataRepository(DataSource dataSource)
	{
		super(dataSource);
		setMetaData(META_DATA);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.mysql.EntityMetaDataRepository#getEntityMetaDatas()
	 */
	@Override
	public Iterable<EntityMetaData> getEntityMetaDatas()
	{
		List<EntityMetaData> meta = Lists.newArrayList();
		for (Entity entity : this)
		{
			meta.add(toEntityMetaData(entity));
		}

		return meta;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.mysql.EntityMetaDataRepository#getEntityMetaData(java.lang.String)
	 */
	@Override
	public DefaultEntityMetaData getEntityMetaData(String name)
	{
		Entity entity = findOne(name);
		if (entity == null)
		{
			return null;
		}

		return toEntityMetaData(entity);
	}

	private DefaultEntityMetaData toEntityMetaData(Entity entity)
	{
		String name = entity.getString(NAME);
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData(name);
		entityMetaData.setAbstract(entity.getBoolean(ABSTRACT));
		entityMetaData.setIdAttribute(entity.getString(ID_ATTRIBUTE));
		entityMetaData.setLabel(entity.getString(LABEL));
		entityMetaData.setDescription(entity.getString(DESCRIPTION));

		// Extends
		String extendsEntityName = entity.getString(EXTENDS);
		if (extendsEntityName != null)
		{
			EntityMetaData extendsEmd = getEntityMetaData(extendsEntityName);
			if (extendsEmd == null) throw new MolgenisDataException("Missing super entity [" + extendsEntityName
					+ "] of entity [" + name + "]");
			entityMetaData.setExtends(extendsEmd);
		}

		return entityMetaData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.mysql.EntityMetaDataRepository#addEntityMetaData(org.molgenis.data.EntityMetaData)
	 */
	@Override
	public void addEntityMetaData(EntityMetaData emd)
	{
		Entity entityMetaDataEntity = new MapEntity();
		entityMetaDataEntity.set(NAME, emd.getName());
		entityMetaDataEntity.set(DESCRIPTION, emd.getDescription());
		entityMetaDataEntity.set(ABSTRACT, emd.isAbstract());
		if (emd.getIdAttribute() != null) entityMetaDataEntity.set(ID_ATTRIBUTE, emd.getIdAttribute().getName());
		entityMetaDataEntity.set(LABEL, emd.getLabel());
		if (emd.getExtends() != null) entityMetaDataEntity.set(EXTENDS, emd.getExtends().getName());

		add(entityMetaDataEntity);
	}

}
