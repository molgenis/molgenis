package org.molgenis.ui.form;

import org.molgenis.data.Entity;

public interface Form
{
	String getTitle();

	Integer getPrimaryKey();

	boolean getHasWritePermission();

	FormMetaData getMetaData();

	Entity getEntity();

	String getBaseUri(String contextUrl);

}
