package org.molgenis.vkgl;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.elasticsearch.ElasticsearchRepository;
import org.molgenis.data.elasticsearch.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class VkglImportService
{
	private static final int BATCH_SIZE = 1000;
	private static final String SAMPLES = "SAMPLES";

	private final FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	private final DataService dataService;
	private final SearchService searchService;

	@Autowired
	public VkglImportService(FileRepositoryCollectionFactory fileRepositoryCollectionFactory, DataService dataService,
			SearchService searchService)
	{
		if (fileRepositoryCollectionFactory == null) throw new IllegalArgumentException(
				"fileRepositoryCollectionFactory is null");
		if (dataService == null) throw new IllegalArgumentException("dataservice is null");
		if (searchService == null) throw new IllegalArgumentException("seachservice is null");

		this.fileRepositoryCollectionFactory = fileRepositoryCollectionFactory;
		this.dataService = dataService;
		this.searchService = searchService;
	}

	public void importVcf(File vcfFile) throws IOException
	{
		RepositoryCollection repositoryCollection = fileRepositoryCollectionFactory
				.createFileRepositoryCollection(vcfFile);

		Iterator<String> it = repositoryCollection.getEntityNames().iterator();
		if (it.hasNext())
		{
			Repository repo = repositoryCollection.getRepositoryByEntityName(it.next());
			try
			{
				repo.getEntityMetaData();// Initialize
				importVcf(repo);
			}
			finally
			{
				IOUtils.closeQuietly(repo);
			}
		}
	}

	private void importVcf(Repository inRepo) throws IOException
	{
		CrudRepository outRepo = null;

		try
		{
			if (!dataService.hasRepository(VkglEntityMetaData.INSTANCE.getName()))
			{
				outRepo = new ElasticsearchRepository(VkglEntityMetaData.INSTANCE, searchService);
				searchService.createMappings(VkglEntityMetaData.INSTANCE, true, true, true, true);
				dataService.addRepository(outRepo);
			}

			outRepo = dataService.getCrudRepository(VkglEntityMetaData.INSTANCE.getName());

			List<Entity> batch = Lists.newArrayList();
			for (Entity entity : inRepo)
			{
				entity.set(VkglEntityMetaData.INTERNAL_ID, UUID.randomUUID().toString().replaceAll("-", ""));
				Iterable<Entity> samples = entity.getEntities(SAMPLES);
				if (samples != null)
				{
					// Only the first
					Iterator<Entity> it = samples.iterator();
					if (it.hasNext())
					{
						entity.set(VkglEntityMetaData.GT, samples.iterator().next().get(VkglEntityMetaData.GT));
					}
				}

				batch.add(entity);
				if (batch.size() == BATCH_SIZE)
				{
					outRepo.add(batch);
					batch.clear();
				}
			}

			if (!batch.isEmpty())
			{
				outRepo.add(batch);
			}
		}
		finally
		{
			IOUtils.closeQuietly(outRepo);
		}
	}
}
