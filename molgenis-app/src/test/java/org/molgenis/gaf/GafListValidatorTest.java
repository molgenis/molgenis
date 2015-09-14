package org.molgenis.gaf;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.file.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes =
{ GafListValidatorTest.Config.class })
public class GafListValidatorTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private GafListValidator gafListValidator;

	@Autowired
	private DataService dataService;

	@Autowired
	private GafListSettings gafListSettings;

	@Autowired
	private GafListValidationReport report;

	private final List<String> columns = Arrays.<String> asList(GAFCol.INTERNAL_SAMPLE_ID.toString(),
			GAFCol.LANE.toString(), GAFCol.SEQUENCER.toString(), GAFCol.EXTERNAL_SAMPLE_ID.toString(),
			GAFCol.PROJECT.toString(), GAFCol.BARCODE_1.toString());

	@BeforeMethod
	public void setUp() throws Exception
	{
		report.cleanUp();
		reset(dataService);
		reset(gafListSettings);

		when(gafListSettings.getRegExpPattern(GAFCol.INTERNAL_SAMPLE_ID.toString())).thenReturn("^[0-9]+$");
		when(gafListSettings.getRegExpPattern(GAFCol.EXTERNAL_SAMPLE_ID.toString())).thenReturn("^[a-zA-Z0-9_]+$");
		when(gafListSettings.getRegExpPattern(GAFCol.PROJECT.toString())).thenReturn("^[a-zA-Z0-9_]+$");
		when(gafListSettings.getRegExpPattern(GAFCol.SEQUENCER.toString())).thenReturn("^[a-zA-Z0-9_]+$");
		when(gafListSettings.getRegExpPattern(GAFCol.CONTACT.toString()))
				.thenReturn(
						"^([^<>@+0-9_,]+ <[a-zA-Z0-9_\\.]+@[a-zA-Z0-9_\\.]+>, )*[^<>@+0-9_,]+ <[a-zA-Z0-9_\\.]+@[a-zA-Z0-9_\\.]+>$");
		when(gafListSettings.getRegExpPattern(GAFCol.SEQUENCING_START_DATE.toString())).thenReturn("^[0-9]{6}$");
		when(gafListSettings.getRegExpPattern(GAFCol.RUN.toString())).thenReturn("^[0-9]{4}$");
		when(gafListSettings.getRegExpPattern(GAFCol.FLOWCELL.toString())).thenReturn(
				"^(([AB][A-Z0-9]{7}XX)|(A[A-Z0-9]{4}))$");
		when(gafListSettings.getRegExpPattern(GAFCol.LANE.toString())).thenReturn("^[1-8](,[1-8])*$");

		when(gafListSettings.getRegExpPattern(GAFCol.BARCODE_1.toString())).thenReturn(
				"^(None)|(((GAF)|(RPI)|(AGI)|(MON)|(RTP)|(HP8))\\s[0-9]{2}\\s([ACGT]{6})([ATCG]{2})?)$");

		when(gafListSettings.getRegExpPattern(GAFCol.ARRAY_FILE.toString())).thenReturn(
				"^.*[\\/\\\\]{1}[a-zA-Z0-9\\._]+$");
		when(gafListSettings.getRegExpPattern(GAFCol.ARRAY_ID.toString())).thenReturn("^[1-9][0-9]*$");
	}

	@Test
	public void valid() throws IOException
	{
		Repository repository = this.getDefaultValidSettingRepositoryMock();
		MapEntity entity0 = getDefaultValidMapEntityMock();
		when(repository.iterator()).thenReturn(Arrays.<Entity> asList(entity0).iterator());
		gafListValidator.validate(report, repository, columns);
		assertFalse(report.hasRunIdsErrors());
	}

	@Test
	public void internalSampleId_invalid() throws IOException
	{
		invalidTest(GAFCol.INTERNAL_SAMPLE_ID.toString(), "1+");
	}

	@Test
	public void lane_invalid() throws IOException
	{
		invalidTest(GAFCol.LANE.toString(), "1,9");
	}

	@Test
	public void externalSampleId_invalid() throws IOException
	{
		invalidTest(GAFCol.EXTERNAL_SAMPLE_ID.toString(), "1aA_+");
	}

	@Test
	public void project_invalid() throws IOException
	{
		invalidTest(GAFCol.PROJECT.toString(), "1aA_+");
	}

	private Repository getDefaultValidSettingRepositoryMock()
	{
		Repository repository = mock(Repository.class);
		EntityMetaData entityMetaData = mock(EntityMetaData.class);

		// INTERNAL_SAMPLE_ID
		DefaultAttributeMetaData internalSampleID = new DefaultAttributeMetaData(GAFCol.INTERNAL_SAMPLE_ID.toString(),
				FieldTypeEnum.INT);

		// LANE
		DefaultAttributeMetaData lane = new DefaultAttributeMetaData(GAFCol.LANE.toString(), FieldTypeEnum.STRING);

		// SEQUENCER
		DefaultAttributeMetaData sequencer = new DefaultAttributeMetaData(GAFCol.SEQUENCER.toString(),
				FieldTypeEnum.ENUM);
		sequencer.setEnumOptions(Arrays.<String> asList("HWUSI_EAS536", "SN163", "M01785"));

		// EXTERNAL_SAMPLE_ID
		DefaultAttributeMetaData externalSampleId = new DefaultAttributeMetaData(GAFCol.EXTERNAL_SAMPLE_ID.toString(),
				FieldTypeEnum.STRING);

		// PROJECT
		DefaultAttributeMetaData project = new DefaultAttributeMetaData(GAFCol.PROJECT.toString(), FieldTypeEnum.STRING);

		// BARCODE_1
		DefaultAttributeMetaData barcode1 = new DefaultAttributeMetaData(GAFCol.BARCODE_1.toString(),
				FieldTypeEnum.STRING);

		when(entityMetaData.getAttributes()).thenReturn(
				Arrays.<AttributeMetaData> asList(internalSampleID, lane, sequencer, externalSampleId, project,
						barcode1));

		when(entityMetaData.getAttribute(GAFCol.INTERNAL_SAMPLE_ID.toString())).thenReturn(internalSampleID);
		when(entityMetaData.getAttribute(GAFCol.LANE.toString())).thenReturn(lane);
		when(entityMetaData.getAttribute(GAFCol.SEQUENCER.toString())).thenReturn(sequencer);
		when(entityMetaData.getAttribute(GAFCol.EXTERNAL_SAMPLE_ID.toString())).thenReturn(externalSampleId);
		when(entityMetaData.getAttribute(GAFCol.PROJECT.toString())).thenReturn(project);
		when(entityMetaData.getAttribute(GAFCol.BARCODE_1.toString())).thenReturn(barcode1);
		when(repository.getEntityMetaData()).thenReturn(entityMetaData);
		when(gafListSettings.getEntityName()).thenReturn("gaflist_gaflist_20141111");

		when(dataService.getEntityMetaData("gaflist_gaflist_20141111")).thenReturn(entityMetaData);

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
		entity0.set(GAFCol.LANE.toString(), "8");
		entity0.set(GAFCol.SEQUENCER.toString(), "SN163");
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
		gafListValidator.validate(report, repository, columns);
		assertTrue(report.hasRunIdsErrors());
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
		public MetaDataService metaDataService()
		{
			return mock(MetaDataService.class);
		}

		@Bean
		public GafListSettings gafListSettings()
		{
			return mock(GafListSettings.class);
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
