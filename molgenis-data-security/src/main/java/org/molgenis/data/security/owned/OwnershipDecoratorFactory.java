package org.molgenis.data.security.owned;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;

import com.google.gson.Gson;
import java.util.Map;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.decorator.DynamicRepositoryDecoratorFactory;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.stereotype.Component;

@Component
public class OwnershipDecoratorFactory implements DynamicRepositoryDecoratorFactory<Entity> {

  private static final String ID = "ownership";
  private static final String OWNER_ATTRIBUTE = "ownerAttribute";
  private final Gson gson;
  private final MutableAclService mutableAclService;

  public OwnershipDecoratorFactory(Gson gson, MutableAclService mutableAclService) {
    this.gson = requireNonNull(gson);
    this.mutableAclService = requireNonNull(mutableAclService);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Repository createDecoratedRepository(
      Repository<Entity> repository, Map<String, Object> parameters) {
    String ownerAttribute = parameters.get(OWNER_ATTRIBUTE).toString();
    return new OwnershipDecorator(repository, mutableAclService, ownerAttribute);
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getLabel() {
    return "Ownership decorator";
  }

  @Override
  public String getDescription() {
    return "When entities are added to the decorated repository, their owner is set to the value of the ownerAttribute.";
  }

  @Override
  public String getSchema() {
    return gson.toJson(
        of(
            "title",
            "Questionnaire",
            "type",
            "object",
            "properties",
            of(OWNER_ATTRIBUTE, of("type", "string", "description", "Name of the owner attribute")),
            "required",
            singleton(OWNER_ATTRIBUTE)));
  }
}
