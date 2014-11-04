package org.molgenis.data.jpa;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.INT;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.MREF;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.STRING;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.XREF;

import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public class PersonMetaData extends DefaultEntityMetaData
{
	public PersonMetaData()
	{
		super("Person", Person.class);

		DefaultAttributeMetaData id = new DefaultAttributeMetaData("id", STRING);
		id.setDescription("automatically generated internal id, only for internal use.");
		id.setIdAttribute(true);
		id.setNillable(false);
		id.setReadOnly(true);
		id.setAuto(true);
		addAttributeMetaData(id);

		DefaultAttributeMetaData firstName = new DefaultAttributeMetaData("firstName", STRING);
		firstName.setDescription("firstName");
		firstName.setIdAttribute(false);
		firstName.setNillable(false);
		firstName.setReadOnly(false);
		firstName.setLabelAttribute(true);
		firstName.setUnique(true);
		firstName.setAggregateable(true);
		addAttributeMetaData(firstName);

		DefaultAttributeMetaData lastName = new DefaultAttributeMetaData("lastName", STRING);
		lastName.setDescription("lastName");
		lastName.setIdAttribute(false);
		lastName.setNillable(false);
		lastName.setReadOnly(false);
		addAttributeMetaData(lastName);

		DefaultAttributeMetaData age = new DefaultAttributeMetaData("age", INT);
		age.setDescription("age");
		age.setIdAttribute(false);
		age.setNillable(true);
		age.setReadOnly(false);
		age.setAggregateable(true);
		addAttributeMetaData(age);

		DefaultAttributeMetaData father = new DefaultAttributeMetaData("father", XREF);
		father.setDescription("father");
		father.setIdAttribute(false);
		father.setNillable(true);
		father.setReadOnly(false);
		father.setRefEntity(this);
		addAttributeMetaData(father);

		DefaultAttributeMetaData children = new DefaultAttributeMetaData("children", MREF);
		children.setDescription("children");
		children.setIdAttribute(false);
		children.setNillable(true);
		children.setReadOnly(false);
		children.setRefEntity(this);
		addAttributeMetaData(children);
	}

}
