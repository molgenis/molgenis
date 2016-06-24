package org.molgenis.data.semanticsearch.repository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.UUID;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.TagMetaData;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration(classes = TagRepositoryTest.Config.class)
public class TagRepositoryTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private Repository repository;

	private TagRepository tagRepository;

	@Autowired
	private IdGenerator idGenerator;

	@Autowired
	private AttributeMetaData attributeMetaData;

	private final UUID uuid = UUID.randomUUID();

	@BeforeMethod
	public void beforeMethod()
	{
		tagRepository = new TagRepository(repository, idGenerator);
		when(idGenerator.generateId()).thenReturn(uuid.toString());
	}

	@Test
	public void testGetTagEntity()
	{
		MapEntity expected = new MapEntity(TagMetaData.ENTITY_NAME);
		expected.set(TagMetaData.IDENTIFIER, uuid.toString());
		expected.set(TagMetaData.OBJECT_IRI, "http://edamontology.org/data_3031");
		expected.set(TagMetaData.LABEL, "Core data");
		expected.set(TagMetaData.RELATION_IRI, "http://molgenis.org/biobankconnect/instanceOf");
		expected.set(TagMetaData.RELATION_LABEL, "instanceOf");
		expected.set(TagMetaData.CODE_SYSTEM, "http://edamontology.org");

		assertEquals(tagRepository.getTagEntity("http://edamontology.org/data_3031", "Core data", Relation.instanceOf,
				"http://edamontology.org"), expected);

		when(
				repository.findOne(new QueryImpl().eq(TagMetaData.OBJECT_IRI, "http://edamontology.org/data_3031")
						.and().eq(TagMetaData.RELATION_IRI, "http://molgenis.org/biobankconnect/instanceOf").and()
						.eq(TagMetaData.CODE_SYSTEM, "http://edamontology.org"))).thenReturn(expected);

		assertEquals(tagRepository.getTagEntity("http://edamontology.org/data_3031", "Core data", Relation.instanceOf,
				"http://edamontology.org"), expected);

		verify(repository, times(1)).add(expected);
	}

	@Configuration
	public static class Config
	{
		@Bean
		Repository repository()
		{
			return mock(Repository.class);
		}

		@Bean
		IdGenerator idGenerator()
		{
			return mock(IdGenerator.class);
		}

		@Bean
		AttributeMetaData attributeMetaData()
		{
			return mock(AttributeMetaData.class);
		}
	}
}
