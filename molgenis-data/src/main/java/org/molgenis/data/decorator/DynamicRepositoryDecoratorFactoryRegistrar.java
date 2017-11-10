package org.molgenis.data.decorator;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Objects.requireNonNull;

@Component
public class DynamicRepositoryDecoratorFactoryRegistrar
{
	private final DynamicRepositoryDecoratorRegistry repositoryDecoratorRegistry;

	public DynamicRepositoryDecoratorFactoryRegistrar(DynamicRepositoryDecoratorRegistry repositoryDecoratorRegistry)
	{
		this.repositoryDecoratorRegistry = requireNonNull(repositoryDecoratorRegistry);
	}

	public void register(ContextRefreshedEvent event)
	{
		ApplicationContext ctx = event.getApplicationContext();
		Map<String, DynamicRepositoryDecoratorFactory> repositoryDecoratorFactoryMap = ctx.getBeansOfType(
				DynamicRepositoryDecoratorFactory.class);
		repositoryDecoratorFactoryMap.values().forEach(repositoryDecoratorRegistry::addFactory);
	}
}