package org.molgenis.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Objects.requireNonNull;

@Component
public class SystemRepositoryDecoratorFactoryRegistrar
{
	private final SystemRepositoryDecoratorRegistry repositoryDecoratorRegistry;

	@Autowired
	public SystemRepositoryDecoratorFactoryRegistrar(SystemRepositoryDecoratorRegistry repositoryDecoratorRegistry)
	{
		this.repositoryDecoratorRegistry = requireNonNull(repositoryDecoratorRegistry);
	}

	public void register(ContextRefreshedEvent event)
	{
		ApplicationContext ctx = event.getApplicationContext();
		Map<String, SystemRepositoryDecoratorFactory> repositoryDecoratorFactoryMap = ctx.getBeansOfType(
				SystemRepositoryDecoratorFactory.class);
		repositoryDecoratorFactoryMap.values()
									 .forEach(repositoryDecoratorRegistry::addFactory);
	}
}
