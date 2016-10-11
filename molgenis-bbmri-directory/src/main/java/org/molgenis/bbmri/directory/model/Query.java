package org.molgenis.bbmri.directory.model;

import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class Query
{
	public abstract List<Collection> getCollections();

	public abstract Filter getFilters();
}
