package org.molgenis.data.mongodb;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.AbstractRepository;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

@Component
public class MongoRepositoryImpl extends AbstractRepository implements MongoRepository
{
	private final EntityMetaData metaData;
	private final DBCollection dbCollection;
	private final MongoRepositoryCollection mongoRepositoryCollection;

	public MongoRepositoryImpl(EntityMetaData metaData, MongoRepositoryCollection mongoRepositoryCollection)
	{
		super("mongo:/" + mongoRepositoryCollection.getMongoDB().getMongo().getConnectPoint());
		this.metaData = metaData;
		this.dbCollection = mongoRepositoryCollection.getMongoDB().getCollection(metaData.getName());// Collection will
																										// be created if
																										// it does not
																										// exist
		this.mongoRepositoryCollection = mongoRepositoryCollection;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return metaData;
	}

	@Override
	public Iterator<Entity> iterator()
	{

		return collect(dbCollection.find()).iterator();
	}

	@Override
	public void add(Entity entity)
	{
		add(Collections.singletonList(entity));
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		List<DBObject> docs = Lists.newArrayList();
		for (Entity entity : entities)
		{
			docs.add(toDbObject(entity));
		}

		dbCollection.insert(docs);

		return docs.size();
	}

	@Override
	public long count()
	{
		return dbCollection.count();
	}

	@Override
	public void update(Entity entity)
	{
		dbCollection.save(toDbObject(entity));
	}

	@Override
	public Entity findOne(Object id)
	{
		DBObject dbObject = dbCollection.findOne(id);
		return toEntity(dbObject);
	}

	@Override
	public Iterable<Entity> findAll(Iterable<?> ids)
	{
		DBObject query = QueryBuilder.start().put("_id").in(ids).get();
		return collect(dbCollection.find(query));
	}

	@Override
	public void update(Iterable<? extends Entity> entities)
	{
		for (Entity entity : entities)
		{
			update(entity);
		}
	}

	@Override
	public void delete(Entity entity)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteById(Object id)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteById(Iterable<Object> ids)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteAll()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void update(List<? extends Entity> entities, DatabaseAction dbAction, String... keyName)
	{
	}

	@Override
	public void flush()
	{
		//

	}

	@Override
	public void clearCache()
	{
		//
	}

	@Override
	public void close() throws IOException
	{
		//
	}

	private List<Entity> collect(DBCursor cursor)
	{
		try
		{
			List<Entity> entities = Lists.newArrayList();

			while (cursor.hasNext())
			{
				DBObject dbObject = cursor.next();
				entities.add(toEntity(dbObject));
			}

			return entities;
		}
		finally
		{
			cursor.close();
		}
	}

	@SuppressWarnings("unchecked")
	private DBObject toDbObject(Entity entity)
	{
		BasicDBObject doc = new BasicDBObject();

		for (AttributeMetaData attr : getEntityMetaData().getAtomicAttributes())
		{
			Object value = entity.get(attr.getName());
			if (value != null)
			{
				if (value instanceof Entity)
				{
					value = ((Entity) value).get(attr.getRefEntity().getIdAttribute().getName());
				}
				else if (value instanceof Iterable)
				{
					// Peek
					Iterator<?> it = ((Iterable<?>) value).iterator();
					if (it.hasNext())
					{
						Object obj = it.next();
						if (obj instanceof Entity)
						{
							List<Object> ids = Lists.newArrayList();
							for (Entity refEntity : (Iterable<Entity>) value)
							{
								ids.add(refEntity.get(attr.getRefEntity().getIdAttribute().getName()));
							}
							value = ids;
						}
					}
				}

				doc.append(attr.getName(), value);

				if (attr.isIdAtrribute())
				{
					doc.append("_id", value);
				}

			}
		}

		System.out.println(doc);

		return doc;
	}

	private Entity toEntity(DBObject dbObject)
	{
		Entity entity = new MongoEntity(metaData, mongoRepositoryCollection);

		for (AttributeMetaData attr : getEntityMetaData().getAtomicAttributes())
		{
			Object value = dbObject.get(attr.getName());
			if (value != null)
			{
				entity.set(attr.getName(), value);
			}
		}

		return entity;
	}

}
