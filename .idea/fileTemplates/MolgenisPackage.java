## Generates boilerplate for a SystemPackage.

## Pretty form variable name
#set( $PACK_NAME = $Package_name)

## Define regex patterns to convert camel cased class/variable names
#set( $regex = "([a-z])([A-Z]+)")
#set( $underscore = "$1_$2")
#set( $space = "$1 $2")

## Create class name and variables
#set( $CLASS_NAME = ${PACK_NAME} + "Package")
#set( $SIMPLE_NAME = $CLASS_NAME.substring(0,3).toLowerCase() )
#set( $FQN_VAR = "PACKAGE_" + $PACK_NAME.replaceAll($regex, $underscore).toUpperCase())
#set( $LABEL = $PACK_NAME.replaceAll($regex, $space))

########################################################################################################

package $PACKAGE_NAME;

import org.molgenis.data.meta.SystemPackage;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.system.model.RootSystemPackage;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class ${CLASS_NAME} extends SystemPackage
{
	private static final String SIMPLE_NAME = "${SIMPLE_NAME}";
	public static final String $FQN_VAR = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	private final RootSystemPackage rootSystemPackage;

	public ${CLASS_NAME}(PackageMetadata packageMetadata, RootSystemPackage rootSystemPackage)
	{
		super($FQN_VAR, packageMetadata);
		this.rootSystemPackage = requireNonNull(rootSystemPackage);
	}

	@Override
	protected void init()
	{
		setParent(rootSystemPackage);
		
		setLabel("$LABEL");
		//TODO setDescription("");
	}
}
