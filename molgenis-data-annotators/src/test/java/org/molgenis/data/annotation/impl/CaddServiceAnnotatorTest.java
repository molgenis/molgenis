package org.molgenis.data.annotation.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.testng.annotations.BeforeMethod;

public class CaddServiceAnnotatorTest
{
	private AttributeMetaData attributeMetaDataCanAnnotate;
	private EntityMetaData metaDataCanAnnotate;
	private CaddServiceAnnotator annotator;
	
	@BeforeMethod
	public void beforeMethod(){
		annotator = new CaddServiceAnnotator();
		
		metaDataCanAnnotate = mock(EntityMetaData.class);
		attributeMetaDataCanAnnotate = mock(AttributeMetaData.class);
		when(attributeMetaDataCanAnnotate.getName()).thenReturn("chr", "pos", "ref", "alt");
		when(attributeMetaDataCanAnnotate.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));
	}
}
