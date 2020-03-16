package org.molgenis.data.elasticsearch.generator;

import org.molgenis.data.QueryRule.Operator;

class QueryClauseLessGenerator extends BaseQueryClauseRangeGenerator {
  QueryClauseLessGenerator(DocumentIdGenerator documentIdGenerator) {
    super(documentIdGenerator, Operator.LESS);
  }
}
