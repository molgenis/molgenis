package org.molgenis.data.importer;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Discovers {@link ImportService} beans and registers them at the {@link ImportServiceFactory}
 */
@Component
public class ImportServiceRegistrar
{
	private final ImportServiceFactory importServiceFactory;

	public ImportServiceRegistrar(ImportServiceFactory importServiceFactory)
	{
		this.importServiceFactory = requireNonNull(importServiceFactory);
	}

	public void register(ContextRefreshedEvent event)
	{
		ApplicationContext ctx = event.getApplicationContext();
		Map<String, ImportService> importServiceMap = ctx.getBeansOfType(ImportService.class);
		importServiceMap.values().forEach(this::register);
	}

	private void register(ImportService importService)
	{
		importServiceFactory.addImportService(importService);
	}
}
