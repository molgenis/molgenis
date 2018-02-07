#set( $EXCEPTION_CLASS_NAME = $Exception_name + "Exception")
#set( $EXTENDS_FROM = $Extends_from)

package $PACKAGE_NAME;

import org.molgenis.i18n.CodedRuntimeException;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
* TODO
*/
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false positives at dev time
@SuppressWarnings("squid:MaximumInheritanceDepth", "squid:S2166")
public class $EXCEPTION_CLASS_NAME extends $EXTENDS_FROM
{
	private static final String ERROR_CODE = "X01"; //TODO

	private final String id;

	public $EXCEPTION_CLASS_NAME(String id)
	{
		super(ERROR_CODE);
		this.id = requireNonNull(id);
	}

	public $EXCEPTION_CLASS_NAME(String id, Throwable cause)
	{
		super(ERROR_CODE, cause);
		this.id = requireNonNull(id);
	}

	@Override
	public String getMessage()
	{
		return format("id:%s", id); //TODO
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { id }; //TODO
	}
}