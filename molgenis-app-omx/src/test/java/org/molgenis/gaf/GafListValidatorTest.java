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

	@BeforeMethod
	public void setUp()
	{
		reset(dataService);
		reset(molgenisSettings);

		when(
				molgenisSettings.getProperty(GafListValidator.GAF_LIST_SETTINGS_PREFIX
						+ GafListValidator.COL_INTERNAL_SAMPLE_ID)).thenReturn("^[0-9]+$");
		when(
				molgenisSettings.getProperty(GafListValidator.GAF_LIST_SETTINGS_PREFIX
						+ GafListValidator.COL_EXTERNAL_SAMPLE_ID)).thenReturn("^[a-zA-Z0-9_]+$");
		when(molgenisSettings.getProperty(GafListValidator.GAF_LIST_SETTINGS_PREFIX + GafListValidator.COL_PROJECT))
				.thenReturn("^[a-zA-Z0-9_]+$");
		when(molgenisSettings.getProperty(GafListValidator.GAF_LIST_SETTINGS_PREFIX + GafListValidator.COL_SEQUENCER))
				.thenReturn("^[a-zA-Z0-9_]+$");
		when(molgenisSettings.getProperty(GafListValidator.GAF_LIST_SETTINGS_PREFIX + GafListValidator.COL_CONTACT))
				.thenReturn(
						"^([^<>@+0-9_,]+ <[a-zA-Z0-9_\\.]+@[a-zA-Z0-9_\\.]+>, )*[^<>@+0-9_,]+ <[a-zA-Z0-9_\\.]+@[a-zA-Z0-9_\\.]+>$");
		when(
				molgenisSettings.getProperty(GafListValidator.GAF_LIST_SETTINGS_PREFIX
						+ GafListValidator.COL_SEQUENCING_START_DATE)).thenReturn("^[0-9]{6}$");
		when(molgenisSettings.getProperty(GafListValidator.GAF_LIST_SETTINGS_PREFIX + GafListValidator.COL_RUN))
				.thenReturn("^[0-9]{4}$");
		when(molgenisSettings.getProperty(GafListValidator.GAF_LIST_SETTINGS_PREFIX + GafListValidator.COL_FLOWCELL))
				.thenReturn("^(([AB][A-Z0-9]{7}XX)|(A[A-Z0-9]{4}))$");
		when(molgenisSettings.getProperty(GafListValidator.GAF_LIST_SETTINGS_PREFIX + GafListValidator.COL_LANE))
				.thenReturn("^[1-8](,[1-8])*$");

		when(molgenisSettings.getProperty(GafListValidator.GAF_LIST_SETTINGS_PREFIX + GafListValidator.COL_BARCODE_1))
				.thenReturn("^(None)|(((GAF)|(RPI)|(AGI)|(MON)|(RTP)|(HP8))\\s[0-9]{2}\\s([ACGT]{6})([ATCG]{2})?)$");

		when(molgenisSettings.getProperty(GafListValidator.GAF_LIST_SETTINGS_PREFIX + GafListValidator.COL_ARRAY_FILE))
				.thenReturn("^.*[\\/\\\\]{1}[a-zA-Z0-9\\._]+$");
		when(molgenisSettings.getProperty(GafListValidator.GAF_LIST_SETTINGS_PREFIX + GafListValidator.COL_ARRAY_ID))
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
		GafListValidationReport report = gafListValidator.validate(repository);
		assertFalse(report.hasErrors());
	}

	@Test
	public void validate_internalSampleId_invalid() throws IOException
	{
		invalidTest(GafListValidator.COL_INTERNAL_SAMPLE_ID, "1+");
	}

	@Test
	public void validate_externalSampleId_invalid() throws IOException
	{
		invalidTest(GafListValidator.COL_EXTERNAL_SAMPLE_ID, "1aA_+");
	}

	@Test
	public void validate_project_invalid() throws IOException
	{
		invalidTest(GafListValidator.COL_PROJECT, "1aA_+");
	}

	private Repository getDefaultValidSettingRepositoryMock()
	{
		Repository repository = mock(Repository.class);
		EntityMetaData entityMetaData = mock(EntityMetaData.class);

		when(entityMetaData.getAttributes()).thenReturn(
				Arrays.<AttributeMetaData> asList(new DefaultAttributeMetaData(GafListValidator.COL_INTERNAL_SAMPLE_ID,
						FieldTypeEnum.STRING), new DefaultAttributeMetaData(GafListValidator.COL_EXTERNAL_SAMPLE_ID,
						FieldTypeEnum.STRING), new DefaultAttributeMetaData(GafListValidator.COL_PROJECT,
						FieldTypeEnum.STRING), new DefaultAttributeMetaData(GafListValidator.COL_BARCODE_1,
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
		entity0.set(GafListValidator.COL_INTERNAL_SAMPLE_ID, "1");
		entity0.set(GafListValidator.COL_EXTERNAL_SAMPLE_ID, "1aA_");
		entity0.set(GafListValidator.COL_PROJECT, "1aA_");
		entity0.set(GafListValidator.COL_BARCODE_1, "RPI 12 GCTAATCA");
		return entity0;
	}
	
	private void invalidTest(String nameColumn, String value) throws IOException
	{
		Repository repository = this.getDefaultValidSettingRepositoryMock();
		MapEntity entity0 = getDefaultValidMapEntityMock();

		entity0.set(nameColumn, value);

		when(repository.iterator()).thenReturn(Arrays.<Entity> asList(entity0).iterator());
		GafListValidationReport report = gafListValidator.validate(repository);
		assertTrue(report.hasErrors());
	}

	@Configuration
	public static class Config
	{
		@Bean
		public GafListValidator gafListValidator()
		{
			return new GafListValidator();
		}

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
	}
}
