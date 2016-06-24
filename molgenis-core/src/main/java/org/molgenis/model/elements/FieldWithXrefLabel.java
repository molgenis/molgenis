package org.molgenis.model.elements;

/**
 * Helper class to deal with xref_label fields.
 * 
 * @author Morris Swertz
 * 
 */
public class FieldWithXrefLabel extends Field
{
	public FieldWithXrefLabel(Field field)
	{
		super(field);
	}

	public synchronized String getXrefLabelPath()
	{
		return xref_label_path;
	}

	public synchronized void setXrefLabelPath(String xrefLabelPath)
	{
		xref_label_path = xrefLabelPath;
	}

	private static final long serialVersionUID = -3365294817917800934L;

	private String xref_label_path;

}
