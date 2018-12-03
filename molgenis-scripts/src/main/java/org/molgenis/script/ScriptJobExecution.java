package org.molgenis.script;

import static org.molgenis.script.ScriptJobExecutionMetadata.NAME;
import static org.molgenis.script.ScriptJobExecutionMetadata.PARAMETERS;
import static org.molgenis.script.ScriptJobExecutionMetadata.SCRIPT_JOB_TYPE;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;

public class ScriptJobExecution extends JobExecution {
  public ScriptJobExecution(Entity entity) {
    super(entity);
    setType(SCRIPT_JOB_TYPE);
  }

  public ScriptJobExecution(EntityType entityType) {
    super(entityType);
    setType(SCRIPT_JOB_TYPE);
  }

  public ScriptJobExecution(String identifier, EntityType entityType) {
    super(identifier, entityType);
    setType(SCRIPT_JOB_TYPE);
  }

  public void setName(String name) {
    set(NAME, name);
  }

  public String getName() {
    return getString(NAME);
  }

  public String getParameters() {
    return getString(PARAMETERS);
  }

  public void setParameters(String parameters) {
    set(PARAMETERS, parameters);
  }
}
