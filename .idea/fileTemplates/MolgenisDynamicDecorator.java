#set( $DECORATOR_NAME = $Decorator_name)
#set( $IDENTIFIER = $DECORATOR_NAME.toLowerCase())

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.decorator.DynamicRepositoryDecoratorFactory;
import org.springframework.stereotype.Component;

@Component
public class ${DECORATOR_NAME}RepositoryDecoratorFactory implements DynamicRepositoryDecoratorFactory
{
	private static final String ID = "${IDENTIFIER}";

	@Override
	@SuppressWarnings("unchecked")
	public Repository createDecoratedRepository(Repository repository)
	{
		return new ${DECORATOR_NAME}RepositoryDecorator(repository);
	}

	@Override
	public String getId()
	{
		return ID;
	}

	@Override
	public String getLabel()
	{
		return ""; //TODO
	}

	@Override
	public String getDescription()
	{
		return ""; //TODO
	}
}
    
public class ${DECORATOR_NAME}RepositoryDecorator extends AbstractRepositoryDecorator<Entity>
{
	public ${DECORATOR_NAME}RepositoryDecorator(Repository<Entity> delegateRepository)
	{
		super(delegateRepository);
	}

	// TODO override one or more methods from Repository
}