package org.molgenis.integrationtest.data.decorator;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;

// Decorator specifically for DynamicDecoratorIT
public class PostFixingRepositoryDecorator extends AbstractRepositoryDecorator<Entity> {
  private final String attributeName;
  private final String text;

  PostFixingRepositoryDecorator(
      Repository<Entity> delegateRepository, String attributeName, String text) {
    super(delegateRepository);
    this.attributeName = attributeName;
    this.text = text;
  }

  @Override
  public void update(Entity entity) {
    entity.set(attributeName, entity.getString(attributeName) + text);
    super.update(entity);
  }
}
