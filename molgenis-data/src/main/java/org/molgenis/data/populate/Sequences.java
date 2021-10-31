package org.molgenis.data.populate;

import java.util.List;
import org.molgenis.data.meta.model.Attribute;

/** Sequences can be used to generate sequential IDs */
public interface Sequences {

  /** @return names of all available sequences */
  List<String> getSequences();

  /**
   * Sets the current value a sequence, creating it if it does not yet exist
   *
   * @param sequenceName the name of the sequence
   * @param value the value to set
   */
  void setValue(String sequenceName, long value);

  /**
   * Generates a new value for an id attribute, creating a sequence for it if one is not yet
   * available.
   *
   * @param attribute the Attribute to generate a new value for
   * @return the next value in the sequence
   */
  long generateId(Attribute attribute);
}
