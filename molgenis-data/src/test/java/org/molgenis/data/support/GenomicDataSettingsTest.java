package org.molgenis.data.support;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.settings.SettingsEntityMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes =
{ GenomicDataSettings.class, GenomicDataSettingsTest.Config.class })
public class GenomicDataSettingsTest extends AbstractTestNGSpringContextTests
{

	@Autowired
	GenomicDataSettings settings;

	@Autowired
	DataService dataService;

	// regressiontest for: #3542 genomebrowser does not show if the CHROM and/or POS field are inside a compound.
	@Test
	public void getAttributeMetadataForAttributeNameArray()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("settings_genomicdata");
		emd.addAttributeMetaData(new DefaultAttributeMetaData("start"));
		emd.addAttributeMetaData(new DefaultAttributeMetaData("chromosome"));
		Entity entity = new DefaultEntity(emd, dataService);
		entity.set("start", "start,POS,startpos");
		entity.set("chromosome", "chromosome,#CHROM,CHROM");

		when(dataService.getEntityMetaData("settings_genomicdata")).thenReturn(emd);
		when(dataService.findOne("settings_genomicdata", "settings_genomicdata")).thenReturn(entity);

		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("entity");
		DefaultAttributeMetaData posAttributeMetaData = new DefaultAttributeMetaData("POS");
		DefaultAttributeMetaData chromAttributeMetaData = new DefaultAttributeMetaData("#CHROM");
		DefaultAttributeMetaData compoundAttributeMetaData = new DefaultAttributeMetaData("compound",
				MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
		compoundAttributeMetaData.addAttributePart(chromAttributeMetaData);
		entityMetaData.addAttributeMetaData(posAttributeMetaData);
		entityMetaData.addAttributeMetaData(compoundAttributeMetaData);

		assertEquals(settings.getAttributeMetadataForAttributeNameArray("start", entityMetaData), posAttributeMetaData);
		assertEquals(settings.getAttributeMetadataForAttributeNameArray("chromosome", entityMetaData),
				chromAttributeMetaData);
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
		public SettingsEntityMeta settingsEntityMeta()
		{
			return mock(SettingsEntityMeta.class);
		}

	}
}
