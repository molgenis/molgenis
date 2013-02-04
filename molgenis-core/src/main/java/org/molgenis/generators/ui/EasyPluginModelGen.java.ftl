<#include "GeneratorHelper.ftl">
<#--#####################################################################-->
<#--                                                                   ##-->
<#--         START OF THE OUTPUT                                       ##-->
<#--                                                                   ##-->
<#--#####################################################################-->
/* Date:        ${date}
 * Template:	${template}
 * generator:   ${generator} ${version}
 * 
 * THIS FILE IS A TEMPLATE. PLEASE EDIT :-)
 */

package ${package};

import java.util.Date;

import org.molgenis.framework.ui.EasyPluginModel;

/**
 * ${clazzName}Model takes care of all state and it can have helper methods to query the database.
 * It should not contain layout or application logic which are solved in View and Controller.
 * @See org.molgenis.framework.ui.ScreenController for available services.
 */
public class ${clazzName}Model extends EasyPluginModel
{
	//a system veriable that is needed by tomcat
	private static final long serialVersionUID = 1L;
	//this string can be referenced from ${clazzName}View.ftl template as <#noparse>${model.helloWorld}</#noparse>
	public String helloWorld = "hello World";
	//this date can be referenced from ${clazzName}View.ftl template as <#noparse>${model.date}</#noparse>
	public Date date = new Date();
	
	//another example, you can also use getInvestigations() and setInvestigations(...)
	//public List<Investigation> investigations = new ArrayList<Investigation>();

	public ${clazzName}Model(${clazzName} controller)
	{
		//each Model can access the controller to notify it when needed.
		super(controller);
	}
	
	public Date getDate() {
		return date;
	}
}
