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
public class PostFixingRepositoryDecoratorFactory
    implements DynamicRepositoryDecoratorFactory<Entity> {
  private static final String PARAM_ATTRIBUTE = "attr";
  private static final String PARAM_TEXT = "text";

  private static final String ID = "postfix";
  private final Gson gson;

  public PostFixingRepositoryDecoratorFactory(Gson gson) {
    this.gson = requireNonNull(gson);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Repository createDecoratedRepository(
      Repository<Entity> repository, Map<String, Object> parameters) {
    return new PostFixingRepositoryDecorator(
        repository,
        parameters.get(PARAM_ATTRIBUTE).toString(),
        parameters.get(PARAM_TEXT).toString());
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getLabel() {
    return "postfix";
  }

  @Override
  public String getDescription() {
    return "This is a test decorator";
  }

  @Override
  public String getSchema() {
    return gson.toJson(
        of(
            "title",
            "Postfixing Decorator",
            "type",
            "object",
            "properties",
            of(
                PARAM_ATTRIBUTE,
                of("type", "string", "description", "The attribute to increment"),
                PARAM_TEXT,
                of("type", "string", "description", "The text to append")),
            "required",
            ImmutableList.of("attr", "text")));
  }
}
