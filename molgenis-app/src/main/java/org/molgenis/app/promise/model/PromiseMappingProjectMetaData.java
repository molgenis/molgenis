package org.molgenis.app.promise.model;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.app.promise.mapper.PromiseMapperFactory;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

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

		AttributeMetaData idAttribute = addAttribute(NAME).setNillable(false).setLabel(LBL_NAME)
				.setDescription("The name of this mapping");
		setIdAttribute(idAttribute);

		addAttribute(BIOBANK_ID).setNillable(false).setLabel(LBL_BIOBANK_ID)
				.setDescription("The ID of the biobank in the BBMRI-NL Sample Collections entity").setUnique(true);
		addAttribute(CREDENTIALS).setDataType(MolgenisFieldTypes.XREF).setRefEntity(PromiseCredentialsMetaData.INSTANCE)
				.setNillable(false).setLabel(LBL_CREDENTIALS)
				.setDescription("The ProMISe credentials for this biobank");
		addAttribute(MAPPER).setNillable(false).setLabel(LBL_MAPPER)
				.setDescription("The mapper to use for this biobank");

//		setDecorator(new RepositoryDecorator()
//		{
//			private Repository target;
//
//			@Override
//			public Stream<Entity> stream(Fetch fetch)
//			{
//				return null;
//			}
//
//			@Override public Entity findOne(Object id, Fetch fetch)
//			{
//				return null;
//			}
//
//			@Override public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
//			{
//				return null;
//			}
//
//			@Override public void create(){}
//
//			@Override public void drop(){}
//
//			@Override public void rebuildIndex(){}
//
//			@Override public void addEntityListener(EntityListener entityListener){}
//
//			@Override public void removeEntityListener(EntityListener entityListener){}
//
//			@Override
//			public void setTarget(Repository target)
//			{
//				this.target = target;
//			}
//
//			@Override
//			public Iterator<Entity> iterator()
//			{
//				return target.iterator();
//			}
//
//			@Override
//			public void close() throws IOException
//			{
//				target.close();
//			}
//
//			@Override
//			public Set<RepositoryCapability> getCapabilities()
//			{
//				return target.getCapabilities();
//			}
//
//			@Override
//			public String getName()
//			{
//				return target.getName();
//			}
//
//			@Override
//			public EntityMetaData getEntityMetaData()
//			{
//				return target.getEntityMetaData();
//			}
//
//			@Override
//			public long count()
//			{
//				return target.count();
//			}
//
//			@Override
//			public Query query()
//			{
//				return target.query();
//			}
//
//			@Override
//			public long count(Query q)
//			{
//				return target.count(q);
//			}
//
//			@Override
//			public Stream<Entity> findAll(Query q)
//			{
//				return target.findAll(q);
//			}
//
//			@Override
//			public Entity findOne(Query q)
//			{
//				return target.findOne(q);
//			}
//
//			@Override
//			public Entity findOne(Object id)
//			{
//				return target.findOne(id);
//			}
//
//			@Override
//			public Stream<Entity> findAll(Stream<Object> ids)
//			{
//				return target.findAll(ids);
//			}
//
//			@Override
//			public AggregateResult aggregate(AggregateQuery aggregateQuery)
//			{
//				return target.aggregate(aggregateQuery);
//			}
//
//			@Override
//			public void update(Entity entity)
//			{
//				update(asList(entity).stream());
//			}
//
//			@Override
//			public void update(Stream<? extends Entity> records)
//			{
//				Iterable<Entity> entities = records.collect(toList());
//				for (Entity entity : entities)
//				{
//					if (promiseMapperFactory.getMapper(entity.getString(MAPPER)) == null)
//					{
//						throw new MolgenisDataException("Unknown mapper + " + entity.getString(MAPPER));
//					}
//				}
//				target.update(records);
//			}
//
//			@Override
//			public void delete(Entity entity)
//			{
//				target.delete(entity);
//			}
//
//			@Override
//			public void delete(Stream<? extends Entity> entities)
//			{
//				target.delete(entities);
//			}
//
//			@Override
//			public void deleteById(Object id)
//			{
//				target.deleteById(id);
//			}
//
//			@Override
//			public void deleteById(Stream<Object> ids)
//			{
//				target.deleteById(ids);
//			}
//
//			@Override
//			public void deleteAll()
//			{
//				target.deleteAll();
//			}
//
//			@Override
//			public void add(Entity entity)
//			{
//				add(asList(entity).stream());
//			}
//
//			@Override
//			public Integer add(Stream<? extends Entity> records)
//			{
//				Iterable<Entity> entities = records.collect(toList());
//				for (Entity entity : entities)
//				{
//					if (promiseMapperFactory.getMapper(entity.getString(MAPPER)) == null)
//					{
//						throw new MolgenisDataException("Unknown mapper + " + entity.getString(MAPPER));
//					}
//				}
//
//				return target.add(records);
//			}
//
//			@Override
//			public void flush()
//			{
//				target.flush();
//			}
//
//			@Override
//			public void clearCache()
//			{
//				target.clearCache();
//			}
//
//		});
	}
}
