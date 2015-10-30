package org.molgenis.data.jpa;

import static org.molgenis.data.RepositoryCapability.UPDATEABLE;
import static org.molgenis.data.RepositoryCapability.WRITABLE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.Sort;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.support.QueryResolver;
import org.molgenis.generators.GeneratorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Repository implementation for (generated) jpa entities
 */
public class JpaRepository extends AbstractRepository
{
	private static final Logger LOG = LoggerFactory.getLogger(JpaRepository.class);

	public static final String BASE_URL = "jpa://";
	private final EntityMetaData entityMetaData;
	private final QueryResolver queryResolver;
	@PersistenceContext
	private EntityManager entityManager;

	public JpaRepository(EntityMetaData entityMetaData, QueryResolver queryResolver)
	{
		this.entityMetaData = entityMetaData;
		this.queryResolver = queryResolver;
	}

	public JpaRepository(EntityManager entityManager, EntityMetaData entityMetaData, QueryResolver queryResolver)
	{
		this(entityMetaData, queryResolver);
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
	@Transactional
	public void add(Entity entity)
	{
		Entity jpaEntity = getTypedEntity(entity);

		if (LOG.isDebugEnabled()) LOG.debug("persisting " + entity.getClass().getSimpleName() + " " + entity);
		getEntityManager().persist(jpaEntity);
		if (LOG.isDebugEnabled())
			LOG.debug("persisted " + entity.getClass().getSimpleName() + " [" + jpaEntity.getIdValue() + "]");

		entity.set(getEntityMetaData().getIdAttribute().getName(), jpaEntity.getIdValue());
	}

	@Override
	@Transactional
	public Integer add(Iterable<? extends Entity> entities)
	{
		Integer count = 0;
		for (Entity e : entities)
		{
			add(e);
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
		if (LOG.isDebugEnabled()) LOG.debug("execute count query " + q);
		return tq.getSingleResult();
	}

	@Override
	@Transactional(readOnly = true)
	public Entity findOne(Object id)
	{
		if (LOG.isDebugEnabled()) LOG.debug("finding by key" + getEntityClass().getSimpleName() + " [" + id + "]");

		return getEntityManager().find(getEntityClass(),
				getEntityMetaData().getIdAttribute().getDataType().convert(id));
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
		if (LOG.isDebugEnabled())
		{
			LOG.debug("finding " + getEntityClass().getSimpleName() + " " + q);
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
	@Transactional
	public void update(Entity entity)
	{
		EntityManager em = getEntityManager();

		if (LOG.isDebugEnabled())
			LOG.debug("merging" + getEntityClass().getSimpleName() + " [" + entity.getIdValue() + "]");
		em.merge(getTypedEntity(entity));

		if (LOG.isDebugEnabled()) LOG.debug("flushing entity manager");
		em.flush();
	}

	@Override
	@Transactional
	public void update(Iterable<? extends Entity> entities)
	{
		EntityManager em = getEntityManager();
		int batchSize = 500;
		int batchCount = 0;
		for (Entity r : entities)
		{
			Entity entity = getTypedEntity(r);

			if (LOG.isDebugEnabled())
				LOG.debug("merging" + getEntityClass().getSimpleName() + " [" + r.getIdValue() + "]");
			em.merge(entity);

			batchCount++;
			if (batchCount == batchSize)
			{
				if (LOG.isDebugEnabled()) LOG.debug("flushing entity manager");
				em.flush();

				if (LOG.isDebugEnabled()) LOG.debug("clearing entity manager");
				em.clear();
				batchCount = 0;
			}
		}
		if (LOG.isDebugEnabled()) LOG.debug("flushing entity manager");
		em.flush();
	}

	@Override
	@Transactional
	public void deleteById(Object id)
	{
		if (LOG.isDebugEnabled()) LOG.debug("removing " + getEntityClass().getSimpleName() + " [" + id + "]");

		Entity entity = findOne(getEntityMetaData().getIdAttribute().getDataType().convert(id));
		if (entity == null)
		{
			throw new UnknownEntityException(
					"Unknown entity [" + getEntityMetaData().getName() + "] with id [" + id + "]");
		}

		delete(entity);
	}

	@Override
	@Transactional
	public void delete(Entity entity)
	{
		EntityManager em = getEntityManager();
		if (LOG.isDebugEnabled())
		{
			LOG.debug("removing " + getEntityClass().getSimpleName() + " [" + entity.getIdValue() + "]");
		}

		em.remove(findOne(entity.getIdValue()));
		if (LOG.isDebugEnabled()) LOG.debug("flushing entity manager");
		em.flush();
	}

	@Override
	@Transactional
	public void delete(Iterable<? extends Entity> entities)
	{
		EntityManager em = getEntityManager();

		for (Entity r : entities)
		{
			em.remove(findOne(r.getIdValue()));
			if (LOG.isDebugEnabled())
			{
				LOG.debug("removing " + getEntityClass().getSimpleName() + " [" + r.getIdValue() + "]");
			}
		}

		if (LOG.isDebugEnabled()) LOG.debug("flushing entity manager");
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
					FieldTypeEnum enumType = meta.getDataType().getEnumType();
					if (enumType == FieldTypeEnum.MREF || enumType == FieldTypeEnum.CATEGORICAL_MREF
							|| enumType == FieldTypeEnum.CATEGORICAL)
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
				if (sortOrder.getDirection() == Sort.Direction.ASC)
				{
					orders.add(cb.asc(from.get(GeneratorHelper.firstToLower(sortOrder.getAttr()))));
				}
				else
				{
					orders.add(cb.desc(from.get(GeneratorHelper.firstToLower(sortOrder.getAttr()))));
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
		LOG.debug("flushing entity manager");
		getEntityManager().flush();
	}

	@Override
	public void clearCache()
	{
		LOG.debug("clearing entity manager");
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

		for (AttributeMetaData attr : getEntityMetaData().getAtomicAttributes())
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
				case CATEGORICAL_MREF:
				case MREF:
				case XREF:
					// Find the ref entities and create an 'in' queryrule
					// TODO other datatypes

					List<QueryRule> nested = Lists.newArrayList();
					for (AttributeMetaData refAttr : attr.getRefEntity().getAtomicAttributes())
					{
						if (refAttr.isLabelAttribute() || refAttr.isLookupAttribute())
						{
							FieldTypeEnum fieldType = refAttr.getDataType().getEnumType();

							if (fieldType == FieldTypeEnum.STRING || fieldType == FieldTypeEnum.ENUM
									|| fieldType == FieldTypeEnum.TEXT || fieldType == FieldTypeEnum.HTML
									|| fieldType == FieldTypeEnum.HYPERLINK || fieldType == FieldTypeEnum.EMAIL)
							{
								Query q = new QueryImpl().like(refAttr.getName(), searchValue.toString());
								EntityManager em = getEntityManager();
								CriteriaBuilder cb = em.getCriteriaBuilder();

								@SuppressWarnings("unchecked")
								CriteriaQuery<Entity> cq = (CriteriaQuery<Entity>) cb
										.createQuery(attr.getRefEntity().getEntityClass());

								@SuppressWarnings("unchecked")
								Root<Entity> from = (Root<Entity>) cq.from(attr.getRefEntity().getEntityClass());
								cq.select(from);

								// add filters
								createWhere(q, from, cq, cb);

								TypedQuery<Entity> tq = em.createQuery(cq);
								List<Entity> refEntities = tq.getResultList();
								if (!refEntities.isEmpty())
								{
									if (!nested.isEmpty())
									{
										nested.add(QueryRule.OR);
									}
									nested.add(new QueryRule(attr.getName(), Operator.IN, refEntities));
								}
							}
						}
					}

					if (!nested.isEmpty())
					{
						rule = new QueryRule(Operator.NESTED, nested);
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

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return Sets.newHashSet(UPDATEABLE, WRITABLE);
	}
}
