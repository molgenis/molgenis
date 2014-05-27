package org.molgenis.data.jpa;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.BOOL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.support.AbstractCrudRepository;
import org.molgenis.data.support.ConvertingIterable;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.support.QueryResolver;
import org.molgenis.data.validation.EntityValidator;
import org.molgenis.generators.GeneratorHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Repository implementation for (generated) jpa entities
 */
public class JpaRepository extends AbstractCrudRepository
{
	public static final String BASE_URL = "jpa://";
	private final EntityMetaData entityMetaData;
	private final QueryResolver queryResolver;
	private final Logger logger = Logger.getLogger(getClass());
	@PersistenceContext
	private EntityManager entityManager;

	public JpaRepository(EntityMetaData entityMetaData, EntityValidator entityValidator, QueryResolver queryResolver)
	{
		super(BASE_URL + entityMetaData.getEntityClass().getName(), entityValidator);
		this.entityMetaData = entityMetaData;
		this.queryResolver = queryResolver;
	}

	public JpaRepository(EntityManager entityManager, EntityMetaData entityMetaData, EntityValidator entityValidator,
			QueryResolver queryResolver)
	{
		this(entityMetaData, entityValidator, queryResolver);
		this.entityManager = entityManager;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

	protected Class<? extends Entity> getEntityClass()
	{
		return entityMetaData.getEntityClass();
	}

	protected EntityManager getEntityManager()
	{
		return entityManager;
	}

	@Override
	protected void addInternal(Entity entity)
	{
		Entity jpaEntity = getTypedEntity(entity);

		if (logger.isDebugEnabled()) logger.debug("persisting " + entity.getClass().getSimpleName() + " " + entity);
		getEntityManager().persist(jpaEntity);
		if (logger.isDebugEnabled()) logger.debug("persisted " + entity.getClass().getSimpleName() + " ["
				+ jpaEntity.getIdValue() + "]");

		entity.set(getEntityMetaData().getIdAttribute().getName(), jpaEntity.getIdValue());
	}

	@Override
	protected Integer addInternal(Iterable<? extends Entity> entities)
	{
		Integer count = 0;
		for (Entity e : entities)
		{
			addInternal(e);
			count++;
		}
		return count;
	}

	@Override
	@Transactional(readOnly = true)
	public Iterator<Entity> iterator()
	{
		return findAll(new QueryImpl()).iterator();
	}

	@Override
	@Transactional(readOnly = true)
	public long count()
	{
		return count(new QueryImpl());
	}

	@Override
	@Transactional(readOnly = true)
	public long count(Query q)
	{
		queryResolver.resolveRefIdentifiers(q.getRules(), getEntityMetaData());

		EntityManager em = getEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();

		// gonna produce a number
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<? extends Entity> from = cq.from(getEntityClass());
		cq.select(cb.countDistinct(from));// We need distinct, sometimes double rows are returned by EL when doing a
											// mref search

		// add filters
		createWhere(q, from, cq, cb);

		// execute the query
		TypedQuery<Long> tq = em.createQuery(cq);
		if (logger.isDebugEnabled()) logger.debug("execute count query " + q);
		return tq.getSingleResult();
	}

	@Override
	@Transactional(readOnly = true)
	public Entity findOne(Object id)
	{
		if (logger.isDebugEnabled()) logger
				.debug("finding by key" + getEntityClass().getSimpleName() + " [" + id + "]");

		return getEntityManager()
				.find(getEntityClass(), getEntityMetaData().getIdAttribute().getDataType().convert(id));
	}

	@Override
	@Transactional(readOnly = true)
	public Iterable<Entity> findAll(Iterable<Object> ids)
	{
		String idAttrName = getEntityMetaData().getIdAttribute().getName();

		// TODO why doesn't this work? Should work now (test it)
		// Query q = new QueryImpl().in(idAttrName, ids);
		// return findAll(q);

		EntityManager em = getEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();

		@SuppressWarnings("unchecked")
		CriteriaQuery<Entity> cq = (CriteriaQuery<Entity>) cb.createQuery(getEntityClass());

		@SuppressWarnings("unchecked")
		Root<Entity> from = (Root<Entity>) cq.from(getEntityClass());
		cq.select(from).where(from.get(idAttrName).in(Lists.newArrayList(ids)));

		TypedQuery<Entity> tq = em.createQuery(cq);
		if (logger.isDebugEnabled())
		{
			logger.debug("finding by key " + getEntityClass().getSimpleName() + " [" + StringUtils.join(ids, ',') + "]");
		}
		return tq.getResultList();
	}

	@Override
	@Transactional(readOnly = true)
	public Iterable<Entity> findAll(Query q)
	{
		queryResolver.resolveRefIdentifiers(q.getRules(), getEntityMetaData());

		EntityManager em = getEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();

		@SuppressWarnings("unchecked")
		CriteriaQuery<Entity> cq = (CriteriaQuery<Entity>) cb.createQuery(getEntityClass());

		@SuppressWarnings("unchecked")
		Root<Entity> from = (Root<Entity>) cq.from(getEntityClass());
		cq.select(from).distinct(true);// We need distinct, sometimes double rows are returned by EL when doing a mref
										// search

		// add filters
		createWhere(q, from, cq, cb);

		TypedQuery<Entity> tq = em.createQuery(cq);

		if (q.getPageSize() > 0) tq.setMaxResults(q.getPageSize());
		if (q.getOffset() > 0) tq.setFirstResult(q.getOffset());
		if (logger.isDebugEnabled())
		{
			logger.debug("finding " + getEntityClass().getSimpleName() + " " + q);
		}
		return tq.getResultList();
	}

	@Override
	@Transactional(readOnly = true)
	public Entity findOne(Query q)
	{
		Iterable<Entity> result = findAll(q);
		Iterator<Entity> it = result.iterator();
		if (it.hasNext())
		{
			return it.next();
		}

		return null;
	}

	@Override
	protected void updateInternal(Entity entity)
	{
		EntityManager em = getEntityManager();

		if (logger.isDebugEnabled()) logger.debug("merging" + getEntityClass().getSimpleName() + " ["
				+ entity.getIdValue() + "]");
		em.merge(getTypedEntity(entity));

		if (logger.isDebugEnabled()) logger.debug("flushing entity manager");
		em.flush();
	}

	@Override
	protected void updateInternal(Iterable<? extends Entity> entities)
	{
		EntityManager em = getEntityManager();
		int batchSize = 500;
		int batchCount = 0;
		for (Entity r : entities)
		{
			Entity entity = getTypedEntity(r);

			if (logger.isDebugEnabled()) logger.debug("merging" + getEntityClass().getSimpleName() + " ["
					+ r.getIdValue() + "]");
			em.merge(entity);

			batchCount++;
			if (batchCount == batchSize)
			{
				if (logger.isDebugEnabled()) logger.debug("flushing entity manager");
				em.flush();

				if (logger.isDebugEnabled()) logger.debug("clearing entity manager");
				em.clear();
				batchCount = 0;
			}
		}
		if (logger.isDebugEnabled()) logger.debug("flushing entity manager");
		em.flush();
	}

	@Override
	protected void updateInternal(List<? extends Entity> entities, DatabaseAction dbAction, String... keyNames)
	{
		if (keyNames.length == 0) throw new MolgenisDataException("At least one key must be provided, e.g. 'name'");

		// nothing todo?
		if (entities.size() == 0) return;

		// retrieve entity class and name
		String entityName = getEntityClass().getSimpleName();

		// create maps to store key values and entities
		// key is a concat of all key values for an entity
		Map<String, Entity> entityIndex = new LinkedHashMap<String, Entity>();
		// list of all keys, each list item a map of a (composite) key for one
		// entity e.g. investigation_name + name
		List<Map<String, Object>> keyIndex = new ArrayList<Map<String, Object>>();

		// select existing for update, only works if one (composit key allows
		// for nulls) the key values are set
		// otherwise skipped
		boolean keysMissing = false;
		for (Entity entity : entities)
		{
			// get all the value of all keys (composite key)
			// use an index to hash the entities
			StringBuilder combinedKeyBuilder = new StringBuilder();

			// extract its key values and put in map
			Map<String, Object> keyValues = new LinkedHashMap<String, Object>();
			boolean incompleteKey = true;

			// note: we can expect null values in composite keys but need at
			// least one key value.
			for (String key : keyNames)
			{
				// create a hash that concats all key values into one string
				combinedKeyBuilder.append(';');

				if (entity.get(key) != null)
				{
					combinedKeyBuilder.append(entity.get(key));
					incompleteKey = false;
					keyValues.put(key, entity.get(key));
				}
			}
			// check if we have missing key
			if (incompleteKey) keysMissing = true;

			// add the keys to the index, if exists
			if (!keysMissing)
			{
				keyIndex.add(keyValues);
				// create the entity index using the hash
				entityIndex.put(combinedKeyBuilder.toString(), entity);
			}
			else
			{
				if ((dbAction.equals(DatabaseAction.ADD) || dbAction.equals(DatabaseAction.ADD_IGNORE_EXISTING) || dbAction
						.equals(DatabaseAction.ADD_UPDATE_EXISTING))
						&& keyNames.length == 1
						&& keyNames[0].equals(getEntityMetaData().getIdAttribute().getName()))
				{
					// don't complain is 'id' field is emptyr
				}
				else
				{
					throw new MolgenisDataException("keys are missing: " + getEntityClass().getSimpleName() + "."
							+ Arrays.asList(keyNames));
				}
			}
		}

		// split lists in new and existing entities, but only if keys are set
		List<? extends Entity> newEntities = entities;
		List<Entity> existingEntities = new ArrayList<Entity>();
		if (!keysMissing && keyIndex.size() > 0)
		{
			newEntities = new ArrayList<Entity>();
			QueryImpl q = new QueryImpl();

			// in case of one field key, simply query
			if (keyNames.length == 1)
			{
				List<Object> values = new ArrayList<Object>();
				for (Map<String, Object> keyValues : keyIndex)
				{
					values.add(keyValues.get(keyNames[0]));
				}
				q.in(keyNames[0], values);
			}
			// in case of composite key make massive 'OR' query
			// form (key1 = x AND key2 = X) OR (key1=y AND key2=y)
			else
			{
				// very expensive!
				for (Map<String, Object> keyValues : keyIndex)
				{
					for (int i = 0; i < keyNames.length; i++)
					{
						if (i > 0) q.or();
						q.eq(keyNames[i], keyValues.get(keyNames[i]));
					}
				}
			}
			Iterable<Entity> selectForUpdate = findAll(q);

			// separate existing from new entities
			for (Entity p : selectForUpdate)
			{
				// reconstruct composite key so we can use the entityIndex
				StringBuilder combinedKeyBuilder = new StringBuilder();
				for (String key : keyNames)
				{
					combinedKeyBuilder.append(';').append(p.get(key));
				}
				// copy existing from entityIndex to existingEntities
				entityIndex.remove(combinedKeyBuilder.toString());
				existingEntities.add(p);
			}
			// copy remaining to newEntities
			newEntities = new ArrayList<Entity>(entityIndex.values());
		}

		// if existingEntities are going to be updated, they will need to
		// receive new values from 'entities' in addition to be mapped to the
		// database as is the case at this point
		if (existingEntities.size() > 0
				&& (dbAction == DatabaseAction.ADD_UPDATE_EXISTING || dbAction == DatabaseAction.UPDATE || dbAction == DatabaseAction.UPDATE_IGNORE_MISSING))
		{
			if (logger.isDebugEnabled()) logger.debug("existingEntities[0] before: "
					+ existingEntities.get(0).toString());

			matchByNameAndUpdateFields(existingEntities, entities);

			if (logger.isDebugEnabled()) logger.debug("existingEntities[0] after: "
					+ existingEntities.get(0).toString());
		}

		switch (dbAction)
		{

		// will test for existing entities before add
		// (so only add if existingEntities.size == 0).
			case ADD:
				if (existingEntities.size() == 0)
				{
					addInternal(newEntities);
				}
				else
				{
					throw new MolgenisDataException("Tried to add existing "
							+ entityName
							+ " elements as new insert: "
							+ Arrays.asList(keyNames)
							+ "="
							+ existingEntities.subList(0, Math.min(5, existingEntities.size()))
							+ (existingEntities.size() > 5 ? " and " + (existingEntities.size() - 5) + "more" : ""
									+ existingEntities));
				}
				break;

			// will not test for existing entities before add
			// (so will ignore existingEntities)
			case ADD_IGNORE_EXISTING:
				if (logger.isDebugEnabled()) logger.debug("updateByName(List<" + entityName + "," + dbAction
						+ ">) will skip " + existingEntities.size() + " existing entities");
				addInternal(newEntities);
				break;

			// will try to update(existingEntities) entities and
			// add(missingEntities)
			// so allows user to be sloppy in adding/updating
			case ADD_UPDATE_EXISTING:
				if (logger.isDebugEnabled()) logger.debug("updateByName(List<" + entityName + "," + dbAction
						+ ">)  will try to update " + existingEntities.size() + " existing entities and add "
						+ newEntities.size() + " new entities");
				addInternal(newEntities);
				update(existingEntities);
				break;

			// update while testing for newEntities.size == 0
			case UPDATE:
				if (newEntities.size() == 0)
				{
					updateInternal(existingEntities);
				}
				else
				{
					throw new MolgenisDataException("Tried to update non-existing " + entityName + "elements "
							+ Arrays.asList(keyNames) + "=" + entityIndex.values());
				}
				break;

			// update that doesn't test for newEntities but just ignores
			// those
			// (so only updates exsiting)
			case UPDATE_IGNORE_MISSING:
				if (logger.isDebugEnabled()) logger.debug("updateByName(List<" + entityName + "," + dbAction
						+ ">) will try to update " + existingEntities.size() + " existing entities and skip "
						+ newEntities.size() + " new entities");
				updateInternal(existingEntities);
				break;

			// remove all elements in list, test if no elements are missing
			// (so test for newEntities == 0)
			case REMOVE:
				if (newEntities.size() == 0)
				{
					if (logger.isDebugEnabled()) logger.debug("updateByName(List<" + entityName + "," + dbAction
							+ ">) will try to remove " + existingEntities.size() + " existing entities");
					delete(existingEntities);
				}
				else
				{
					throw new MolgenisDataException("Tried to remove non-existing " + entityName + " elements "
							+ Arrays.asList(keyNames) + "=" + entityIndex.values());

				}
				break;

			// remove entities that are in the list, ignore if they don't
			// exist in database
			// (so don't check the newEntities.size == 0)
			case REMOVE_IGNORE_MISSING:
				if (logger.isDebugEnabled()) logger.debug("updateByName(List<" + entityName + "," + dbAction
						+ ">) will try to remove " + existingEntities.size() + " existing entities and skip "
						+ newEntities.size() + " new entities");
				delete(existingEntities);
				break;

			// unexpected error
			default:
				throw new MolgenisDataException("updateByName failed because of unknown dbAction " + dbAction);
		}
	}

	private void matchByNameAndUpdateFields(List<? extends Entity> existingEntities, List<? extends Entity> entities)
	{
		for (Entity entityInDb : existingEntities)
		{
			for (Entity newEntity : entities)
			{
				boolean match = false;
				// check if there are any label fields otherwise check impossible
				if (entityInDb.getLabelAttributeNames().size() > 0)
				{
					match = true;
				}
				for (String labelField : entityInDb.getLabelAttributeNames())
				{
					Object x1 = entityInDb.get(labelField);
					Object x2 = newEntity.get(labelField);

					if (!x1.equals(x2))
					{
						match = false;
						break;
					}
				}

				if (match)
				{
					try
					{
						MapEntity mapEntity = new MapEntity();
						for (String field : entityInDb.getAttributeNames())
						{
							mapEntity.set(field, newEntity.get(field));
						}
						entityInDb.set(mapEntity, false);
					}
					catch (Exception ex)
					{
						throw new MolgenisDataException(ex);
					}
				}

			}
		}
	}

	@Override
	@Transactional
	public void deleteById(Object id)
	{
		if (logger.isDebugEnabled()) logger.debug("removing " + getEntityClass().getSimpleName() + " [" + id + "]");

		Entity entity = findOne(getEntityMetaData().getIdAttribute().getDataType().convert(id));
		if (entity == null)
		{
			throw new UnknownEntityException("Unknown entity [" + getEntityMetaData().getName() + "] with id [" + id
					+ "]");
		}

		delete(entity);
	}

	@Override
	@Transactional
	public void delete(Entity entity)
	{
		EntityManager em = getEntityManager();
		if (logger.isDebugEnabled())
		{
			logger.debug("removing " + getEntityClass().getSimpleName() + " [" + entity.getIdValue() + "]");
		}

		em.remove(getTypedEntity(entity));
		if (logger.isDebugEnabled()) logger.debug("flushing entity manager");
		em.flush();
	}

	@Override
	@Transactional
	public void delete(Iterable<? extends Entity> entities)
	{
		EntityManager em = getEntityManager();

		for (Entity r : entities)
		{
			em.remove(getTypedEntity(r));
			if (logger.isDebugEnabled())
			{
				logger.debug("removing " + getEntityClass().getSimpleName() + " [" + r.getIdValue() + "]");
			}
		}

		if (logger.isDebugEnabled()) logger.debug("flushing entity manager");
		em.flush();
	}

	@Override
	@Transactional
	public void deleteAll()
	{
		delete(this);
	}

	private void createWhere(Query q, Root<?> from, CriteriaQuery<?> cq, CriteriaBuilder cb)
	{
		List<Predicate> where = createPredicates(from, cb, q.getRules());
		if (!where.isEmpty()) cq.where(cb.and(where.toArray(new Predicate[where.size()])));
		List<Order> orders = createOrder(from, cb, q.getSort());
		if (!orders.isEmpty()) cq.orderBy(orders);
	}

	/** Converts MOLGENIS query rules into JPA predicates */
	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	private List<Predicate> createPredicates(Root<?> from, CriteriaBuilder cb, List<QueryRule> originalRules)
	{
		List<QueryRule> rules = Lists.newArrayList(originalRules);

		// default Query links criteria based on 'and'
		List<Predicate> andPredicates = new ArrayList<Predicate>();

		// optionally, subqueries can be formulated seperated by 'or'
		List<Predicate> orPredicates = new ArrayList<Predicate>();

		ListIterator<QueryRule> it = rules.listIterator();
		while (it.hasNext())
		{
			QueryRule r = it.next();

			switch (r.getOperator())
			{
				case AND:
					break;
				case NESTED:
					List<QueryRule> nestedRules = r.getNestedRules();
					if (nestedRules != null && !nestedRules.isEmpty())
					{
						List<Predicate> subPredicates = createPredicates(from, cb, nestedRules);
						Predicate predicate;
						if (subPredicates.size() == 1)
						{
							predicate = subPredicates.get(0);
						}
						else
						{
							Predicate[] subPredicatesArr = subPredicates.toArray(new Predicate[0]);
							Operator andOrOperator = nestedRules.get(1).getOperator();
							switch (andOrOperator)
							{
								case AND:
									predicate = cb.and(subPredicatesArr);
									break;
								case OR:
									predicate = cb.or(subPredicatesArr);
									break;
								default:
									throw new MolgenisDataException("Expected AND or OR operator in query rule [" + r
											+ "] instead of " + andOrOperator);
							}
						}
						andPredicates.add(predicate); // added to orPredicates list near end of method if required
					}
					break;
				case OR:
					orPredicates.add(cb.and(andPredicates.toArray(new Predicate[andPredicates.size()])));
					andPredicates.clear();
					break;
				case EQUALS:
					andPredicates.add(cb.equal(from.get(r.getJpaAttribute()), r.getValue()));
					break;
				case IN:
					AttributeMetaData meta = getEntityMetaData().getAttribute(r.getField());

					In<Object> in;
					if (meta.getDataType().getEnumType() == FieldTypeEnum.MREF
							|| meta.getDataType().getEnumType() == FieldTypeEnum.CATEGORICAL)
					{
						in = cb.in(from.join(r.getJpaAttribute(), JoinType.LEFT));
					}
					else
					{
						in = cb.in(from.get(r.getJpaAttribute()));
					}

					for (Object o : (Iterable) r.getValue())
					{
						in.value(o);
					}
					andPredicates.add(in);

					break;
				case LIKE:
					String like = "%" + r.getValue() + "%";
					String f = r.getJpaAttribute();
					andPredicates.add(cb.like(from.<String> get(f), like));
					break;
				case SEARCH:
					// Create like predicated for all attributes and remove original 'search' QueryRule
					andPredicates.addAll(createPredicates(from, cb, createSearchQueryRules(r.getValue())));
					it.remove();
					break;
				default:
					// go into comparator based criteria, that need
					// conversion...

					Path<Comparable> field = from.get(r.getJpaAttribute());
					Object value = r.getValue();
					Comparable cValue = null;

					// convert to type
					if (field.getJavaType() == Integer.class)
					{
						cValue = DataConverter.toInt(value);
					}
					else if (field.getJavaType() == Long.class)
					{
						cValue = DataConverter.toLong(value);
					}
					else if (field.getJavaType() == Date.class)
					{
						cValue = DataConverter.toDate(value);
					}
					else throw new MolgenisDataException("cannot solve query rule:  " + r);

					// comparable values...
					switch (r.getOperator())
					{
						case GREATER:
							andPredicates.add(cb.greaterThan(field, cValue));
							break;
						case LESS:
							andPredicates.add(cb.lessThan(field, cValue));
							break;
						case GREATER_EQUAL:
							andPredicates.add(cb.greaterThanOrEqualTo(field, cValue));
							break;
						case LESS_EQUAL:
							andPredicates.add(cb.lessThanOrEqualTo(field, cValue));
							break;
						default:
							throw new RuntimeException("canno solve query rule:  " + r);
					}
			}

		}

		if (orPredicates.size() > 0)
		{
			if (andPredicates.size() > 0)
			{
				orPredicates.add(cb.and(andPredicates.toArray(new Predicate[0])));
			}
			List<Predicate> result = new ArrayList<Predicate>();

			result.add(cb.or(orPredicates.toArray(new Predicate[0])));

			return result;
		}
		else
		{
			if (andPredicates.size() > 0)
			{

				return andPredicates;
			}
			return new ArrayList<Predicate>();
		}
	}

	private List<Order> createOrder(Root<?> from, CriteriaBuilder cb, Sort sort)
	{
		List<Order> orders = new ArrayList<Order>();

		if (sort != null)
		{
			for (Sort.Order sortOrder : sort)
			{
				if (sortOrder.isAscending())
				{
					orders.add(cb.asc(from.get(GeneratorHelper.firstToLower(sortOrder.getProperty()))));
				}
				else
				{
					orders.add(cb.desc(from.get(GeneratorHelper.firstToLower(sortOrder.getProperty()))));
				}
			}
		}

		return orders;
	}

	@Override
	public void close() throws IOException
	{
		// Nothing
	}

	@Override
	@Transactional
	public void deleteById(Iterable<Object> ids)
	{
		for (Object id : ids)
		{
			deleteById(id);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public void flush()
	{
		logger.debug("flushing entity manager");
		getEntityManager().flush();
	}

	@Override
	public void clearCache()
	{
		logger.debug("clearing entity manager");
		getEntityManager().clear();
	}

	// If the entity is of the correct type return it, else convert it to the correct type
	private Entity getTypedEntity(Entity entity)
	{
		if (getEntityClass().isAssignableFrom(entity.getClass()))
		{
			return entity;
		}

		Entity jpaEntity = BeanUtils.instantiateClass(getEntityClass());
		jpaEntity.set(entity);

		return jpaEntity;
	}

	@Override
	@Transactional(readOnly = true)
	public <E extends Entity> Iterable<E> findAll(Iterable<Object> ids, Class<E> clazz)
	{
		return new ConvertingIterable<E>(clazz, findAll(ids));
	}

	@Override
	@Transactional(readOnly = true)
	public <E extends Entity> Iterable<E> findAll(Query q, Class<E> clazz)
	{
		return new ConvertingIterable<E>(clazz, findAll(q));
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public <E extends Entity> E findOne(Object id, Class<E> clazz)
	{
		Entity entity = findOne(id);
		if (entity == null)
		{
			return null;
		}

		if (clazz.isAssignableFrom(entity.getClass()))
		{
			return (E) entity;
		}

		E e = BeanUtils.instantiate(clazz);
		e.set(entity);
		return e;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public <E extends Entity> E findOne(Query q, Class<E> clazz)
	{
		Entity entity = findOne(q);
		if (entity == null)
		{
			return null;
		}

		if (clazz.isAssignableFrom(entity.getClass()))
		{
			return (E) entity;
		}

		E e = BeanUtils.instantiate(clazz);
		e.set(entity);
		return e;
	}

	@Override
	public AggregateResult aggregate(AttributeMetaData xAttributeMeta, AttributeMetaData yAttributeMeta, Query query)
	{
		if ((xAttributeMeta == null) && (yAttributeMeta == null))
		{
			throw new MolgenisDataException("Missing aggregate attribute");
		}

		FieldTypeEnum xDataType = null;
		String xAttributeName = null;
		if (xAttributeMeta != null)
		{
			xAttributeName = xAttributeMeta.getName();

			if (!xAttributeMeta.isAggregateable())
			{
				throw new MolgenisDataException("Attribute '" + xAttributeName + "' is not aggregateable");
			}

			xDataType = xAttributeMeta.getDataType().getEnumType();
		}

		FieldTypeEnum yDataType = null;
		String yAttributeName = null;
		if (yAttributeMeta != null)
		{
			yAttributeName = yAttributeMeta.getName();
			if (!yAttributeMeta.isAggregateable())
			{
				throw new MolgenisDataException("Attribute '" + yAttributeName + "' is not aggregateable");
			}

			yDataType = yAttributeMeta.getDataType().getEnumType();
		}

		List<Object> xValues = Lists.newArrayList();
		List<Object> yValues = Lists.newArrayList();
		List<List<Long>> matrix = new ArrayList<List<Long>>();
		Set<String> xLabels = Sets.newLinkedHashSet();
		Set<String> yLabels = Sets.newLinkedHashSet();

		if (xDataType != null)
		{
			if (xDataType == BOOL)
			{
				xValues.add(Boolean.TRUE);
				xValues.add(Boolean.FALSE);
				xLabels.add(xAttributeName + ": true");
				xLabels.add(xAttributeName + ": false");
			}
			else if (xAttributeMeta.getRefEntity() != null)
			{
				EntityMetaData xRefEntityMeta = xAttributeMeta.getRefEntity();
				String xRefEntityLblAttr = xRefEntityMeta.getLabelAttribute().getName();

				for (Entity xRefEntity : findAll(xRefEntityMeta.getEntityClass()))
				{
					xLabels.add(xRefEntity.getString(xRefEntityLblAttr));
					xValues.add(xRefEntity.get(xRefEntityLblAttr));
				}
			}
			else
			{
				for (Object value : getDistinctValues(xAttributeMeta))
				{
					String valueStr = DataConverter.toString(value);
					xLabels.add(valueStr);
					xValues.add(valueStr);
				}
			}
		}

		if (yDataType != null)
		{
			if (yDataType == BOOL)
			{
				yValues.add(Boolean.TRUE);
				yValues.add(Boolean.FALSE);
				yLabels.add(yAttributeName + ": true");
				yLabels.add(yAttributeName + ": false");
			}
			else if (yAttributeMeta.getRefEntity() != null)
			{
				EntityMetaData yRefEntityMeta = yAttributeMeta.getRefEntity();
				String yRefEntityLblAttr = yRefEntityMeta.getLabelAttribute().getName();

				for (Entity yRefEntity : findAll(yRefEntityMeta.getEntityClass()))
				{
					yLabels.add(yRefEntity.getString(yRefEntityLblAttr));
					yValues.add(yRefEntity.get(yRefEntityLblAttr));
				}
			}
			else
			{
				for (Object value : getDistinctValues(yAttributeMeta))
				{
					String valueStr = DataConverter.toString(value);
					yLabels.add(valueStr);
					yValues.add(valueStr);
				}
			}
		}

		boolean hasXValues = !xValues.isEmpty();
		boolean hasYValues = !yValues.isEmpty();

		if (hasXValues)
		{
			List<Long> totals = Lists.newArrayList();

			for (Object xValue : xValues)
			{
				List<Long> row = Lists.newArrayList();

				if (hasYValues)
				{
					int i = 0;

					for (Object yValue : yValues)
					{

						// Both x and y choosen
						Query finalQ = query.getRules().isEmpty() ? new QueryImpl() : new QueryImpl(query).and();
						finalQ.eq(xAttributeName, xValue).and().eq(yAttributeName, yValue);
						long count = count(finalQ);
						row.add(count);
						if (totals.size() == i)
						{
							totals.add(count);
						}
						else
						{
							totals.set(i, totals.get(i) + count);
						}
						i++;
					}
				}
				else
				{
					// No y attribute chosen
					Query finalQ = query.getRules().isEmpty() ? new QueryImpl() : new QueryImpl(query).and();
					finalQ.eq(xAttributeName, xValue);
					long count = count(finalQ);
					row.add(count);
					if (totals.isEmpty())
					{
						totals.add(count);
					}
					else
					{
						totals.set(0, totals.get(0) + count);
					}

				}

				matrix.add(row);
			}

			yLabels.add(hasYValues ? "Total" : "Count");
			xLabels.add("Total");

			matrix.add(totals);
		}
		else
		{
			// No xattribute chosen
			List<Long> row = Lists.newArrayList();
			for (Object yValue : yValues)
			{
				Query finalQ = query.getRules().isEmpty() ? new QueryImpl() : new QueryImpl(query).and();
				finalQ.eq(yAttributeName, yValue);
				long count = count(finalQ);
				row.add(count);
			}
			matrix.add(row);

			xLabels.add("Count");
			yLabels.add("Total");
		}

		// Count row totals
		if (hasYValues)
		{
			for (List<Long> row : matrix)
			{
				long total = 0;
				for (Long count : row)
				{
					total += count;
				}
				row.add(total);
			}
		}

		return new AggregateResult(matrix, new ArrayList<String>(xLabels), new ArrayList<String>(yLabels));
	}

	// Get all distinct values of an attribute
	private List<?> getDistinctValues(AttributeMetaData attr)
	{
		EntityManager em = getEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = cb.createTupleQuery();

		String attrName = attr.getName().substring(0, 1).toLowerCase() + attr.getName().substring(1);
		Root<? extends Entity> root = cq.from(getEntityClass());
		cq.distinct(true).multiselect(root.get(attrName));

		TypedQuery<Tuple> tq = em.createQuery(cq);
		List<Tuple> tuples = tq.getResultList();

		List<Object> result = Lists.newArrayList();
		for (Tuple tuple : tuples)
		{
			result.add(tuple.get(0));
		}

		return result;
	}

	// Find all instances of an jpa entity
	private List<? extends Entity> findAll(Class<? extends Entity> entityClass)
	{
		EntityManager em = getEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();

		@SuppressWarnings("unchecked")
		CriteriaQuery<Entity> cq = (CriteriaQuery<Entity>) cb.createQuery(entityClass);
		TypedQuery<Entity> tq = em.createQuery(cq.select(cq.from(entityClass)));

		return tq.getResultList();
	}

	/*
	 * Convert a search query to a list of QueryRule, creates for every attribute a QueryRule and 'OR's them
	 * 
	 * No search on XREF/MREF possible at the moment
	 * 
	 * Replace this by ES indexing??
	 */
	private List<QueryRule> createSearchQueryRules(Object searchValue)
	{
		List<QueryRule> searchRules = Lists.newArrayList();

		for (AttributeMetaData attr : getEntityMetaData().getAttributes())
		{
			QueryRule rule = null;
			switch (attr.getDataType().getEnumType())
			{
				case ENUM:
				case STRING:
				case TEXT:
				case HTML:
				case HYPERLINK:
				case EMAIL:
					rule = new QueryRule(attr.getName(), Operator.LIKE, searchValue);
					break;
				case BOOL:
					if (DataConverter.canConvert(searchValue, Boolean.class))
					{
						rule = new QueryRule(attr.getName(), Operator.EQUALS, DataConverter.toBoolean(searchValue));
					}
					break;
				case DATE:
					if (DataConverter.canConvert(searchValue, java.sql.Date.class))
					{
						rule = new QueryRule(attr.getName(), Operator.EQUALS, DataConverter.toDate(searchValue));
					}
					break;
				case DATE_TIME:
					if (DataConverter.canConvert(searchValue, java.util.Date.class))
					{
						rule = new QueryRule(attr.getName(), Operator.EQUALS, DataConverter.toUtilDate(searchValue));
					}
					break;
				case DECIMAL:
					if (DataConverter.canConvert(searchValue, Double.class))
					{
						rule = new QueryRule(attr.getName(), Operator.EQUALS, DataConverter.toDouble(searchValue));
					}
					break;
				case INT:
					if (DataConverter.canConvert(searchValue, Integer.class))
					{
						rule = new QueryRule(attr.getName(), Operator.EQUALS, DataConverter.toInt(searchValue));
					}
					break;
				case LONG:
					if (DataConverter.canConvert(searchValue, Long.class))
					{
						rule = new QueryRule(attr.getName(), Operator.EQUALS, DataConverter.toLong(searchValue));
					}
					break;

				case CATEGORICAL:
				case MREF:
				case XREF:
					// Find the ref entities and create an 'in' queryrule
					// TODO other datatypes
					if (attr.getRefEntity().getLabelAttribute().getDataType().getEnumType() == FieldTypeEnum.STRING)
					{
						Query q = new QueryImpl().like(attr.getRefEntity().getLabelAttribute().getName(), searchValue);
						EntityManager em = getEntityManager();
						CriteriaBuilder cb = em.getCriteriaBuilder();

						@SuppressWarnings("unchecked")
						CriteriaQuery<Entity> cq = (CriteriaQuery<Entity>) cb.createQuery(attr.getRefEntity()
								.getEntityClass());

						@SuppressWarnings("unchecked")
						Root<Entity> from = (Root<Entity>) cq.from(attr.getRefEntity().getEntityClass());
						cq.select(from);

						// add filters
						createWhere(q, from, cq, cb);

						TypedQuery<Entity> tq = em.createQuery(cq);
						List<Entity> refEntities = tq.getResultList();
						if (!refEntities.isEmpty())
						{
							rule = new QueryRule(attr.getName(), Operator.IN, refEntities);
						}
					}

					break;
				default:
					break;

			}

			if (rule != null)
			{
				if (!searchRules.isEmpty())
				{
					searchRules.add(new QueryRule(Operator.OR));
				}

				searchRules.add(rule);
			}

		}

		return searchRules;
	}
}
