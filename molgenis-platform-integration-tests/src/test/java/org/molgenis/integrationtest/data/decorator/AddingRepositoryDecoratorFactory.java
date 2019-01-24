package org.molgenis.integrationtest.data.decorator;

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
public class AddingRepositoryDecoratorFactory implements DynamicRepositoryDecoratorFactory<Entity> {
  private static final String PARAM_ATTRIBUTE = "attr";

  private static final String ID = "add";
  private final Gson gson;

  public AddingRepositoryDecoratorFactory(Gson gson) {
    this.gson = requireNonNull(gson);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Repository<Entity> createDecoratedRepository(
      Repository<Entity> repository, Map<String, Object> parameters) {
    return new AddingRepositoryDecorator(repository, parameters.get(PARAM_ATTRIBUTE).toString());
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getLabel() {
    return "add";
  }

  @Override
  public String getDescription() {
    return "This is a test decorator.";
  }

  @Override
  public String getSchema() {
    return gson.toJson(
        of(
            "title",
            "Adding Decorator",
            "type",
            "object",
            "properties",
            of(PARAM_ATTRIBUTE, of("type", "string", "description", "The attribute to increment")),
            "required",
            ImmutableList.of("attr")));
  }
}
