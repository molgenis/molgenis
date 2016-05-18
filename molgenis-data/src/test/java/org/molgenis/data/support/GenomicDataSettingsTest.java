package org.molgenis.data.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.settings.SettingsEntityMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

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
		EntityMetaData emd = new EntityMetaDataImpl("settings_genomicdata");
		emd.addAttribute(new AttributeMetaData("start"));
		emd.addAttribute(new AttributeMetaData("chromosome"));
		Entity entity = new DefaultEntity(emd, dataService);
		entity.set("start", "start,POS,startpos");
		entity.set("chromosome", "chromosome,#CHROM,CHROM");

		when(dataService.getEntityMetaData("settings_genomicdata")).thenReturn(emd);
		when(dataService.findOneById("settings_genomicdata", "settings_genomicdata")).thenReturn(entity);

		EntityMetaData entityMetaData = new EntityMetaDataImpl("entity");
		AttributeMetaData posAttributeMetaData = new AttributeMetaData("POS");
		AttributeMetaData chromAttributeMetaData = new AttributeMetaData("#CHROM");
		AttributeMetaData compoundAttributeMetaData = new AttributeMetaData("compound",
				MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
		compoundAttributeMetaData.addAttributePart(chromAttributeMetaData);
		entityMetaData.addAttribute(posAttributeMetaData);
		entityMetaData.addAttribute(compoundAttributeMetaData);

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
