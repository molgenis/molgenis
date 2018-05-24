package org.molgenis.semanticmapper.controller;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_FirstAttributeMappingInfo.class)
public abstract class FirstAttributeMappingInfo
{
	public abstract String getMappingProjectId();

	public abstract String getTarget();

	public abstract String getSource();

	public abstract String getTargetAttribute();

	public static FirstAttributeMappingInfo create(String mappingProjectId, String target, String source,
			String targetAttribute)
	{
		return new AutoValue_FirstAttributeMappingInfo(mappingProjectId, target, source, targetAttribute);
	}
}
