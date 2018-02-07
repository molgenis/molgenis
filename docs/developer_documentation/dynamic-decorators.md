**
This section describes how you can make use of the dynamic decorators
**

MOLGENIS decorators are used to add functionality, like for example security checks, to repository functionality.
Most of these decorators are applied to all repositories, with the dynamic decorators it is possible to configure for which entity types a decorator should be used.

# Creating a dynamic decorator
To create a dynamic decorator two classes are needed:
### The decorator
This class must extend AbstractRepositoryDecorator, and can be used to override the add, update and delete functions in this class.

Example decorator that logs the add and update of an entity:
```
package org.molgenis.app;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
  
public class LoggingRepositoryDecorator extends AbstractRepositoryDecorator<Entity>
{
  private static final Logger LOG = LoggerFactory.getLogger(LoggingRepositoryDecorator.class);

  public LoggingRepositoryDecorator(Repository<Entity> delegateRepository)
  {
    super(delegateRepository);
  }

  @Override
  public void add(Entity entity)
  {
    LOG.info("adding entity:'{}' of type '{}'",entity.getIdValue(),entity.getEntityType().getId());
    super.add(entity);
  }

  @Override
  public void update(Entity entity)
  {
    LOG.info("updating entity:'{}' of type '{}'",entity.getIdValue(),entity.getEntityType().getId());
    super.update(entity);
  }
}
```

### The factory for this decorator
This class must implement DynamicRepositoryDecoratorFactory

Example:
```$xslt
package org.molgenis.app;
  
import org.molgenis.data.Repository;
import org.molgenis.data.decorator.DynamicRepositoryDecoratorFactory;
import org.springframework.stereotype.Component;
  
@Component
public class LoggingRepositoryDecoratorFactory implements DynamicRepositoryDecoratorFactory
{
  private static final String ID = "log";

  @Override
  @SuppressWarnings("unchecked")
  public Repository createDecoratedRepository(Repository repository)
  {
    return new LoggingRepositoryDecorator(repository);
  }
  
  @Override
  public String getId()
  {
    return ID;
  }
      
  @Override
  public String getLabel()
  { 
    return "Logging Decorator";
  }
  
  @Override
  public String getDescription()
  {
    return "This is a demo decorator that logs the add(Entity entity) and update(Entity entity)";
  }
}
```

# Configuring a dynamic decorator for an entityType
To apply a dynamic decorator to an entity type, a row should be added to the "sys_dec_DecoratorConfiguration" via the dataexplorer, this entity contains:
- The identifier of the entitytype. 
- an mref to the decorators to be applied

Once this row is added the decorators are applied to the specified entity type.