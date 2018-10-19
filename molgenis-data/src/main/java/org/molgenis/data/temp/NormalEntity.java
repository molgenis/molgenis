package org.molgenis.data.temp;

import org.molgenis.data.support.StaticEntity;

public class NormalEntity extends StaticEntity {

  private String id;

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }
}
