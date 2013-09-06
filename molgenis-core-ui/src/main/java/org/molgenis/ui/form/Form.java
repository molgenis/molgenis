package org.molgenis.ui.form;

public interface Form
{
	String getTitle();

	String getPrimaryKey();

	boolean getHasWritePermission();

	FormMetaData getMetaData();

	org.molgenis.util.Entity getEntity();
}
