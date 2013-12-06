package org.molgenis.ui.form;

import java.util.List;

import org.molgenis.data.AttributeMetaData;

public interface FormMetaData
{
	String getName();

	/**
	 * All non system fields without the primarykey field
	 * 
	 * @return
	 */
	List<AttributeMetaData> getFields();
}
