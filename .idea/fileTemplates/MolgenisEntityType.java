## Generates boilerplate for an EntityType. Creates the Metadata, Factory and implementation classes in one file. 

## Pretty form variable names
#set( $ENTITY_TYPE_NAME = $EntityType_name)
#set( $PACK_NAME = $Package_name)

## Define regex patterns to convert camel cased class/variable names
#set( $regex = "([a-z])([A-Z]+)" )
#set( $underscore = "$1_$2" )
#set( $space = "$1 $2" )

## Create class names
#set( $META_CLASS_NAME = $ENTITY_TYPE_NAME + "Metadata" )
#set( $FACTORY_CLASS_NAME = $ENTITY_TYPE_NAME + "Factory" )

## Create package fully qualified-, class- and variable names 
#set( $PACKAGE_FQN_VAR = "PACKAGE_" +  $PACK_NAME.replaceAll($regex, $underscore).toUpperCase())
#set( $PACKAGE_CLASS = $PACK_NAME + "Package" )
#set( $PACKAGE_VAR = $PACKAGE_CLASS.substring(0,1).toLowerCase() + $PACKAGE_CLASS.substring(1) )

## Create other variables
#set( $FQN_VAR = $ENTITY_TYPE_NAME.replaceAll($regex, $underscore).toUpperCase() )
#set( $LABEL = $ENTITY_TYPE_NAME.replaceAll($regex, $space) )
#set( $META_CLASS_VAR = $META_CLASS_NAME.substring(0,1).toLowerCase() + $META_CLASS_NAME.substring(1) )

########################################################################################################

package $PACKAGE_NAME;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;
import org.springframework.stereotype.Component;
import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static ${PACKAGE_NAME}.${META_CLASS_NAME}.ID;
import static ${PACKAGE_NAME}.${PACKAGE_CLASS}.$PACKAGE_FQN_VAR;

@Component
public class $META_CLASS_NAME extends SystemEntityType
{
	private static final String SIMPLE_NAME = "$ENTITY_TYPE_NAME";
	public static final String $FQN_VAR = $PACKAGE_FQN_VAR + PACKAGE_SEPARATOR + SIMPLE_NAME;

    public static final String ID = "id";

	private final $PACKAGE_CLASS $PACKAGE_VAR;

	public $META_CLASS_NAME($PACKAGE_CLASS $PACKAGE_VAR)
	{
		super(SIMPLE_NAME, $PACKAGE_FQN_VAR);
		this.$PACKAGE_VAR = requireNonNull($PACKAGE_VAR);
	}

	@Override
	public void init()
	{
		setPackage($PACKAGE_VAR);
		
		setLabel("$LABEL");
		//TODO setDescription("");
		
		addAttribute(ID, ROLE_ID).setLabel("Identifier");
	}
}

public class $ENTITY_TYPE_NAME extends StaticEntity
{
	public $ENTITY_TYPE_NAME(Entity entity)
	{
		super(entity);
	}

	public $ENTITY_TYPE_NAME(EntityType entityType)
	{
		super(entityType);
	}

	public $ENTITY_TYPE_NAME(String id, EntityType entityType)
	{
		super(entityType);
		setId(id);
	}

	public void setId(String id)
	{
		set(ID, id);
	}

	public String getId()
	{
		return getString(ID);
	}
}
    
@Component
public class $FACTORY_CLASS_NAME extends AbstractSystemEntityFactory<$ENTITY_TYPE_NAME, $META_CLASS_NAME, String>
{
	$FACTORY_CLASS_NAME($META_CLASS_NAME $META_CLASS_VAR, EntityPopulator entityPopulator)
	{
		super(${ENTITY_TYPE_NAME}.class, $META_CLASS_VAR, entityPopulator);
	}
}