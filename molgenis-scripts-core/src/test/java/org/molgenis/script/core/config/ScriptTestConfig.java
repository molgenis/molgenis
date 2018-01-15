package org.molgenis.script.core.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.script.core.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, ScriptMetaData.class, ScriptTypeMetaData.class, ScriptParameterMetaData.class,
		ScriptFactory.class, ScriptTypeFactory.class, ScriptParameterFactory.class, ScriptPackage.class })
public class ScriptTestConfig
{
}
