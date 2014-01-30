package org.molgenis.data;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.INT;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.STRING;

import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public class PersonMetaData extends DefaultEntityMetaData
{
	public PersonMetaData()
	{
		super("Person");

		DefaultAttributeMetaData id = new DefaultAttributeMetaData("id", INT);
		id.setDescription("automatically generated internal id, only for internal use.");
		id.setIdAttribute(true);
		id.setNillable(false);
		id.setReadOnly(true);
		
		this.addAttributeMetaData(id);
		
		DefaultAttributeMetaData firstName = new DefaultAttributeMetaData("firstName", STRING);
		firstName.setDescription("firstName");
		firstName.setIdAttribute(false);
		firstName.setNillable(false);
		firstName.setReadOnly(false);
		firstName.setLabelAttribute(true);
		
		this.addAttributeMetaData(firstName);
		
		DefaultAttributeMetaData lastName = new DefaultAttributeMetaData("lastName", STRING);
		lastName.setDescription("lastName");
		lastName.setIdAttribute(false);
		lastName.setNillable(false);
		lastName.setReadOnly(false);
		lastName.setLabelAttribute(true);
		
		this.addAttributeMetaData(lastName);
	}

	

}
