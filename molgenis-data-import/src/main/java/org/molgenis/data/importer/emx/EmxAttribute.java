package org.molgenis.data.importer.emx;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.meta.model.Attribute;

class EmxAttribute {
  private final Attribute attr;
  private boolean idAttr;
  private boolean labelAttr;
  private boolean lookupAttr;

  EmxAttribute(Attribute attr) {
    this.attr = requireNonNull(attr);
  }

  Attribute getAttr() {
    return attr;
  }

  boolean isIdAttr() {
    return idAttr;
  }

  void setIdAttr(boolean idAttr) {
    this.idAttr = idAttr;
  }

  boolean isLabelAttr() {
    return labelAttr;
  }

  void setLabelAttr(boolean labelAttr) {
    this.labelAttr = labelAttr;
  }

  boolean isLookupAttr() {
    return lookupAttr;
  }

  void setLookupAttr(boolean lookupAttr) {
    this.lookupAttr = lookupAttr;
  }
}
