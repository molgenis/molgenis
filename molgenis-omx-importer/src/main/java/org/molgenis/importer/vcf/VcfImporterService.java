package org.molgenis.importer.vcf;

import java.io.File;
import java.io.IOException;

import org.elasticsearch.client.Client;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.elasticsearch.ElasticsearchRepository;
import org.molgenis.data.vcf.VcfRepositoryCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VcfImporterService
{
	private final DataService dataService;
	private final Client client;

	@Autowired
	public VcfImporterService(DataService dataService, Client client)
	{
		if (dataService == null) throw new IllegalArgumentException("data service is null");
		if (client == null) throw new IllegalArgumentException("client is null");
		this.dataService = dataService;
		this.client = client;
	}

	public void importVcf(File vcfFile, String entityName) throws IOException
	{
		RepositoryCollection repositoryCollection = new VcfRepositoryCollection(vcfFile, entityName);
		for (String inEntityName : repositoryCollection.getEntityNames())
		{
			Repository inRepository = repositoryCollection.getRepositoryByEntityName(inEntityName);

			if (dataService.hasRepository(inEntityName))
			{
				throw new IOException("Can't overwrite existing " + inEntityName);
			}

			EntityMetaData entityMetaData = inRepository.getEntityMetaData();
			CrudRepository outRepository = new ElasticsearchRepository(client, "molgenis", entityMetaData);
			try
			{
				outRepository.add(inRepository);
			}
			finally
			{
				outRepository.close();
			}
			dataService.addRepository(outRepository);
		}
	}
}
