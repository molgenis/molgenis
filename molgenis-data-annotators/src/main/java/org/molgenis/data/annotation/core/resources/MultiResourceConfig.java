package org.molgenis.data.annotation.core.resources;

import java.util.Map;

/**
 * Created by charbonb on 15/06/15.
 */
public interface MultiResourceConfig
{
	Map<String, ResourceConfig> getConfigs();
}
