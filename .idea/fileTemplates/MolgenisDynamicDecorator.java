#set( $DECORATOR_NAME = $Decorator_name)
#set( $IDENTIFIER = $DECORATOR_NAME.toLowerCase())

import static java.util.Objects.requireNonNull;
import static com.google.common.collect.ImmutableMap.of;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import java.util.Map;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.decorator.DynamicRepositoryDecoratorFactory;
import org.springframework.stereotype.Component;

@Component
public class ${DECORATOR_NAME}RepositoryDecoratorFactory
    implements DynamicRepositoryDecoratorFactory<Entity> {
  private static final String ID = "${IDENTIFIER}";
  private final Gson gson;

  public ${DECORATOR_NAME}RepositoryDecoratorFactory(Gson gson) {
    this.gson = requireNonNull(gson);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Repository createDecoratedRepository(Repository<Entity> repository, Map<String, Object> parameters) {
    return new ${DECORATOR_NAME}RepositoryDecorator(repository);
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getLabel() {
    return ""; //TODO
  }

  @Override
  public String getDescription() {
    return ""; //TODO
  }

  @Override
  public String getSchema() {
    return gson.toJson(
        of(
            "title",
            "${DECORATOR_NAME}",
            "type",
            "object",
            "properties",
            of("example", of("type", "string", "description", "An example of a parameter")),
            "required", ImmutableList.of("example"))); //TODO
  }
}
    
public class ${DECORATOR_NAME}RepositoryDecorator extends AbstractRepositoryDecorator<Entity> {
  public ${DECORATOR_NAME}RepositoryDecorator(Repository<Entity> delegateRepository) {
   super(delegateRepository);
  }

  // TODO override one or more methods from Repository
}
