package org.molgenis.data.vcf.importer;

import org.molgenis.data.importer.ImportServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class VcfImporterServiceRegistrator implements ApplicationListener<ContextRefreshedEvent>
{
	private final VcfImporterService vcfImporterService;
	private final ImportServiceFactory importServiceFactory;

	@Autowired
	public VcfImporterServiceRegistrator(VcfImporterService vcfImporterService,
			ImportServiceFactory importServiceFactory)
	{
		super();
		this.vcfImporterService = vcfImporterService;
		this.importServiceFactory = importServiceFactory;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		importServiceFactory.addImportService(vcfImporterService);
	}

}
