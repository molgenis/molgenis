package org.molgenis.data.system;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.io.IOUtils;
import org.mockito.Mockito;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.system.FreemarkerTemplateMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.system.core.FreemarkerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@ContextConfiguration(classes = RepositoryTemplateLoaderTest.Config.class)
public class RepositoryTemplateLoaderTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	static class Config
	{
		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public RepositoryTemplateLoader repositoryTemplateLoader()
		{
			return new RepositoryTemplateLoader(dataService());
		}
	}

	@Autowired
	private DataService dataService;

	@Autowired
	private RepositoryTemplateLoader repositoryTemplateLoader;

	private FreemarkerTemplate template1;
	private FreemarkerTemplate template1Modified;
	private FreemarkerTemplate template2;

	@BeforeTest
	public void init()
	{
		template1 = new FreemarkerTemplate();
		template1.setId("1234");
		template1.setName("template1");
		template1.setValue("template1\ncontents");

		template1Modified = new FreemarkerTemplate();
		template1Modified.setId("1234");
		template1Modified.setName("template1");
		template1Modified.setValue("template1\nmodified contents");

		template2 = new FreemarkerTemplate();
		template2.setId("2345");
		template2.setName("template2");
		template2.setValue("template2\ncontents");
	}

	@BeforeMethod
	public void reset()
	{
		Mockito.reset(dataService);
	}

	@Test
	public void loadAndRead() throws IOException
	{
		when(
				dataService.findOne(FreemarkerTemplateMetaData.ENTITY_NAME, new QueryImpl().eq("Name", "template1"),
						FreemarkerTemplate.class)).thenReturn(template1);
		Object source = repositoryTemplateLoader.findTemplateSource("template1");
		assertNotNull(source);
		Reader reader = repositoryTemplateLoader.getReader(source, null);
		assertTrue(IOUtils.contentEquals(reader, new StringReader(template1.getValue())));
	}

	@Test
	public void lastModifiedEqualsMinusOne() throws IOException
	{
		when(
				dataService.findOne(FreemarkerTemplateMetaData.ENTITY_NAME, new QueryImpl().eq("Name", "template1"),
						FreemarkerTemplate.class)).thenReturn(template1);
		Object source = repositoryTemplateLoader.findTemplateSource("template1");
		assertTrue(repositoryTemplateLoader.getLastModified(source) == -1);
	}

	@Test
	public void newSourceReturnedWhenContentChanges() throws IOException
	{
		when(
				dataService.findOne(FreemarkerTemplateMetaData.ENTITY_NAME, new QueryImpl().eq("Name", "template1"),
						FreemarkerTemplate.class)).thenReturn(template1, template1Modified);
		Object source = repositoryTemplateLoader.findTemplateSource("template1");
		assertTrue(IOUtils.contentEquals(repositoryTemplateLoader.getReader(source, null),
				new StringReader(template1.getValue())));
		Object modifiedSource = repositoryTemplateLoader.findTemplateSource("template1");
		assertNotEquals(source, modifiedSource);
		assertTrue(IOUtils.contentEquals(repositoryTemplateLoader.getReader(modifiedSource, null), new StringReader(
				template1Modified.getValue())));
	}

	@Test
	public void sourceBelongsToContentAndCanBeReadMultipleTimes() throws IOException
	{
		when(
				dataService.findOne(FreemarkerTemplateMetaData.ENTITY_NAME, new QueryImpl().eq("Name", "template1"),
						FreemarkerTemplate.class)).thenReturn(template1);
		when(
				dataService.findOne(FreemarkerTemplateMetaData.ENTITY_NAME, new QueryImpl().eq("Name", "template2"),
						FreemarkerTemplate.class)).thenReturn(template2);

		Object source1 = repositoryTemplateLoader.findTemplateSource("template1");
		Object source2 = repositoryTemplateLoader.findTemplateSource("template2");

		assertTrue(IOUtils.contentEquals(repositoryTemplateLoader.getReader(source2, null),
				new StringReader(template2.getValue())));

		assertTrue(IOUtils.contentEquals(repositoryTemplateLoader.getReader(source1, null),
				new StringReader(template1.getValue())));

		assertTrue(IOUtils.contentEquals(repositoryTemplateLoader.getReader(source1, null),
				new StringReader(template1.getValue())));

		assertTrue(IOUtils.contentEquals(repositoryTemplateLoader.getReader(source1, null),
				new StringReader(template1.getValue())));

		assertTrue(IOUtils.contentEquals(repositoryTemplateLoader.getReader(source2, null),
				new StringReader(template2.getValue())));

	}
}
