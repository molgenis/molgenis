package org.molgenis.fairpoint;

import java.io.StringWriter;
import java.util.Arrays;

import org.mockito.Mockito;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.fair.FairService;
import org.molgenis.fieldtypes.DateField;
import org.molgenis.fieldtypes.XrefField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.hp.hpl.jena.rdf.model.Model;

@ContextConfiguration(classes = FairServiceTest.Config.class)
public class FairServiceTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private MetaDataService metaDataService;

	@Autowired
	private FairService fairService;

	private DefaultEntityMetaData cities;

	private DefaultEntityMetaData persons;

	private DefaultEntityMetaData patients;

	@BeforeTest
	public void beforeTest()
	{
		cities = new DefaultEntityMetaData("cities");
		cities.setDescription("list of cities");
		cities.addAttribute("cityName").setNillable(false).setIdAttribute(true).setDescription("unique city name");

		persons = new DefaultEntityMetaData("persons");
		persons.setDescription("person defines general attributes like firstName, lastName");
		persons.setAbstract(true);
		persons.addAttribute("displayName").setNillable(false).setIdAttribute(true);
		persons.addAttribute("firstName");
		persons.addAttribute("lastName");

		patients = new DefaultEntityMetaData("patients");
		patients.setDescription("patient extends person, adding patientNumber");
		patients.setExtends(persons);
		patients.addAttribute("birthdate").setDataType(new DateField());
		patients.addAttribute("birthplace").setDataType(new XrefField()).setRefEntity(cities);
		patients.addAttribute("disease").setDescription("disease description");
	}

	@Test
	public void testGetProfile()
	{
		Mockito.when(metaDataService.getEntityMetaDatas()).thenReturn(Arrays.asList(cities, persons, patients));

		Model model = fairService.getProfile("http://localhost:8080/api/v1");
		StringWriter writer = new StringWriter();
		model.write(writer, "Turtle");

		System.out.println(writer);
	}

	@Configuration
	public static class Config
	{
		@Bean
		public MetaDataService metaDataService()
		{
			return Mockito.mock(MetaDataService.class);
		}

		@Bean
		public FairService fairportService()
		{
			return new FairService();
		}
	}
}
