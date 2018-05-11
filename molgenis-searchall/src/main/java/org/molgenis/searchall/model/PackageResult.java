package org.molgenis.searchall.model;

import com.google.auto.value.AutoValue;
import org.molgenis.data.meta.model.Package;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_PackageResult.class)
public abstract class PackageResult implements Described
{
	public abstract String getId();

	public static PackageResult create(String id, String label, String description)
	{
		return new AutoValue_PackageResult(label, description, id);
	}

	public static PackageResult create(Package pack)
	{
		return PackageResult.create(pack.getId(), pack.getLabel(), pack.getDescription());
	}


}
