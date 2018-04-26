package org.molgenis.data.annotation.config;

import org.molgenis.data.annotation.core.effects.EffectsMetaData;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, EffectsMetaData.class })
public class EffectsTestConfig
{
}
