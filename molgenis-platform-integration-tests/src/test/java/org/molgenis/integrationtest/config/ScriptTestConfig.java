package org.molgenis.integrationtest.config;

import org.molgenis.script.core.ScriptPackage;
import org.molgenis.script.core.ScriptTypeMetaData;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ ScriptTypeMetaData.class, ScriptPackage.class })
public class ScriptTestConfig
{
}
