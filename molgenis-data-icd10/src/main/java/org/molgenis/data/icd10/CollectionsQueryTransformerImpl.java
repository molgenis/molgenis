package org.molgenis.data.icd10;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.TreeTraverser;
import java.util.Collection;
import java.util.List;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.springframework.stereotype.Component;

@Component
public class CollectionsQueryTransformerImpl implements CollectionsQueryTransformer {
  private static final TreeTraverser<QueryRule> RULE_TRAVERSER =
      TreeTraverser.using(QueryRule::getNestedRules);

  private final Icd10ClassExpander icd10ClassExpander;
  private final DataService dataService;

  CollectionsQueryTransformerImpl(Icd10ClassExpander icd10ClassExpander, DataService dataService) {
    this.icd10ClassExpander = requireNonNull(icd10ClassExpander);
    this.dataService = requireNonNull(dataService);
  }

  @Override
  public Query<Entity> transformQuery(
      Query<Entity> query, String icd10EntityTypeId, String expandAttribute) {
    if (query != null && query.getRules() != null && !query.getRules().isEmpty()) {
      query
          .getRules()
          .forEach(
              rule ->
                  RULE_TRAVERSER
                      .preOrderTraversal(rule)
                      .filter(nestedRule -> isTransformableRule(nestedRule, expandAttribute))
                      .forEach(nestedRule -> transformQueryRule(nestedRule, icd10EntityTypeId)));
    }

    return query;
  }

  private void transformQueryRule(QueryRule rule, String icd10EntityTypeId) {
    List<Object> queryValues;

    switch (rule.getOperator()) {
      case EQUALS:
        queryValues = singletonList(rule.getValue());
        rule.setOperator(QueryRule.Operator.IN);
        break;
      case IN:
        //noinspection unchecked
        queryValues = (List<Object>) rule.getValue();
        break;
      default:
        throw new IllegalStateException("Can't expand queries other than IN or EQUALS");
    }

    List<Entity> diseaseTypes =
        dataService.findAll(icd10EntityTypeId, queryValues.stream()).collect(toList());

    rule.setValue(expandDiseaseTypes(diseaseTypes));
  }

  /**
   * Returns <code>true</code> if a rule is 'IN' or 'EQUALS' on the attribute that should be
   * expanded
   */
  private boolean isTransformableRule(QueryRule nestedRule, String expandAttribute) {
    return nestedRule != null
        && nestedRule.getField() != null
        && nestedRule.getField().equals(expandAttribute)
        && (nestedRule.getOperator() == QueryRule.Operator.IN
            || nestedRule.getOperator() == QueryRule.Operator.EQUALS);
  }

  /** Expand ICD-10 entities with all their children, grandchildren, etc. */
  private Collection<Entity> expandDiseaseTypes(List<Entity> diseaseTypes) {
    return icd10ClassExpander.expandClasses(diseaseTypes);
  }
}
