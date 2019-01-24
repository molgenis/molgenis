package org.molgenis.script.core.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.script.core.ScriptFactory;
import org.molgenis.script.core.ScriptMetadata;
import org.molgenis.script.core.ScriptPackage;
import org.molgenis.script.core.ScriptParameterFactory;
import org.molgenis.script.core.ScriptParameterMetadata;
import org.molgenis.script.core.ScriptTypeFactory;
import org.molgenis.script.core.ScriptTypeMetadata;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  EntityBaseTestConfig.class,
  ScriptMetadata.class,
  ScriptTypeMetadata.class,
  ScriptParameterMetadata.class,
  ScriptFactory.class,
  ScriptTypeFactory.class,
  ScriptParameterFactory.class,
  ScriptPackage.class
})
public class ScriptTestConfig {}
