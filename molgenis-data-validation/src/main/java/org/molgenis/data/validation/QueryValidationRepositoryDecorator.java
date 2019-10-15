package org.molgenis.data.validation;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;

/**
 * Query validation decorator that validates {@link Query queries} based on the repository {@link
 * org.molgenis.data.meta.model.EntityType}.
 *
 * @param <E> entity type
 * @see <a
 *     href="https://github.com/molgenis/molgenis/issues/5248">https://github.com/molgenis/molgenis/issues/5248</a>
 */
public class QueryValidationRepositoryDecorator<E extends Entity>
    extends AbstractRepositoryDecorator<E> {
  private final QueryValidator queryValidator;
  private final FetchValidator fetchValidator;

  public QueryValidationRepositoryDecorator(
      Repository<E> delegateRepository,
      QueryValidator queryValidator,
      FetchValidator fetchValidator) {
    super(delegateRepository);
    this.queryValidator = requireNonNull(queryValidator);
    this.fetchValidator = requireNonNull(fetchValidator);
  }

  @Override
  public long count(Query<E> q) {
    queryValidator.validate(q, getEntityType());
    return super.count(q);
  }

  @Override
  public Stream<E> findAll(Query<E> q) {
    queryValidator.validate(q, getEntityType());
    return super.findAll(q);
  }

  @Override
  public E findOne(Query<E> q) {
    queryValidator.validate(q, getEntityType());
    return super.findOne(q);
  }

  @Override
  public void forEachBatched(Fetch fetch, Consumer<List<E>> consumer, int batchSize) {
    Fetch validFetch = fetch != null ? fetchValidator.validateFetch(fetch, getEntityType()) : null;
    super.forEachBatched(validFetch, consumer, batchSize);
  }

  @Override
  public E findOneById(Object id, Fetch fetch) {
    Fetch validFetch = fetch != null ? fetchValidator.validateFetch(fetch, getEntityType()) : null;
    return super.findOneById(id, validFetch);
  }

  @Override
  public Stream<E> findAll(Stream<Object> ids, Fetch fetch) {
    Fetch validFetch = fetch != null ? fetchValidator.validateFetch(fetch, getEntityType()) : null;
    return super.findAll(ids, validFetch);
  }
}
