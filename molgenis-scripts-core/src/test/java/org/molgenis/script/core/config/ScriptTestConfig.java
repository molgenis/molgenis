package org.molgenis.script.core.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.script.core.ScriptFactory;
import org.molgenis.script.core.ScriptMetaData;
import org.molgenis.script.core.ScriptPackage;
import org.molgenis.script.core.ScriptParameterFactory;
import org.molgenis.script.core.ScriptParameterMetaData;
import org.molgenis.script.core.ScriptTypeFactory;
import org.molgenis.script.core.ScriptTypeMetaData;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  EntityBaseTestConfig.class,
  ScriptMetaData.class,
  ScriptTypeMetaData.class,
  ScriptParameterMetaData.class,
  ScriptFactory.class,
  ScriptTypeFactory.class,
  ScriptParameterFactory.class,
  ScriptPackage.class
})
public class ScriptTestConfig {}
