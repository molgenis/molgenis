package org.molgenis.data.annotator.tabix;

import org.molgenis.data.Entity;

public interface RowParser
{
	Entity toEntity(String line);
}
