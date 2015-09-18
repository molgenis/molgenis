package org.molgenis.data.mapper.controller;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.mapping.model.MappingProject;
import org.molgenis.data.mapper.mapping.model.MappingTarget;
import org.molgenis.data.mapper.service.AlgorithmService;
import org.molgenis.data.mapper.service.MappingService;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.util.GsonConfig;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration(classes = GsonConfig.class)
public class MappingServiceControllerTest extends AbstractTestNGSpringContextTests
{
	@InjectMocks
	private MappingServiceController controller;

	@Mock
	private MolgenisUserService molgenisUserService;

	@Mock
	private MappingService mappingService;

	@Mock
	private AlgorithmService algorithmService;

	@Mock
	private DataService dataService;

	@Mock
	private OntologyTagService ontologyTagService;

	@Mock
	private SemanticSearchService semanticSearchService;

	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	private MolgenisUser me = new MolgenisUser();
	private DefaultEntityMetaData lifeLines;
	private DefaultEntityMetaData hop;
	private MappingProject mappingProject;

	private MockMvc mockMvc;

	@BeforeMethod
	public void beforeTest()
	{
		me.setUsername("fdlk");
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("fdlk", null);
		authentication.setAuthenticated(true);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		hop = new DefaultEntityMetaData("HOP");
		hop.addAttributeMetaData(new DefaultAttributeMetaData("age", FieldTypeEnum.INT));
		lifeLines = new DefaultEntityMetaData("LifeLines");
		hop.addAttributeMetaData(new DefaultAttributeMetaData("dob", FieldTypeEnum.DATE));

		mappingProject = new MappingProject("hop hop hop", me);
		mappingProject.setIdentifier("asdf");
		MappingTarget mappingTarget = mappingProject.addTarget(hop);
		EntityMapping entityMapping = mappingTarget.addSource(lifeLines);
		AttributeMapping attributeMapping = entityMapping.addAttributeMapping("age");
		attributeMapping.setAlgorithm("$('dob').age()");

		initMocks(this);

		mockMvc = MockMvcBuilders.standaloneSetup(controller).setMessageConverters(gsonHttpMessageConverter).build();
	}

	@Test
	public void itShouldUpdateExistingAttributeMappingWhenSaving() throws Exception
	{
		when(mappingService.getMappingProject("asdf")).thenReturn(mappingProject);

		mockMvc.perform(MockMvcRequestBuilders.post(MappingServiceController.URI + "/saveattributemapping")
				.param("mappingProjectId", "asdf").param("target", "HOP").param("source", "LifeLines")
				.param("targetAttribute", "age").param("algorithm", "$('length').value()"))
				.andExpect(MockMvcResultMatchers.redirectedUrl("/menu/main/mappingservice/mappingproject/asdf"));
		MappingProject expected = new MappingProject("hop hop hop", me);
		expected.setIdentifier("asdf");
		MappingTarget mappingTarget = expected.addTarget(hop);
		EntityMapping entityMapping = mappingTarget.addSource(lifeLines);
		AttributeMapping ageMapping = entityMapping.addAttributeMapping("age");
		ageMapping.setAlgorithm("$('length').value()");

		Mockito.verify(mappingService).updateMappingProject(expected);
	}

	@Test
	public void itShouldCreateNewAttributeMappingWhenSavingIfNonePresent() throws Exception
	{
		when(mappingService.getMappingProject("asdf")).thenReturn(mappingProject);

		mockMvc.perform(MockMvcRequestBuilders.post(MappingServiceController.URI + "/saveattributemapping")
				.param("mappingProjectId", "asdf").param("target", "HOP").param("source", "LifeLines")
				.param("targetAttribute", "height").param("algorithm", "$('length').value()"))
				.andExpect(MockMvcResultMatchers.redirectedUrl("/menu/main/mappingservice/mappingproject/asdf"));

		MappingProject expected = new MappingProject("hop hop hop", me);
		expected.setIdentifier("asdf");
		MappingTarget mappingTarget = expected.addTarget(hop);
		EntityMapping entityMapping = mappingTarget.addSource(lifeLines);
		AttributeMapping ageMapping = entityMapping.addAttributeMapping("age");
		ageMapping.setAlgorithm("$('dob').age()");
		AttributeMapping heightMapping = entityMapping.addAttributeMapping("height");
		heightMapping.setAlgorithm("$('length').value()");

		Mockito.verify(mappingService).updateMappingProject(expected);
	}

	@Test
	public void itShouldRemoveEmptyAttributeMappingsWhenSaving() throws Exception
	{
		when(mappingService.getMappingProject("asdf")).thenReturn(mappingProject);

		mockMvc.perform(MockMvcRequestBuilders.post(MappingServiceController.URI + "/saveattributemapping")
				.param("mappingProjectId", "asdf").param("target", "HOP").param("source", "LifeLines")
				.param("targetAttribute", "age").param("algorithm", ""))
				.andExpect(MockMvcResultMatchers.redirectedUrl("/menu/main/mappingservice/mappingproject/asdf"));

		MappingProject expected = new MappingProject("hop hop hop", me);
		expected.setIdentifier("asdf");
		MappingTarget mappingTarget = expected.addTarget(hop);
		mappingTarget.addSource(lifeLines);
		Mockito.verify(mappingService).updateMappingProject(expected);
	}

}
