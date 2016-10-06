package org.molgenis.data.mapper.service.impl;

import com.google.common.collect.Maps;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedMatchCandidate;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedQueryString;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.file.FileStore;
import org.molgenis.js.magma.JsMagmaScriptRunner;
import org.molgenis.script.*;
import org.molgenis.security.core.token.TokenService;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.script.ScriptMetaData.SCRIPT;
import static org.molgenis.script.ScriptMetaData.TYPE;
import static org.molgenis.script.ScriptParameterMetaData.SCRIPT_PARAMETER;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = AlgorithmTemplateServiceImplTest.Config.class)
public class AlgorithmTemplateServiceImplTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private ScriptFactory scriptFactory;

	@Autowired
	private ScriptParameterFactory scriptParameterFactory;

	@Autowired
	private EntityMetaDataFactory entityMetaFactory;

	@Autowired
	private AttributeMetaDataFactory attrMetaFactory;

	@Autowired
	private AlgorithmTemplateServiceImpl algorithmTemplateServiceImpl;

	@Autowired
	private DataService dataService;

	private Script script0;
	private String param0Name = "param0", param1Name = "param1";

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		ScriptParameter param0 = scriptParameterFactory.create();
		param0.setName(param0Name);

		ScriptParameter param1 = scriptParameterFactory.create();
		param1.setName(param1Name);

		script0 = scriptFactory.create();
		script0.setName("name");
		script0.setContent(String.format("$('%s'),$('%s')", param0, param1));
		script0.set(ScriptMetaData.PARAMETERS, Arrays.asList(param0, param1));

		Query<Script> q = new QueryImpl<Script>().eq(TYPE, JsMagmaScriptRunner.NAME);
		when(dataService.findAll(SCRIPT, q, Script.class)).thenReturn(Stream.of(script0));
		when(dataService.findOneById(SCRIPT_PARAMETER, param0Name)).thenReturn(param0);
		when(dataService.findOneById(SCRIPT_PARAMETER, param1Name)).thenReturn(param1);
	}

	@Test
	public void find()
	{
		String sourceAttr0Name = "sourceAttr0";
		String sourceAttr1Name = "sourceAttr1";
		EntityMetaData sourceEntityMeta = entityMetaFactory.create("source");
		AttributeMetaData sourceAttr0 = attrMetaFactory.create().setName(sourceAttr0Name);
		AttributeMetaData sourceAttr1 = attrMetaFactory.create().setName(sourceAttr1Name);
		sourceEntityMeta.addAttribute(sourceAttr0);
		sourceEntityMeta.addAttribute(sourceAttr1);
		ExplainedQueryString sourceAttr0Explain = ExplainedQueryString.create("b", param0Name, 1.0);
		ExplainedQueryString sourceAttr1Explain = ExplainedQueryString.create("b", param1Name, 0.5);
		Map<AttributeMetaData, ExplainedMatchCandidate<AttributeMetaData>> attrResults = Maps.newHashMap();
		attrResults.put(sourceAttr0,
				ExplainedMatchCandidate.create(sourceAttr0, singletonList(sourceAttr0Explain), false));
		attrResults.put(sourceAttr1,
				ExplainedMatchCandidate.create(sourceAttr1, singletonList(sourceAttr1Explain), false));

		Stream<AlgorithmTemplate> templateStream = algorithmTemplateServiceImpl.find(attrResults);

		Map<String, String> model = Maps.newHashMap();
		model.put(param0Name, sourceAttr0Name);
		model.put(param1Name, sourceAttr1Name);
		AlgorithmTemplate expectedAlgorithmTemplate = new AlgorithmTemplate(script0, model);
		assertEquals(templateStream.collect(Collectors.toList()),
				Stream.of(expectedAlgorithmTemplate).collect(Collectors.toList()));
	}

	@Configuration
	@ComponentScan({ "org.molgenis.script" })
	public static class Config
	{
		@Bean
		public FileStore fileStore()
		{
			return mock(FileStore.class);
		}

		@Bean
		public TokenService tokenService()
		{
			return mock(TokenService.class);
		}

		@Bean
		public AlgorithmTemplateServiceImpl algorithmTemplateServiceImpl()
		{
			return new AlgorithmTemplateServiceImpl(dataService());
		}

		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}
	}
}
