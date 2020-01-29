package org.molgenis.data.elasticsearch.generator;

import org.elasticsearch.index.query.QueryBuilder;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.model.EntityType;

interface QueryClauseGenerator {
  Operator getOperator();

  QueryBuilder createQueryClause(QueryRule queryRule, EntityType entityType);
}
