package org.molgenis.data.validation.query;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Query validation decorator that validates {@link Query queries} based on the repository
 * {@link org.molgenis.data.meta.model.EntityType}.
 *
 * @param <E> entity type
 * @see <a href="https://github.com/molgenis/molgenis/issues/5248">https://github.com/molgenis/molgenis/issues/5248</a>
 */
public class QueryValidationRepositoryDecorator<E extends Entity> extends AbstractRepositoryDecorator<E>
{
	private final QueryValidator queryValidator;

	public QueryValidationRepositoryDecorator(Repository<E> delegateRepository, QueryValidator queryValidator)
	{
		super(delegateRepository);
		this.queryValidator = requireNonNull(queryValidator);
	}

	@Override
	public long count(Query<E> q)
	{
		queryValidator.validate(q, getEntityType());
		return super.count(q);
	}

	@Override
	public Stream<E> findAll(Query<E> q)
	{
		queryValidator.validate(q, getEntityType());
		return super.findAll(q);
	}

	@Override
	public E findOne(Query<E> q)
	{
		queryValidator.validate(q, getEntityType());
		return super.findOne(q);
	}
}
