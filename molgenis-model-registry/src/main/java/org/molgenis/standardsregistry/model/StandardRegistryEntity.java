package org.molgenis.standardsregistry.model;

/**
 *
 *
 * @author sido
 */
public class StandardRegistryEntity {

    private String name;
    private String label;
    private boolean abstr;

    public StandardRegistryEntity(String name, String label, boolean abstr)
    {
      super();
      this.name = name;
      this.label = label;
      this.abstr = abstr;
    }

    public String getName()
        {
            return name;
        }

    @SuppressWarnings("unused")
    public String getLabel()
        {
            return label;
        }

    public boolean isAbtract()
        {
            return abstr;
        }
}
