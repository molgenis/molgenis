package org.molgenis.gaf;

public interface GafListSettings
{
	String getEntityName();

	String getExample(String columnName);

	String getRegExpPattern(String columnName);
}
