package org.molgenis.data.vcf.datastructures;

import java.util.Objects;
import org.molgenis.data.Entity;

public class Sample {
  String id;

  Entity genotype;

  public Sample() {
    super();
  }

  public Sample(String id) {
    super();
    this.id = id;
  }

  public Sample(String id, Entity genotype) {
    super();
    this.id = id;
    this.genotype = genotype;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Entity getGenotype() {
    return genotype;
  }

  public void setGenotype(Entity genotype) {
    this.genotype = genotype;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Sample)) {
      return false;
    }
    Sample sample = (Sample) o;
    return Objects.equals(getId(), sample.getId())
        && Objects.equals(getGenotype(), sample.getGenotype());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getGenotype());
  }

  @Override
  public String toString() {
    return "Sample [id=" + id + ", genotype=" + genotype + "]";
  }
}
