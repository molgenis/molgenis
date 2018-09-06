package org.molgenis.data.icd10;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import java.util.Map;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.decorator.DynamicRepositoryDecoratorFactory;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unused")
public class Icd10ExpanderDecoratorFactory implements DynamicRepositoryDecoratorFactory<Entity> {
  private static final String ID = "icd10expander";
  private static final String ICD10_ENTITY_TYPE_ID = "icd10EntityTypeId";
  private static final String EXPAND_ATTRIBUTE = "expandAttribute";

  private final Gson gson;
  private final CollectionsQueryTransformer queryTransformer;

  public Icd10ExpanderDecoratorFactory(Gson gson, CollectionsQueryTransformer queryTransformer) {
    this.gson = requireNonNull(gson);
    this.queryTransformer = requireNonNull(queryTransformer);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Repository createDecoratedRepository(
      Repository<Entity> repository, Map<String, Object> parameters) {
    return new Icd10ExpanderDecorator(
        repository,
        queryTransformer,
        parameters.get(ICD10_ENTITY_TYPE_ID).toString(),
        parameters.get(EXPAND_ATTRIBUTE).toString());
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getLabel() {
    return "ICD-10 Query Expander";
  }

  @Override
  public String getDescription() {
    return "Expands queries on an attribute that refers to an entity type with ICD-10 codes.";
  }

  @Override
  public String getSchema() {
    return gson.toJson(
        of(
            "title",
            "Icd10Expander",
            "type",
            "object",
            "properties",
            of(
                ICD10_ENTITY_TYPE_ID,
                of("type", "string", "description", "The entity type containing the ICD-10 data."),
                EXPAND_ATTRIBUTE,
                of(
                    "type",
                    "string",
                    "description",
                    "The attribute on which the query expansion will be applied.")),
            "required",
            ImmutableList.of(ICD10_ENTITY_TYPE_ID, EXPAND_ATTRIBUTE)));
  }
}
