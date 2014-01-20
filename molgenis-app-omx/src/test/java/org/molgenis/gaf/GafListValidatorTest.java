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
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.gaf.GafListValidator.GafListValidationReport;
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

	private Repository<Entity> repository;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUp()
	{
		reset(dataService);
		reset(molgenisSettings);
		repository = mock(Repository.class);

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
						"^([^<>@+0-9_,]+ <[a-zA-Z0-9_\\\\.]+@[a-zA-Z0-9_\\\\.]+>, )*[^<>@+0-9_,]+ <[a-zA-Z0-9_\\\\.]+@[a-zA-Z0-9_\\\\.]+>$");
		when(
				molgenisSettings.getProperty(GafListValidator.GAF_LIST_SETTINGS_PREFIX
						+ GafListValidator.COL_SEQUENCING_START_DATE)).thenReturn("^[0-9]{6}$");
		when(molgenisSettings.getProperty(GafListValidator.GAF_LIST_SETTINGS_PREFIX + GafListValidator.COL_RUN))
				.thenReturn("^[0-9]{4}$");
		when(molgenisSettings.getProperty(GafListValidator.GAF_LIST_SETTINGS_PREFIX + GafListValidator.COL_FLOWCELL))
				.thenReturn("^(([AB][A-Z0-9]{7}XX)|(A[A-Z0-9]{4}))$");
		when(molgenisSettings.getProperty(GafListValidator.GAF_LIST_SETTINGS_PREFIX + GafListValidator.COL_LANE))
				.thenReturn("^[1-8](,[1-8])*$");
		when(
				molgenisSettings.getProperty(GafListValidator.GAF_LIST_SETTINGS_PREFIX
						+ GafListValidator.COL_BARCODE_MENU)).thenReturn(
				"^(None)|(((GAF)|(RPI)|(AGI)|(MON)|(RTP)|(HP8))\\\\s[0-9]{2}\\\\s([ACGT]{6})([ATCG]{2})?)$");
		when(molgenisSettings.getProperty(GafListValidator.GAF_LIST_SETTINGS_PREFIX + GafListValidator.COL_ARRAY_FILE))
				.thenReturn("^[a-z]+//:[a-zA-Z0-9][a-zA-Z0-9\\\\.\\\\-_]+/[a-zA-Z0-9\\\\._]+$");
		when(molgenisSettings.getProperty(GafListValidator.GAF_LIST_SETTINGS_PREFIX + GafListValidator.COL_ARRAY_ID))
				.thenReturn("^[1-9][0-9]*$");
		when(
				molgenisSettings.getProperty(GafListValidator.GAF_LIST_SETTINGS_PREFIX
						+ GafListValidator.COL_DATA_SHIPPED_DATE)).thenReturn("^[0-9]{8}$");
		when(
				molgenisSettings.getProperty(GafListValidator.GAF_LIST_SETTINGS_PREFIX
						+ GafListValidator.COL_DATA_SHIPPED_TO))
				.thenReturn(
						"^([^<>@+0-9_,]+( <[a-zA-Z0-9_\\\\.]+@[a-zA-Z0-9_\\\\.]+>)?, )*[^<>@+0-9_,]+( <[a-zA-Z0-9_\\\\.]+@[a-zA-Z0-9_\\\\.]+>)?$");

		Query q = new QueryImpl().eq(ObservableFeature.IDENTIFIER, GafListValidator.COL_LAB_STATUS_PHASE);
		ObservableFeature feature = mock(ObservableFeature.class);
		when(dataService.findOne(ObservableFeature.ENTITY_NAME, q)).thenReturn(feature);
		Category category1 = mock(Category.class);
		when(category1.getValueCode()).thenReturn(GafListValidator.LAB_STATUS_PHASE_FINISHED_SUCCESSFULLY);
		when(dataService.findAll(Category.ENTITY_NAME, new QueryImpl().eq(Category.OBSERVABLEFEATURE, feature)))
				.thenReturn(Arrays.<Entity> asList(category1));

	}

	@Test
	public void validate_internalSampleId() throws IOException
	{
		MapEntity entity0 = new MapEntity();
		entity0.set(GafListValidator.COL_LAB_STATUS_PHASE, GafListValidator.LAB_STATUS_PHASE_FINISHED_SUCCESSFULLY);
		entity0.set(GafListValidator.COL_INTERNAL_SAMPLE_ID, "123");
		when(repository.getAttributes()).thenReturn(
				Arrays.<AttributeMetaData> asList(new DefaultAttributeMetaData(GafListValidator.COL_LAB_STATUS_PHASE,
						FieldTypeEnum.CATEGORICAL), new DefaultAttributeMetaData(
						GafListValidator.COL_INTERNAL_SAMPLE_ID, FieldTypeEnum.STRING)));
		when(repository.iterator()).thenReturn(Arrays.<Entity> asList(entity0).iterator());
		GafListValidationReport report = gafListValidator.validate(repository);
		assertFalse(report.hasErrors());
	}

	@Test
	public void validate_internalSampleId_invalid() throws IOException
	{
		MapEntity entity0 = new MapEntity();
		entity0.set(GafListValidator.COL_LAB_STATUS_PHASE, GafListValidator.LAB_STATUS_PHASE_FINISHED_SUCCESSFULLY);
		entity0.set(GafListValidator.COL_INTERNAL_SAMPLE_ID, "abc");
		when(repository.getAttributes()).thenReturn(
				Arrays.<AttributeMetaData> asList(new DefaultAttributeMetaData(GafListValidator.COL_LAB_STATUS_PHASE,
						FieldTypeEnum.CATEGORICAL), new DefaultAttributeMetaData(
						GafListValidator.COL_INTERNAL_SAMPLE_ID, FieldTypeEnum.STRING)));
		when(repository.iterator()).thenReturn(Arrays.<Entity> asList(entity0).iterator());
		GafListValidationReport report = gafListValidator.validate(repository);
		assertTrue(report.hasErrors());
	}

	@Test
	public void validate_externalSampleId() throws IOException
	{
		MapEntity entity0 = new MapEntity();
		entity0.set(GafListValidator.COL_LAB_STATUS_PHASE, GafListValidator.LAB_STATUS_PHASE_FINISHED_SUCCESSFULLY);
		entity0.set(GafListValidator.COL_EXTERNAL_SAMPLE_ID, "123");
		when(repository.getAttributes()).thenReturn(
				Arrays.<AttributeMetaData> asList(new DefaultAttributeMetaData(GafListValidator.COL_LAB_STATUS_PHASE,
						FieldTypeEnum.CATEGORICAL), new DefaultAttributeMetaData(
						GafListValidator.COL_EXTERNAL_SAMPLE_ID, FieldTypeEnum.STRING)));
		when(repository.iterator()).thenReturn(Arrays.<Entity> asList(entity0).iterator());
		GafListValidationReport report = gafListValidator.validate(repository);
		assertFalse(report.hasErrors());
	}

	@Test
	public void validate_externalSampleId_invalid() throws IOException
	{
		MapEntity entity0 = new MapEntity();
		entity0.set(GafListValidator.COL_LAB_STATUS_PHASE, GafListValidator.LAB_STATUS_PHASE_FINISHED_SUCCESSFULLY);
		entity0.set(GafListValidator.COL_EXTERNAL_SAMPLE_ID, "+++");
		when(repository.getAttributes()).thenReturn(
				Arrays.<AttributeMetaData> asList(new DefaultAttributeMetaData(GafListValidator.COL_LAB_STATUS_PHASE,
						FieldTypeEnum.CATEGORICAL), new DefaultAttributeMetaData(
						GafListValidator.COL_EXTERNAL_SAMPLE_ID, FieldTypeEnum.STRING)));
		when(repository.iterator()).thenReturn(Arrays.<Entity> asList(entity0).iterator());
		GafListValidationReport report = gafListValidator.validate(repository);
		assertTrue(report.hasErrors());
	}

	@Test
	public void validate_project() throws IOException
	{
		MapEntity entity0 = new MapEntity();
		entity0.set(GafListValidator.COL_LAB_STATUS_PHASE, GafListValidator.LAB_STATUS_PHASE_FINISHED_SUCCESSFULLY);
		entity0.set(GafListValidator.COL_PROJECT, "abc");
		when(repository.getAttributes()).thenReturn(
				Arrays.<AttributeMetaData> asList(new DefaultAttributeMetaData(GafListValidator.COL_LAB_STATUS_PHASE,
						FieldTypeEnum.CATEGORICAL), new DefaultAttributeMetaData(GafListValidator.COL_PROJECT,
						FieldTypeEnum.STRING)));
		when(repository.iterator()).thenReturn(Arrays.<Entity> asList(entity0).iterator());
		GafListValidationReport report = gafListValidator.validate(repository);
		assertFalse(report.hasErrors());
	}

	@Test
	public void validate_project_invalid() throws IOException
	{
		MapEntity entity0 = new MapEntity();
		entity0.set(GafListValidator.COL_LAB_STATUS_PHASE, GafListValidator.LAB_STATUS_PHASE_FINISHED_SUCCESSFULLY);
		entity0.set(GafListValidator.COL_PROJECT, "+++");
		when(repository.getAttributes()).thenReturn(
				Arrays.<AttributeMetaData> asList(new DefaultAttributeMetaData(GafListValidator.COL_LAB_STATUS_PHASE,
						FieldTypeEnum.CATEGORICAL), new DefaultAttributeMetaData(GafListValidator.COL_PROJECT,
						FieldTypeEnum.STRING)));
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
