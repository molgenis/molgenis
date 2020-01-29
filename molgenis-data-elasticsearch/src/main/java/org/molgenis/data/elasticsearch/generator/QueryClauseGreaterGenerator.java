package org.molgenis.data.elasticsearch.generator;

import org.molgenis.data.QueryRule.Operator;

class QueryClauseGreaterGenerator extends BaseQueryClauseRangeGenerator {
  QueryClauseGreaterGenerator(DocumentIdGenerator documentIdGenerator) {
    super(documentIdGenerator, Operator.GREATER);
  }
}
