package org.molgenis.data.support;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

//@ContextConfiguration(classes =
//{ GenomicDataSettings.class, GenomicDataSettingsTest.Config.class })
public class GenomicDataSettingsTest extends AbstractTestNGSpringContextTests
{
	//
	//	@Autowired
	//	GenomicDataSettings settings;
	//
	//	@Autowired
	//	DataService dataService;
	//
	//	// regressiontest for: #3542 genomebrowser does not show if the CHROM and/or POS field are inside a compound.
	//	@Test
	//	public void getAttributeMetadataForAttributeNameArray()
	//	{
	//		EntityMetaData emd = new EntityMetaData("settings_genomicdata");
	//		emd.addAttribute(new AttributeMetaData("start"));
	//		emd.addAttribute(new AttributeMetaData("chromosome"));
	//		Entity entity = new DefaultEntity(emd, dataService);
	//		entity.set("start", "start,POS,startpos");
	//		entity.set("chromosome", "chromosome,#CHROM,CHROM");
	//
	//		when(dataService.getEntityMetaData("settings_genomicdata")).thenReturn(emd);
	//		when(dataService.findOneById("settings_genomicdata", "settings_genomicdata")).thenReturn(entity);
	//
	//		EntityMetaData entityMetaData = new EntityMetaData("entity");
	//		AttributeMetaData posAttributeMetaData = new AttributeMetaData("POS");
	//		AttributeMetaData chromAttributeMetaData = new AttributeMetaData("#CHROM");
	//		AttributeMetaData compoundAttributeMetaData = new AttributeMetaData("compound",
	//				MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
	//		compoundAttributeMetaData.addAttributePart(chromAttributeMetaData);
	//		entityMetaData.addAttribute(posAttributeMetaData);
	//		entityMetaData.addAttribute(compoundAttributeMetaData);
	//
	//		assertEquals(settings.getAttributeMetadataForAttributeNameArray("start", entityMetaData), posAttributeMetaData);
	//		assertEquals(settings.getAttributeMetadataForAttributeNameArray("chromosome", entityMetaData),
	//				chromAttributeMetaData);
	//	}
	//
	//	@Configuration
	//	public static class Config
	//	{
	//		@Bean
	//		public DataService dataService()
	//		{
	//			return mock(DataService.class);
	//		}
	//
	//		@Bean
	//		public SettingsEntityMeta settingsEntityMeta()
	//		{
	//			return mock(SettingsEntityMeta.class);
	//		}
	//
	//	}
}
