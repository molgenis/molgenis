package org.molgenis.data.icd10;

import static java.util.Objects.requireNonNull;

import java.util.stream.Stream;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.support.AggregateQueryImpl;

class Icd10ExpanderDecorator extends AbstractRepositoryDecorator<Entity> {
  private final CollectionsQueryTransformer queryTransformer;
  private final String icd10EntityTypeId;
  private final String expandAttribute;

  Icd10ExpanderDecorator(
      Repository<Entity> delegateRepository,
      CollectionsQueryTransformer queryTransformer,
      String icd10EntityTypeId,
      String expandAttribute) {
    super(delegateRepository);
    this.queryTransformer = requireNonNull(queryTransformer);
    this.icd10EntityTypeId = requireNonNull(icd10EntityTypeId);
    this.expandAttribute = requireNonNull(expandAttribute);
  }

  @Override
  public long count(Query<Entity> query) {
    query = query != null ? transformQuery(query) : null;
    return delegate().count(query);
  }

  @Override
  public Entity findOne(Query<Entity> query) {
    query = query != null ? transformQuery(query) : null;
    return delegate().findOne(query);
  }

  @Override
  public Stream<Entity> findAll(Query<Entity> query) {
    query = query != null ? transformQuery(query) : null;
    return delegate().findAll(query);
  }

  @Override
  public AggregateResult aggregate(AggregateQuery aggregateQuery) {
    Query<Entity> q = aggregateQuery.getQuery();
    Query<Entity> transformedQuery = q != null ? transformQuery(q) : null;
    aggregateQuery =
        new AggregateQueryImpl(
            aggregateQuery.getAttributeX(),
            aggregateQuery.getAttributeY(),
            aggregateQuery.getAttributeDistinct(),
            transformedQuery);
    return delegate().aggregate(aggregateQuery);
  }

  private Query<Entity> transformQuery(Query<Entity> query) {
    return queryTransformer.transformQuery(query, icd10EntityTypeId, expandAttribute);
  }
}
