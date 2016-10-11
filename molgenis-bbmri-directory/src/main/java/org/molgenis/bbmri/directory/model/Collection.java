package org.molgenis.bbmri.directory.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Collection.class)
public abstract class Collection
{
	public abstract String getCollectionId();

	public abstract String getBiobankId();

	public static Collection createCollection(String collectionId, String biobankId)
	{
		return new AutoValue_Collection(collectionId, biobankId);
	}
}
