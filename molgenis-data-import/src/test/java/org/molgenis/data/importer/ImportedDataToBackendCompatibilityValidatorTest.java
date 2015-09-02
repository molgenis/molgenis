package org.molgenis.data.importer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

@ContextConfiguration(classes =
{ ImportedDataToBackendCompatibilityValidatorTest.Config.class })
public class ImportedDataToBackendCompatibilityValidatorTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private DataService dataService;
	private ImportedDataToBackendCompatibilityValidator validator;

	@BeforeMethod
	void setup()
	{
		validator = new ImportedDataToBackendCompatibilityValidator(dataService);
	}

	@Test
	public void validateEntityPassTest()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("test");
		validator.validate(ImmutableList.of(emd));

		when(dataService.hasRepository("test")).thenReturn(false);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "")
	public void validateEntityFailTest()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("Test");
		validator.validate(ImmutableList.of(emd));

		when(dataService.hasRepository("Test")).thenReturn(true);
	}
	
	@Configuration
	public static class Config
	{
		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}
	}
}
