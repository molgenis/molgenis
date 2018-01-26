package org.molgenis.core.ui.freemarker;

import org.apache.commons.io.IOUtils;
import org.mockito.Mockito;
import org.molgenis.core.ui.data.system.core.FreemarkerTemplate;
import org.molgenis.core.ui.data.system.core.FreemarkerTemplateFactory;
import org.molgenis.core.ui.data.system.core.FreemarkerTemplateMetaData;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.mockito.Mockito.when;
import static org.molgenis.core.ui.data.system.core.FreemarkerTemplateMetaData.FREEMARKER_TEMPLATE;
import static org.testng.Assert.*;

@ContextConfiguration(classes = RepositoryTemplateLoaderTest.Config.class)
public class RepositoryTemplateLoaderTest extends AbstractMolgenisSpringTest
{
	@Configuration
	@Import({ FreemarkerTemplateMetaData.class, FreemarkerTemplateFactory.class })
	static class Config
	{
		@Autowired
		private DataService dataService;

		@Bean
		public RepositoryTemplateLoader repositoryTemplateLoader()
		{
			return new RepositoryTemplateLoader(dataService);
		}
	}

	@Autowired
	private FreemarkerTemplateFactory freemarkerTemplateFactory;

	@Autowired
	private DataService dataService;

	@Autowired
	private RepositoryTemplateLoader repositoryTemplateLoader;

	private FreemarkerTemplate template1;
	private FreemarkerTemplate template1Modified;
	private FreemarkerTemplate template2;

	@BeforeMethod
	public void init()
	{
		template1 = freemarkerTemplateFactory.create();
		template1.setId("1234");
		template1.setName("template1");
		template1.setValue("template1\ncontents");

		template1Modified = freemarkerTemplateFactory.create();
		template1Modified.setId("1234");
		template1Modified.setName("template1");
		template1Modified.setValue("template1\nmodified contents");

		template2 = freemarkerTemplateFactory.create();
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
		when(dataService.findOne(FREEMARKER_TEMPLATE, new QueryImpl<FreemarkerTemplate>().eq("Name", "template1"),
				FreemarkerTemplate.class)).thenReturn(template1);
		Object source = repositoryTemplateLoader.findTemplateSource("template1");
		assertNotNull(source);
		Reader reader = repositoryTemplateLoader.getReader(source, null);
		assertTrue(IOUtils.contentEquals(reader, new StringReader(template1.getValue())));
	}

	@Test
	public void lastModifiedEqualsMinusOne() throws IOException
	{
		when(dataService.findOne(FREEMARKER_TEMPLATE, new QueryImpl<FreemarkerTemplate>().eq("Name", "template1"),
				FreemarkerTemplate.class)).thenReturn(template1);
		Object source = repositoryTemplateLoader.findTemplateSource("template1");
		assertTrue(repositoryTemplateLoader.getLastModified(source) == -1);
	}

	@Test
	public void newSourceReturnedWhenContentChanges() throws IOException
	{
		when(dataService.findOne(FREEMARKER_TEMPLATE, new QueryImpl<FreemarkerTemplate>().eq("Name", "template1"),
				FreemarkerTemplate.class)).thenReturn(template1, template1Modified);
		Object source = repositoryTemplateLoader.findTemplateSource("template1");
		assertTrue(IOUtils.contentEquals(repositoryTemplateLoader.getReader(source, null),
				new StringReader(template1.getValue())));
		Object modifiedSource = repositoryTemplateLoader.findTemplateSource("template1");
		assertNotEquals(source, modifiedSource);
		assertTrue(IOUtils.contentEquals(repositoryTemplateLoader.getReader(modifiedSource, null),
				new StringReader(template1Modified.getValue())));
	}

	@Test
	public void sourceBelongsToContentAndCanBeReadMultipleTimes() throws IOException
	{
		when(dataService.findOne(FREEMARKER_TEMPLATE, new QueryImpl<FreemarkerTemplate>().eq("Name", "template1"),
				FreemarkerTemplate.class)).thenReturn(template1);
		when(dataService.findOne(FREEMARKER_TEMPLATE, new QueryImpl<FreemarkerTemplate>().eq("Name", "template2"),
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
