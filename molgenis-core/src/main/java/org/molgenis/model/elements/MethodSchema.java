//

package org.molgenis.model.elements;

// imports
import org.molgenis.util.SimpleTree;

/**
 * A method-schema describes a number of methods, which can be exposed with
 * something like webservices. This class acts as the schema-collection for
 * single methods.
 */
public class MethodSchema extends SimpleTree<MethodSchema>
{
	public MethodSchema(String name, MethodSchema parent)
	{
		super(name, parent);
	}

	private static final long serialVersionUID = 2513991729164752297L;
}
