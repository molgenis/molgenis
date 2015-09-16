package org.molgenis.data.annotation.resources;

import java.util.Map;

/**
 * Created by charbonb on 15/06/15.
 */
public interface MultiResourceConfig
{
	public Map<String, ResourceConfig> getConfigs();
}
