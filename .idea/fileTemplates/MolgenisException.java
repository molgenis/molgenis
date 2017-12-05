#set( $EXCEPTION_CLASS_NAME = $Exception_name + "Exception")

package $PACKAGE_NAME;

import org.molgenis.data.CodedRuntimeException;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class $EXCEPTION_CLASS_NAME extends CodedRuntimeException
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