package org.molgenis.integrationtest.script;

import org.molgenis.script.ScriptPackage;
import org.molgenis.script.ScriptTypeMetaData;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ ScriptTypeMetaData.class, ScriptPackage.class })
public class ScriptTestConfig
{
}
