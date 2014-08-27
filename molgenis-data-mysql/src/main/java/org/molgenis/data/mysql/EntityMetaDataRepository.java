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
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.validation.EntityValidator;

import com.google.common.collect.Lists;

public class EntityMetaDataRepository extends MysqlRepository
{
	public static final EntityMetaData META_DATA = new EntityMetaDataMetaData();

	public EntityMetaDataRepository(DataSource dataSource, EntityValidator entityValidator)
	{
		super(dataSource, entityValidator);
		setMetaData(META_DATA);
	}

	public List<DefaultEntityMetaData> getEntityMetaDatas()
	{
		List<DefaultEntityMetaData> meta = Lists.newArrayList();
		for (Entity entity : this)
		{
			meta.add(toEntityMetaData(entity));
		}

		return meta;
	}

	public EntityMetaData getEntityMetaData(String name)
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
