package org.molgenis.data.mysql;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.molgenis.AppConfig;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Range;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.fieldtypes.EnumField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes = AppConfig.class)
public class AttributeMetaDataRepositoryTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private AttributeMetaDataRepository attributeMetaDataRepository;

	@Autowired
	private EntityMetaDataRepository entityMetaDataRepository;

	@BeforeMethod
	public void beforeMethod()
	{
		attributeMetaDataRepository.deleteAll();
		entityMetaDataRepository.deleteAll();
	}

	@Test
	public void addAndGetAttributeMetaData()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("test");
		entityMetaDataRepository.addEntityMetaData(emd);

		List<String> enumOptions = Arrays.asList("enum1", "enum2");
		AttributeMetaData enumAttr = emd.addAttribute("enum").setDataType(new EnumField()).setEnumOptions(enumOptions);
		attributeMetaDataRepository.addAttributeMetaData(emd.getName(), enumAttr);

		AttributeMetaData intRangeAttr = emd.addAttribute("intrange").setDataType(MolgenisFieldTypes.INT)
				.setRange(new Range(1l, 5l));
		attributeMetaDataRepository.addAttributeMetaData(emd.getName(), intRangeAttr);

		List<DefaultAttributeMetaData> retrieved = attributeMetaDataRepository
				.getEntityAttributeMetaData(emd.getName());

		assertNotNull(retrieved);
		assertEquals(retrieved.size(), 2);

		assertEquals(retrieved.get(0).getName(), "enum");
		assertNotNull(retrieved.get(0).getDataType());
		assertEquals(retrieved.get(0).getDataType().getEnumType(), FieldTypeEnum.ENUM);
		assertEquals(retrieved.get(0).getEnumOptions(), enumOptions);

		assertEquals(retrieved.get(1).getName(), "intrange");
		assertNotNull(retrieved.get(1).getDataType());
		assertEquals(retrieved.get(1).getDataType().getEnumType(), FieldTypeEnum.INT);
		assertNotNull(retrieved.get(1).getRange());
		assertEquals(retrieved.get(1).getRange().getMin(), Long.valueOf(1l));
		assertEquals(retrieved.get(1).getRange().getMax(), Long.valueOf(5l));
	}

}
