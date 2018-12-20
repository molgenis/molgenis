package org.molgenis.ontology.core.meta;

import static java.lang.Boolean.TRUE;
import static org.molgenis.ontology.core.meta.OntologyTermNodePathMetaData.ID;
import static org.molgenis.ontology.core.meta.OntologyTermNodePathMetaData.NODE_PATH;
import static org.molgenis.ontology.core.meta.OntologyTermNodePathMetaData.ROOT;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

public class OntologyTermNodePath extends StaticEntity {
  public OntologyTermNodePath(Entity entity) {
    super(entity);
  }

  public OntologyTermNodePath(EntityType entityType) {
    super(entityType);
  }

  public OntologyTermNodePath(String id, EntityType entityType) {
    super(entityType);
    setId(id);
  }

  public String getId() {
    return getString(ID);
  }

  public void setId(String id) {
    set(ID, id);
  }

  public String getNodePath() {
    return getString(NODE_PATH);
  }

  public void setNodePath(String nodePath) {
    set(NODE_PATH, nodePath);
  }

  public boolean isRoot() {
    Boolean isRoot = getBoolean(ROOT);
    return TRUE.equals(isRoot);
  }

  public void setRoot(boolean root) {
    set(ROOT, root);
  }
}
