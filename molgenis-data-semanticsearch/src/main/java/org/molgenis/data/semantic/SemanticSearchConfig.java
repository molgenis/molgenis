package org.molgenis.data.semantic;

import java.util.UUID;

import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.TagMetaData;
import org.molgenis.data.semanticsearch.repository.TagRepository;
import org.molgenis.data.semanticsearch.service.impl.UntypedTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.IdGenerator;

@Configuration
public class SemanticSearchConfig
{
	@Autowired
	private DataService dataService;

	@Bean
	TagRepository tagRepository()
	{
		Repository repo = dataService.getRepository(TagMetaData.ENTITY_NAME);
		return new TagRepository(repo, new IdGenerator()
		{

			@Override
			public UUID generateId()
			{
				return UUID.randomUUID();
			}
		});
	}

	@Bean
	public UntypedTagService tagService()
	{
		return new UntypedTagService(dataService, tagRepository());
	}
}
