package org.molgenis.data.elasticsearch.generator;

import org.molgenis.data.QueryRule.Operator;

class QueryClauseLessEqualGenerator extends BaseQueryClauseRangeGenerator {
  QueryClauseLessEqualGenerator(DocumentIdGenerator documentIdGenerator) {
    super(documentIdGenerator, Operator.LESS_EQUAL);
  }
}
