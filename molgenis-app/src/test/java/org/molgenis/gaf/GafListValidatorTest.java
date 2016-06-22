package org.molgenis.gaf;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.ENUM;
import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.gaf.GAFCol.BARCODE_1;
import static org.molgenis.gaf.GAFCol.EXTERNAL_SAMPLE_ID;
import static org.molgenis.gaf.GAFCol.INTERNAL_SAMPLE_ID;
import static org.molgenis.gaf.GAFCol.LANE;
import static org.molgenis.gaf.GAFCol.PROJECT;
import static org.molgenis.gaf.GAFCol.SEQUENCER;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.file.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes = { GafListValidatorTest.Config.class })
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

	private final List<String> columns = Arrays
			.asList(INTERNAL_SAMPLE_ID.toString(), LANE.toString(), SEQUENCER.toString(), EXTERNAL_SAMPLE_ID.toString(),
					PROJECT.toString(), BARCODE_1.toString());

	@BeforeMethod
	public void setUp() throws Exception
	{
		report.cleanUp();
		reset(dataService);
		reset(gafListSettings);

		when(gafListSettings.getRegExpPattern(INTERNAL_SAMPLE_ID.toString())).thenReturn("^[0-9]+$");
		when(gafListSettings.getRegExpPattern(EXTERNAL_SAMPLE_ID.toString())).thenReturn("^[a-zA-Z0-9_]+$");
		when(gafListSettings.getRegExpPattern(PROJECT.toString())).thenReturn("^[a-zA-Z0-9_]+$");
		when(gafListSettings.getRegExpPattern(SEQUENCER.toString())).thenReturn("^[a-zA-Z0-9_]+$");
		when(gafListSettings.getRegExpPattern(GAFCol.CONTACT.toString())).thenReturn(
				"^([^<>@+0-9_,]+ <[a-zA-Z0-9_\\.]+@[a-zA-Z0-9_\\.]+>, )*[^<>@+0-9_,]+ <[a-zA-Z0-9_\\.]+@[a-zA-Z0-9_\\.]+>$");
		when(gafListSettings.getRegExpPattern(GAFCol.SEQUENCING_START_DATE.toString())).thenReturn("^[0-9]{6}$");
		when(gafListSettings.getRegExpPattern(GAFCol.RUN.toString())).thenReturn("^[0-9]{4}$");
		when(gafListSettings.getRegExpPattern(GAFCol.FLOWCELL.toString()))
				.thenReturn("^(([AB][A-Z0-9]{7}XX)|(A[A-Z0-9]{4}))$");
		when(gafListSettings.getRegExpPattern(LANE.toString())).thenReturn("^[1-8](,[1-8])*$");

		when(gafListSettings.getRegExpPattern(BARCODE_1.toString()))
				.thenReturn("^(None)|(((GAF)|(RPI)|(AGI)|(MON)|(RTP)|(HP8))\\s[0-9]{2}\\s([ACGT]{6})([ATCG]{2})?)$");

		when(gafListSettings.getRegExpPattern(GAFCol.ARRAY_FILE.toString()))
				.thenReturn("^.*[\\/\\\\]{1}[a-zA-Z0-9\\._]+$");
		when(gafListSettings.getRegExpPattern(GAFCol.ARRAY_ID.toString())).thenReturn("^[1-9][0-9]*$");
	}

	@Test
	public void valid() throws IOException
	{
		Repository<Entity> repository = this.getDefaultValidSettingRepositoryMock();
		Entity entity0 = getDefaultValidMapEntityMock();
		when(repository.iterator()).thenReturn(Collections.<Entity>singletonList(entity0).iterator());
		gafListValidator.validate(report, repository, columns);
		assertFalse(report.hasRunIdsErrors());
	}

	@Test
	public void internalSampleId_invalid() throws IOException
	{
		invalidTest(INTERNAL_SAMPLE_ID.toString(), "1+");
	}

	@Test
	public void lane_invalid() throws IOException
	{
		invalidTest(LANE.toString(), "1,9");
	}

	@Test
	public void externalSampleId_invalid() throws IOException
	{
		invalidTest(EXTERNAL_SAMPLE_ID.toString(), "1aA_+");
	}

	@Test
	public void project_invalid() throws IOException
	{
		invalidTest(PROJECT.toString(), "1aA_+");
	}

	private Repository<Entity> getDefaultValidSettingRepositoryMock()
	{
		Repository<Entity> repository = mock(Repository.class);
		EntityMetaData entityMetaData = mock(EntityMetaData.class);

		AttributeMetaDataMetaData attrMetaMeta = mock(AttributeMetaDataMetaData.class);

		// INTERNAL_SAMPLE_ID
		AttributeMetaData internalSampleID = new AttributeMetaData(attrMetaMeta).setName(INTERNAL_SAMPLE_ID.toString())
				.setDataType(INT);

		// LANE
		AttributeMetaData lane = new AttributeMetaData(attrMetaMeta).setName(LANE.toString());

		// SEQUENCER
		AttributeMetaData sequencer = new AttributeMetaData(attrMetaMeta).setName(SEQUENCER.toString())
				.setDataType(ENUM);
		sequencer.setEnumOptions(Arrays.asList("HWUSI_EAS536", "SN163", "M01785"));

		// EXTERNAL_SAMPLE_ID
		AttributeMetaData externalSampleId = new AttributeMetaData(attrMetaMeta).setName(EXTERNAL_SAMPLE_ID.toString());

		// PROJECT
		AttributeMetaData project = new AttributeMetaData(attrMetaMeta).setName(PROJECT.toString());

		// BARCODE_1
		AttributeMetaData barcode1 = new AttributeMetaData(attrMetaMeta).setName(BARCODE_1.toString());

		when(entityMetaData.getAttributes())
				.thenReturn(Arrays.asList(internalSampleID, lane, sequencer, externalSampleId, project, barcode1));

		when(entityMetaData.getAttribute(INTERNAL_SAMPLE_ID.toString())).thenReturn(internalSampleID);
		when(entityMetaData.getAttribute(LANE.toString())).thenReturn(lane);
		when(entityMetaData.getAttribute(SEQUENCER.toString())).thenReturn(sequencer);
		when(entityMetaData.getAttribute(EXTERNAL_SAMPLE_ID.toString())).thenReturn(externalSampleId);
		when(entityMetaData.getAttribute(PROJECT.toString())).thenReturn(project);
		when(entityMetaData.getAttribute(BARCODE_1.toString())).thenReturn(barcode1);
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
	private Entity getDefaultValidMapEntityMock()
	{
		DynamicEntity entity0 = new DynamicEntity(null); // FIXME pass entity meta data instead of null
		entity0.set(INTERNAL_SAMPLE_ID.toString(), "1");
		entity0.set(LANE.toString(), "8");
		entity0.set(SEQUENCER.toString(), "SN163");
		entity0.set(EXTERNAL_SAMPLE_ID.toString(), "1aA_");
		entity0.set(PROJECT.toString(), "1aA_");
		entity0.set(BARCODE_1.toString(), "RPI 12 GCTAATCA");
		return entity0;
	}

	private void invalidTest(String nameColumn, String value) throws IOException
	{
		Repository<Entity> repository = this.getDefaultValidSettingRepositoryMock();
		Entity entity0 = getDefaultValidMapEntityMock();

		entity0.set(nameColumn, value);

		when(repository.iterator()).thenReturn(Arrays.<Entity>asList(entity0).iterator());
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
