package org.molgenis.app.promise.model;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.app.promise.mapper.PromiseMapperFactory;
import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.RepositoryDecorator;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PromiseMappingProjectMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "PromiseMappingProject";
	public static final String FULLY_QUALIFIED_NAME = PromisePackage.NAME + '_' + ENTITY_NAME;

	public static final String NAME = "name";
	public static final String BIOBANK_ID = "biobank_id";
	public static final String CREDENTIALS = "Credentials";
	public static final String MAPPER = "mapper";

	public static final String LBL_NAME = "Name";
	public static final String LBL_BIOBANK_ID = "Biobank ID";
	public static final String LBL_CREDENTIALS = "Credentials";
	public static final String LBL_MAPPER = "Mapper";

	@Autowired
	public PromiseMappingProjectMetaData(PromiseMapperFactory promiseMapperFactory)
	{
		super(ENTITY_NAME, PromisePackage.getPackage());

		requireNonNull(promiseMapperFactory);

		setLabel("ProMISe mapping projects");
		setDescription("");

		addAttribute(NAME).setIdAttribute(true).setNillable(false).setLabel(LBL_NAME)
				.setDescription("The name of this mapping");
		addAttribute(BIOBANK_ID).setNillable(false).setLabel(LBL_BIOBANK_ID)
				.setDescription("The ID of the biobank in the BBMRI-NL Sample Collections entity").setUnique(true);
		addAttribute(CREDENTIALS).setDataType(MolgenisFieldTypes.XREF).setRefEntity(PromiseCredentialsMetaData.INSTANCE)
				.setNillable(false).setLabel(LBL_CREDENTIALS)
				.setDescription("The ProMISe credentials for this biobank");
		addAttribute(MAPPER).setNillable(false).setLabel(LBL_MAPPER)
				.setDescription("The mapper to use for this biobank");

		setDecorator(new RepositoryDecorator()
		{
			private Repository target;

			@Override
			public void setTarget(Repository target)
			{
				this.target = target;
			}

			@Override
			public Iterator<Entity> iterator()
			{
				return target.iterator();
			}

			@Override
			public void close() throws IOException
			{
				target.close();
			}

			@Override
			public Set<RepositoryCapability> getCapabilities()
			{
				return target.getCapabilities();
			}

			@Override
			public String getName()
			{
				return target.getName();
			}

			@Override
			public EntityMetaData getEntityMetaData()
			{
				return target.getEntityMetaData();
			}

			@Override
			public long count()
			{
				return target.count();
			}

			@Override
			public Query query()
			{
				return target.query();
			}

			@Override
			public long count(Query q)
			{
				return target.count(q);
			}

			@Override
			public Iterable<Entity> findAll(Query q)
			{
				return target.findAll(q);
			}

			@Override
			public Entity findOne(Query q)
			{
				return target.findOne(q);
			}

			@Override
			public Entity findOne(Object id)
			{
				return target.findOne(id);
			}

			@Override
			public Iterable<Entity> findAll(Iterable<Object> ids)
			{
				return target.findAll(ids);
			}

			@Override
			public AggregateResult aggregate(AggregateQuery aggregateQuery)
			{
				return target.aggregate(aggregateQuery);
			}

			@Override
			public void update(Entity entity)
			{
				update(Arrays.asList(entity));
			}

			@Override
			public void update(Iterable<? extends Entity> records)
			{
				for (Entity entity : records)
				{
					if (promiseMapperFactory.getMapper(entity.getString(MAPPER)) == null)
					{
						throw new MolgenisDataException("Unknown mapper + " + entity.getString(MAPPER));
					}
				}
				target.update(records);
			}

			@Override
			public void delete(Entity entity)
			{
				target.delete(entity);
			}

			@Override
			public void delete(Iterable<? extends Entity> entities)
			{
				target.delete(entities);
			}

			@Override
			public void deleteById(Object id)
			{
				target.deleteById(id);
			}

			@Override
			public void deleteById(Iterable<Object> ids)
			{
				target.deleteById(ids);
			}

			@Override
			public void deleteAll()
			{
				target.deleteAll();
			}

			@Override
			public void add(Entity entity)
			{
				add(Arrays.asList(entity));
			}

			@Override
			public Integer add(Iterable<? extends Entity> entities)
			{
				for (Entity entity : entities)
				{
					if (promiseMapperFactory.getMapper(entity.getString(MAPPER)) == null)
					{
						throw new MolgenisDataException("Unknown mapper + " + entity.getString(MAPPER));
					}
				}

				return target.add(entities);
			}

			@Override
			public void flush()
			{
				target.flush();
			}

			@Override
			public void clearCache()
			{
				target.clearCache();
			}

		});
	}
}
