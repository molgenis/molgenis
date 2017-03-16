package org.molgenis.script.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.script.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, ScriptMetaData.class, ScriptTypeMetaData.class, ScriptParameterMetaData.class,
		ScriptFactory.class, ScriptTypeFactory.class, ScriptParameterFactory.class, ScriptPackage.class })
public class ScriptTestConfig
{
}
