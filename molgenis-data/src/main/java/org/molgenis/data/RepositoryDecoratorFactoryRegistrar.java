package org.molgenis.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Objects.requireNonNull;

@Component
public class RepositoryDecoratorFactoryRegistrar
{
	private final RepositoryDecoratorRegistry repositoryDecoratorRegistry;

	@Autowired
	public RepositoryDecoratorFactoryRegistrar(RepositoryDecoratorRegistry repositoryDecoratorRegistry)
	{
		this.repositoryDecoratorRegistry = requireNonNull(repositoryDecoratorRegistry);
	}

	public void register(ContextRefreshedEvent event)
	{
		ApplicationContext ctx = event.getApplicationContext();
		Map<String, StaticEntityRepositoryDecoratorFactory> repositoryDecoratorFactoryMap = ctx
				.getBeansOfType(StaticEntityRepositoryDecoratorFactory.class);
		repositoryDecoratorFactoryMap.values().forEach(
				(repositoryDecoratorFactory) -> repositoryDecoratorRegistry.addFactory(repositoryDecoratorFactory));
	}
}
