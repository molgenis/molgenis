package org.molgenis.data.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.Repository;
import org.molgenis.data.Sort;

public class QueryImpl<E extends Entity> implements Query<E> {
  private final List<List<QueryRule>> rules = new ArrayList<>();

  private int offset;
  private int pageSize;
  private Sort sort;
  /** {@link Fetch} that defines which entity attributes to retrieve. */
  private Fetch fetch;

  @Nullable
  @CheckForNull
  @Override
  public Repository<E> getRepository() {
    return repository;
  }

  public void setRepository(Repository<E> repository) {
    this.repository = repository;
  }

  private Repository<E> repository;

  public static <T extends Entity> Query<T> query() {
    return new QueryImpl<>();
  }

  public QueryImpl() {
    this.rules.add(new ArrayList<>());
  }

  public QueryImpl(Repository<E> repository) {
    this();
    this.repository = repository;
  }

  public QueryImpl(Query<E> q) {
    this();
    for (QueryRule rule : q.getRules()) {
      addRule(rule);
    }
    this.pageSize = q.getPageSize();
    this.offset = q.getOffset();
    this.sort = q.getSort();
    this.fetch = q.getFetch();
  }

  public QueryImpl(QueryRule queryRule) {
    this();
    addRule(queryRule);
  }

  public QueryImpl(List<QueryRule> queryRules) {
    this();
    queryRules.forEach(this::addRule);
  }

  public void addRule(QueryRule rule) {
    this.rules.get(this.rules.size() - 1).add(rule);
  }

  @Override
  public Long count() {
    if (repository == null) throw new RuntimeException("Query failed: repository not set");
    return repository.count(this);
  }

  @Override
  public Stream<E> findAll() {
    if (repository == null) throw new RuntimeException("Query failed: repository not set");
    return repository.findAll(this);
  }

  @Nullable
  @CheckForNull
  @Override
  public E findOne() {
    if (repository == null) throw new RuntimeException("Query failed: repository not set");
    return repository.findOne(this);
  }

  @Override
  public List<QueryRule> getRules() {
    if (this.rules.size() > 1)
      throw new MolgenisDataException("Nested query not closed, use unnest() or unnestAll()");

    if (!this.rules.isEmpty()) {
      List<QueryRule> queryRules = this.rules.get(this.rules.size() - 1);

      return Collections.unmodifiableList(queryRules);
    } else return Collections.emptyList();
  }

  @Override
  public int getPageSize() {
    return pageSize;
  }

  public QueryImpl<E> setPageSize(int pageSize) {
    this.pageSize = pageSize;
    return this;
  }

  @Override
  public int getOffset() {
    return offset;
  }

  public QueryImpl<E> setOffset(int offset) {
    this.offset = offset;
    return this;
  }

  @Nullable
  @CheckForNull
  @Override
  public Sort getSort() {
    return sort;
  }

  public void setSort(Sort sort) {
    this.sort = sort;
  }

  @Override
  public Query<E> search(String searchTerms) {
    rules.get(this.rules.size() - 1).add(new QueryRule(Operator.SEARCH, searchTerms));
    return this;
  }

  @Override
  public Query<E> search(String field, String searchTerms) {
    rules.get(this.rules.size() - 1).add(new QueryRule(field, Operator.SEARCH, searchTerms));
    return this;
  }

  @Override
  public Query<E> or() {
    rules.get(this.rules.size() - 1).add(new QueryRule(Operator.OR));
    return this;
  }

  @Override
  public Query<E> and() {
    rules.get(this.rules.size() - 1).add(new QueryRule(Operator.AND));
    return this;
  }

  @Override
  public Query<E> not() {
    rules.get(this.rules.size() - 1).add(new QueryRule(Operator.NOT));
    return this;
  }

  @Override
  public Query<E> like(String field, String value) {
    rules.get(this.rules.size() - 1).add(new QueryRule(field, Operator.LIKE, value));
    return this;
  }

  @Override
  public Query<E> eq(String field, Object value) {
    rules.get(this.rules.size() - 1).add(new QueryRule(field, Operator.EQUALS, value));
    return this;
  }

  @Override
  public Query<E> in(String field, Iterable<?> valueIterable) {
    rules.get(this.rules.size() - 1).add(new QueryRule(field, Operator.IN, valueIterable));
    return this;
  }

  @Override
  public Query<E> gt(String field, Object value) {
    rules.get(this.rules.size() - 1).add(new QueryRule(field, Operator.GREATER, value));
    return this;
  }

  @Override
  public Query<E> ge(String field, Object value) {
    rules.get(this.rules.size() - 1).add(new QueryRule(field, Operator.GREATER_EQUAL, value));
    return this;
  }

  @Override
  public Query<E> le(String field, Object value) {
    rules.get(this.rules.size() - 1).add(new QueryRule(field, Operator.LESS_EQUAL, value));
    return this;
  }

  @Override
  public Query<E> lt(String field, Object value) {
    rules.get(this.rules.size() - 1).add(new QueryRule(field, Operator.LESS, value));
    return this;
  }

  @Override
  public Query<E> nest() {
    // add element to our nesting list...
    this.rules.add(new ArrayList<>());
    return this;
  }

  @Override
  public Query<E> unnest() {
    if (this.rules.size() == 1)
      throw new MolgenisDataException("Cannot unnest root element of query");

    // remove last element and add to parent as nested rule
    QueryRule nested = new QueryRule(Operator.NESTED, this.rules.get(this.rules.size() - 1));
    this.rules.remove(this.rules.get(this.rules.size() - 1));
    this.rules.get(this.rules.size() - 1).add(nested);
    return this;
  }

  @Override
  public Query<E> unnestAll() {
    while (this.rules.size() > 1) {
      unnest();
    }
    return this;
  }

  @Override
  public Query<E> rng(String field, Object from, Object to) {
    rules
        .get(this.rules.size() - 1)
        .add(new QueryRule(field, Operator.RANGE, Arrays.asList(from, to)));
    return this;
  }

  @Override
  public Query<E> pageSize(int pageSize) {
    setPageSize(pageSize);
    return this;
  }

  @Override
  public Query<E> offset(int offset) {
    setOffset(offset);
    return this;
  }

  @Override
  public Sort sort() {
    this.sort = new Sort();
    return this.sort;
  }

  @Override
  public Query<E> sort(Sort sort) {
    setSort(sort);
    return this;
  }

  @Override
  public Iterator<E> iterator() {
    if (repository == null) throw new RuntimeException("Query failed: repository not set");
    return repository.findAll(this).iterator();
  }

  @Nullable
  @CheckForNull
  @Override
  public Fetch getFetch() {
    return fetch;
  }

  @Override
  public void setFetch(Fetch fetch) {
    this.fetch = fetch;
  }

  @Override
  public Fetch fetch() {
    this.fetch = new Fetch();
    return getFetch();
  }

  @Override
  public Query<E> fetch(Fetch fetch) {
    setFetch(fetch);
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    QueryImpl<?> query = (QueryImpl<?>) o;
    return offset == query.offset
        && pageSize == query.pageSize
        && Objects.equals(rules, query.rules)
        && Objects.equals(sort, query.sort);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rules, offset, pageSize, sort);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (!rules.isEmpty()) {
      if (rules.size() == 1) {
        List<QueryRule> rule = rules.get(0);
        if (!rule.isEmpty()) {
          builder.append("rules=").append(rule);
        }
      } else {
        builder.append("rules=").append(rules);
      }
    }
    if (offset != 0) {
      if (builder.length() > 0) {
        builder.append(", ");
      }
      builder.append("offset=").append(offset);
    }
    if (pageSize != 0) {
      if (builder.length() > 0) {
        builder.append(", ");
      }
      builder.append("pageSize=").append(pageSize);
    }
    if (sort != null) {
      if (builder.length() > 0) {
        builder.append(", ");
      }
      builder.append("sort=").append(sort);
    }
    if (fetch != null) {
      if (builder.length() > 0) {
        builder.append(", ");
      }
      builder.append("fetch=").append(fetch);
    }
    return builder.toString();
  }
}
