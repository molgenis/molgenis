package org.molgenis.gaf;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.gaf.GafListValidatorTest.Config;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes =
{ Config.class })
public class GafListValidatorTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private GafListValidator gafListValidator;

	@Autowired
	private DataService dataService;

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	private GafListValidationReport report;

	@BeforeMethod
	public void setUp()
	{
		reset(dataService);
		reset(molgenisSettings);

		when(
				molgenisSettings.getProperty(GafListValidator.GAF_LIST_VALIDATOR_PREFIX
 + GAFCol.INTERNAL_SAMPLE_ID))
				.thenReturn("^[0-9]+$");
		when(
				molgenisSettings.getProperty(GafListValidator.GAF_LIST_VALIDATOR_PREFIX
 + GAFCol.EXTERNAL_SAMPLE_ID))
				.thenReturn("^[a-zA-Z0-9_]+$");
		when(molgenisSettings.getProperty(GafListValidator.GAF_LIST_VALIDATOR_PREFIX + GAFCol.PROJECT))
				.thenReturn("^[a-zA-Z0-9_]+$");
		when(molgenisSettings.getProperty(GafListValidator.GAF_LIST_VALIDATOR_PREFIX + GAFCol.SEQUENCER))
				.thenReturn("^[a-zA-Z0-9_]+$");
		when(molgenisSettings.getProperty(GafListValidator.GAF_LIST_VALIDATOR_PREFIX + GAFCol.CONTACT))
				.thenReturn(
						"^([^<>@+0-9_,]+ <[a-zA-Z0-9_\\.]+@[a-zA-Z0-9_\\.]+>, )*[^<>@+0-9_,]+ <[a-zA-Z0-9_\\.]+@[a-zA-Z0-9_\\.]+>$");
		when(
				molgenisSettings.getProperty(GafListValidator.GAF_LIST_VALIDATOR_PREFIX
 + GAFCol.SEQUENCING_START_DATE))
				.thenReturn("^[0-9]{6}$");
		when(molgenisSettings.getProperty(GafListValidator.GAF_LIST_VALIDATOR_PREFIX + GAFCol.RUN))
				.thenReturn("^[0-9]{4}$");
		when(molgenisSettings.getProperty(GafListValidator.GAF_LIST_VALIDATOR_PREFIX + GAFCol.FLOWCELL))
				.thenReturn("^(([AB][A-Z0-9]{7}XX)|(A[A-Z0-9]{4}))$");
		when(molgenisSettings.getProperty(GafListValidator.GAF_LIST_VALIDATOR_PREFIX + GAFCol.LANE))
				.thenReturn("^[1-8](,[1-8])*$");

		when(molgenisSettings.getProperty(GafListValidator.GAF_LIST_VALIDATOR_PREFIX + GAFCol.BARCODE_1))
				.thenReturn("^(None)|(((GAF)|(RPI)|(AGI)|(MON)|(RTP)|(HP8))\\s[0-9]{2}\\s([ACGT]{6})([ATCG]{2})?)$");

		when(molgenisSettings.getProperty(GafListValidator.GAF_LIST_VALIDATOR_PREFIX + GAFCol.ARRAY_FILE))
				.thenReturn("^.*[\\/\\\\]{1}[a-zA-Z0-9\\._]+$");
		when(molgenisSettings.getProperty(GafListValidator.GAF_LIST_VALIDATOR_PREFIX + GAFCol.ARRAY_ID))
				.thenReturn("^[1-9][0-9]*$");

		ObservableFeature feature = mock(ObservableFeature.class);
		Category category1 = mock(Category.class);
		when(
				dataService.findAll(Category.ENTITY_NAME, new QueryImpl().eq(Category.OBSERVABLEFEATURE, feature),
						Category.class)).thenReturn(Arrays.asList(category1));

	}

	@Test
	public void validate() throws IOException
	{
		Repository repository = this.getDefaultValidSettingRepositoryMock();
		MapEntity entity0 = getDefaultValidMapEntityMock();
		when(repository.iterator()).thenReturn(Arrays.<Entity> asList(entity0).iterator());
		gafListValidator.validate(report, repository);
		assertFalse(report.hasErrors());
	}

	@Test
	public void validate_internalSampleId_invalid() throws IOException
	{
		invalidTest(GAFCol.INTERNAL_SAMPLE_ID.toString(), "1+");
	}

	@Test
	public void validate_externalSampleId_invalid() throws IOException
	{
		invalidTest(GAFCol.EXTERNAL_SAMPLE_ID.toString(), "1aA_+");
	}

	@Test
	public void validate_project_invalid() throws IOException
	{
		invalidTest(GAFCol.PROJECT.toString(), "1aA_+");
	}

	private Repository getDefaultValidSettingRepositoryMock()
	{
		Repository repository = mock(Repository.class);
		EntityMetaData entityMetaData = mock(EntityMetaData.class);

		when(entityMetaData.getAttributes()).thenReturn(
				Arrays.<AttributeMetaData> asList(new DefaultAttributeMetaData(GAFCol.INTERNAL_SAMPLE_ID.toString(),
						FieldTypeEnum.STRING), new DefaultAttributeMetaData(GAFCol.EXTERNAL_SAMPLE_ID.toString(),
						FieldTypeEnum.STRING), new DefaultAttributeMetaData(GAFCol.PROJECT.toString(),
						FieldTypeEnum.STRING), new DefaultAttributeMetaData(GAFCol.BARCODE_1.toString(),
						FieldTypeEnum.STRING)));

		when(repository.getEntityMetaData()).thenReturn(entityMetaData);

		return repository;
	}
	
	/**
	 * returns a MapEntity with valid values
	 * 
	 * @return MapEntity
	 */
	private MapEntity getDefaultValidMapEntityMock()
	{
		MapEntity entity0 = new MapEntity();
		entity0.set(GAFCol.INTERNAL_SAMPLE_ID.toString(), "1");
		entity0.set(GAFCol.EXTERNAL_SAMPLE_ID.toString(), "1aA_");
		entity0.set(GAFCol.PROJECT.toString(), "1aA_");
		entity0.set(GAFCol.BARCODE_1.toString(), "RPI 12 GCTAATCA");
		return entity0;
	}
	
	private void invalidTest(String nameColumn, String value) throws IOException
	{
		Repository repository = this.getDefaultValidSettingRepositoryMock();
		MapEntity entity0 = getDefaultValidMapEntityMock();

		entity0.set(nameColumn, value);

		when(repository.iterator()).thenReturn(Arrays.<Entity> asList(entity0).iterator());
		gafListValidator.validate(report, repository);
		assertTrue(report.hasErrors());
	}

	@Configuration
	public static class Config
	{
		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public MolgenisSettings molgenisSettings()
		{
			return mock(MolgenisSettings.class);
		}

		@Bean
		public GafListValidator gafListValidator()
		{
			return new GafListValidator();
		}

		@Bean
		public GafListValidationReport report()
		{
			return new GafListValidationReport();
		}

		@Bean
		public FileStore fileStore()
		{
			return mock(FileStore.class);
		}
	}
}
