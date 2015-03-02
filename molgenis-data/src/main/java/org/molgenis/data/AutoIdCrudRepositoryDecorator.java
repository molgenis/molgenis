package org.molgenis.data;

import org.apache.commons.io.IOUtils;
import org.molgenis.fieldtypes.StringField;
import org.molgenis.util.HugeMap;

/**
 * Adds auto id capabilities to a CrudRepository
 */
public class AutoIdCrudRepositoryDecorator extends CrudRepositoryDecorator
{
	private final IdGenerator idGenerator;

	public AutoIdCrudRepositoryDecorator(CrudRepository decoratedRepository, IdGenerator idGenerator)
	{
		super(decoratedRepository);
		this.idGenerator = idGenerator;
	}

	@Override
	public void add(Entity entity)
	{
		AttributeMetaData attr = getEntityMetaData().getIdAttribute();
		if ((attr != null) && attr.isAuto() && (attr.getDataType() instanceof StringField))
		{
			entity.set(attr.getName(), idGenerator.generateId());
		}

		super.add(entity);
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		AttributeMetaData attr = getEntityMetaData().getIdAttribute();
		if ((attr != null) && attr.isAuto() && (attr.getDataType() instanceof StringField))
		{
			HugeMap<Integer, Object> idMap = new HugeMap<>();
			try
			{
				Iterable<? extends Entity> decoratedEntities = new AutoIdEntityIterableDecorator(getEntityMetaData(),
						entities, idGenerator, idMap);
				return super.add(decoratedEntities);
			}
			finally
			{
				IOUtils.closeQuietly(idMap);
			}
		}

		return super.add(entities);
	}

}
