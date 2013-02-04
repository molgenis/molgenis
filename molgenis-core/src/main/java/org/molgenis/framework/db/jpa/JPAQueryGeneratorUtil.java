package org.molgenis.framework.db.jpa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.db.SubQueryRule;
import org.molgenis.util.AbstractEntity;
import org.molgenis.util.Entity;

/**
 * @author joris lops
 */
public class JPAQueryGeneratorUtil
{
	private static final Logger logger = Logger.getLogger(JPAQueryGeneratorUtil.class);

	public static <IN extends Entity> TypedQuery<IN> createQuery(Database db, Class<IN> inputClass, Mapper<IN> mapper,
			EntityManager em, QueryRule... rules) throws DatabaseException
	{
		return createQuery(db, inputClass, inputClass, mapper, em, rules);
	}

	@SuppressWarnings("unchecked")
	public static <IN extends Entity, OUT> TypedQuery<OUT> createQuery(Database db, Class<IN> inputClass,
			Class<OUT> outputClass, Mapper<IN> mapper, EntityManager em, QueryRule... rules) throws DatabaseException
	{
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<OUT> cq = cb.createQuery(outputClass);
		Root<IN> root = cq.from(inputClass);

		if (inputClass.getSimpleName().equals(outputClass.getSimpleName()))
		{
			cq.select((Selection<? extends OUT>) root);
		}
		else
		{
			cq.select((Selection<? extends OUT>) cb.count(root));
		}

		int[] limitOffset = new int[2];
		Arrays.fill(limitOffset, -1);
		Predicate wherePredicate = createWhere(db, mapper, em, root, cq, cb, limitOffset, rules);
		if (wherePredicate != null)
		{
			cq.where(wherePredicate);
		}
		TypedQuery<OUT> query = em.createQuery(cq);
		if (limitOffset[0] != -1)
		{
			query.setMaxResults(limitOffset[0]);
		}
		if (limitOffset[1] != -1)
		{
			query.setFirstResult(limitOffset[1]);
		}
		return query;
	}

	public static <E extends Entity> TypedQuery<Long> createCount(Database db, Class<E> entityClass,
			AbstractJpaMapper<E> abstractJpaMapper, EntityManager em, QueryRule... rules) throws DatabaseException
	{
		// remove any 'offset' from the query as this screws up count
		List<QueryRule> limitLess = new ArrayList<QueryRule>();
		for (QueryRule r : rules)
		{
			if (!Operator.OFFSET.equals(r.getOperator())) limitLess.add(r);
		}

		return createQuery(db, entityClass, Long.class, abstractJpaMapper, em,
				limitLess.toArray(new QueryRule[limitLess.size()]));
	}

	private static <IN extends Entity, OUT> Predicate createWhere(Database db, Mapper<IN> mapper, EntityManager em,
			Root<IN> root, CriteriaQuery<OUT> cq, CriteriaBuilder cb, int[] limitOffset, QueryRule... rul)
			throws DatabaseException
	{
		Map<String, Join<?, ?>> joinHash = new HashMap<String, Join<?, ?>>();
		return _createWhere(db, mapper, em, root, cq, cb, limitOffset, joinHash, rul);
	}

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	private static <IN extends Entity, OUT> Predicate _createWhere(Database db, Mapper<IN> mapper, EntityManager em,
			Root<IN> root, CriteriaQuery<OUT> cq, CriteriaBuilder cb, int[] limitOffset,
			Map<String, Join<?, ?>> joinHash, QueryRule... rul) throws DatabaseException
	{
		List<QueryRule> rules = Arrays.asList(rul);

		Predicate whereClause = null;
		List<Order> orders = new ArrayList<Order>();

		QueryRule prevRule = null;

		forLoop: for (int i = 0; i < rules.size(); ++i)
		{
			QueryRule rule = rules.get(i);
			if (mapper != null)
			{
				rule.setField(mapper.getTableFieldName(rule.getField()));

				Operator operator = rule.getOperator();
				if (operator == Operator.SORTASC || operator == Operator.SORTDESC)
				{
					rule.setField(mapper.getTableFieldName(rule.getValue().toString()));
				}

				String attributeName = rule.getJpaAttribute();

				Predicate predicate = null;

				Expression<?> expression = _addJoin(rule, root, joinHash);

				Expression<?> lhs = null;
				if (expression != null)
				{
					lhs = expression;
				}
				else if (attributeName != null)
				{
					lhs = root.get(attributeName);
				}
				Object rhs = rule.getValue();

				switch (operator)
				{
					case LAST:
						throw new UnsupportedOperationException("Not supported yet.");
					case SORTASC:
						orders.add(cb.asc(lhs));
						break;
					case SORTDESC:
						orders.add(cb.desc(lhs));
						break;
					case LIMIT:
						limitOffset[0] = (Integer) rule.getValue();
						break;
					case OFFSET:
						limitOffset[1] = (Integer) rule.getValue();
						break;
					default:
						switch (operator)
						{
							case EQUALS:
								if (rhs instanceof Entity)
								{
									try
									{
										predicate = cb.equal(root.get(attributeName), rule.getValue());
									}
									catch (Exception ex)
									{
										logger.error(ex);
									}
								}
								else
								{
									try
									{
										// it's a xref attribute which is joined
										// to root
										if (attributeName.contains(".")
												|| root.get(attributeName).getJavaType().getName()
														.equals("java.util.List")
												|| root.get(attributeName).getJavaType().newInstance() instanceof Entity)
										{
											predicate = cb.equal(lhs, rhs);
										}
										else
										{ // normal attribute
											predicate = cb.equal(lhs, rhs);
										}
									}
									catch (InstantiationException ex)
									{
										// this is a hack, newInstance can not
										// be called on inmutable object
										// like Integer
										predicate = cb.equal(lhs, rhs);
									}
									catch (IllegalAccessException ex)
									{
										logger.error(ex);
										throw new DatabaseException(ex);
									}
								}
								break;
							case NOT:
								predicate = cb.notEqual(lhs, rhs);
								break;
							case LIKE:
								if (lhs.getJavaType().getSimpleName().equals("String"))
								{
									predicate = cb.like(lhs.as(String.class), (String) rhs);
								}
								else
								{
									// TODO: What to do here?
								}
								break;
							case LESS:
								predicate = cb.lessThan((Expression) lhs, (Comparable<Object>) rhs);
								break;
							case GREATER:
								predicate = cb.greaterThan((Expression) lhs, (Comparable<Object>) rhs);
								break;
							case LESS_EQUAL:
								predicate = cb.lessThanOrEqualTo((Expression) lhs, (Comparable<Object>) rhs);
								break;
							case GREATER_EQUAL:
								predicate = cb.greaterThanOrEqualTo((Expression) lhs, (Comparable<Object>) rhs);
								break;
							case NESTED:
								QueryRule[] nestedrules = rule.getNestedRules();
								predicate = _createWhere(db, mapper, em, root, cq, cb, new int[2], joinHash,
										nestedrules);
								break;
							case SUBQUERY:
								SubQueryRule sqr = (SubQueryRule) rule;

								Subquery sq = cq.subquery(sqr.getSubQueryResultClass());
								Root<IN> sqFrom = sq.from(sqr.getSubQueryFromClass());

								Mapper<IN> sqMapper = db.getMapper(sqr.getSubQueryFromClass().getName());

								Predicate where = _createWhere(db, sqMapper, em, sqFrom, cq, cb, new int[2], joinHash,
										(QueryRule[]) sqr.getValue());
								sq.select(sqFrom.get(sqr.getSubQueryAttributeJpa())).where(where);

								// the operator of subquery should be handled in
								// the right way such that no code duplication
								// should occure
								// for the moment only in will work (more to
								// come)
								String fieldForSubQuery = sqr.getJpaAttribute();

								if (sqr.getSubQueryOperator().equals(Operator.IN))
								{
									predicate = cb.in(root.get(fieldForSubQuery)).value(sq);
								}
								else
								{
									throw new UnsupportedOperationException();
								}
								break;
							case IN: // not a query but a list for example
										// SELECT * FROM
								// x WHERE x.a1 IN (v1, v2, v3)
								Object[] values = new Object[0];
								if (rule.getValue() instanceof List)
								{
									values = ((List<?>) rule.getValue()).toArray();
								}
								else
								{
									values = (Object[]) rule.getValue();
								}
								Class<?> attrClass = null;
								if (attributeName.contains("."))
								{
									attrClass = lhs.getJavaType();
								}
								else
								{
									attrClass = root.get(attributeName).getJavaType();
								}
								// pseudo code: if(Attribute instanceof
								// AbstractEntity)
								if (AbstractEntity.class.isAssignableFrom(attrClass))
								{
									predicate = root.get(attributeName).in(values);
								}
								else
								{
									predicate = lhs.in(values);
								}

								break;
						}
						// make a where clause from the predicate
						if (whereClause != null)
						{
							if (predicate != null)
							{
								if (prevRule != null && prevRule.getOperator().equals(Operator.OR))
								{
									List<QueryRule> restOfQueryRules = rules.subList(i, rules.size());
									Predicate rightsPred = _createWhere(db, mapper, em, root, cq, cb, limitOffset,
											joinHash, restOfQueryRules.toArray(new QueryRule[1]));
									if (rightsPred != null)
									{
										whereClause = cb.or(whereClause, rightsPred);
									}
									break forLoop;
								}
								else
								{
									whereClause = cb.and(whereClause, predicate);
								}
							}
						}
						else
						{
							whereClause = predicate;
						}
						break;
				}
			}
			prevRule = rule;
		}
		if (orders.size() > 0)
		{
			cq.orderBy(orders);
		}
		// if (whereClause != null) {
		// cq.where(whereClause);
		// }
		return whereClause;
	}

	private static <E extends Entity> Expression<?> _addJoin(QueryRule rule, Root<E> root,
			Map<String, Join<?, ?>> joinHash) throws DatabaseException
	{
		try
		{
			String attributeName = rule.getJpaAttribute();

			if (attributeName == null) return null;

			if (rule.getValue() instanceof Entity) return root.get(attributeName);

			if (attributeName.contains(".") || root.get(attributeName).getJavaType().getName().equals("java.util.List")
					|| root.get(attributeName).getJavaType().newInstance() instanceof Entity)
			{

				Entity entity = root.getJavaType().newInstance();
				String xrefAttribtename = entity.getXrefIdFieldName(attributeName);

				String[] attributeNameSplit = StringUtils.split(attributeName, ".");
				if (attributeNameSplit.length == 0)
				{
					return null;
				}
				else if (attributeNameSplit.length > 1)
				{
					attributeName = attributeNameSplit[0];
					xrefAttribtename = attributeNameSplit[1];
				}

				Join<?, ?> join = null;
				if (joinHash.containsKey(attributeName))
				{
					join = joinHash.get(attributeName);
				}
				else
				{
					join = root.join(attributeName, JoinType.LEFT);
					joinHash.put(attributeName, join);
				}
				Expression<?> attribute = join.get(xrefAttribtename);

				return attribute;
			}
		}
		catch (InstantiationException ex)
		{
			// this is a hack, newInstance can not
			// be called on inmutable object
			// like Integer
		}
		catch (IllegalAccessException ex)
		{
			logger.error(ex);
			throw new DatabaseException(ex);
		}
		return null;
	}
}
