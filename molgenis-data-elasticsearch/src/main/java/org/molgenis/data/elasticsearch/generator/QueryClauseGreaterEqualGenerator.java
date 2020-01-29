package org.molgenis.data.elasticsearch.generator;

import org.molgenis.data.QueryRule.Operator;

class QueryClauseGreaterEqualGenerator extends BaseQueryClauseRangeGenerator {
  QueryClauseGreaterEqualGenerator(DocumentIdGenerator documentIdGenerator) {
    super(documentIdGenerator, Operator.GREATER_EQUAL);
  }
}
