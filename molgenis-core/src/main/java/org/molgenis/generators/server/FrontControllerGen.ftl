<#setting number_format="#"/>
<#include "GeneratorHelper.ftl">
<#--#####################################################################-->
<#--                                                                   ##-->
<#--         START OF THE OUTPUT                                       ##-->
<#--                                                                   ##-->
<#--#####################################################################-->
/*
 * Created by: ${generator}
 * Date: ${date}
 */

package ${package}.servlet;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.molgenis.framework.server.MolgenisContext;
import org.molgenis.framework.server.MolgenisFrontController;
import org.molgenis.framework.server.MolgenisService;

public class FrontController extends MolgenisFrontController
{
	private static final long serialVersionUID = 3141439968743510237L;
	
	@Override
	public void init(javax.servlet.ServletConfig conf) throws javax.servlet.ServletException
	{
		//save options so they can be passed to superclass
		this.usedOptions = new UsedMolgenisOptions();
	
		//create fresh logger based on MolgenisOptions
		createLogger();
		logger = Logger.getLogger(FrontController.class);
		
		//first, we initialize so the ServletContext is created from the webserver
		super.init(conf);
		
		//now we can create the MolgenisContext with objects reusable over many requests
		context = new MolgenisContext(this.getServletConfig(), new UsedMolgenisOptions(), "${model.name}");
		
		//finally, we store all mapped services, and pass them the context used for databasing, serving, etc.
		LinkedHashMap<String,MolgenisService> services = new LinkedHashMap<String,MolgenisService>();
		
		try
		{
			<#list services as service>
			services.put("${service?split('@')[1]}", new ${service?split('@')[0]}(context));
			</#list>
		}
		catch(Exception e)
		{
			logger.fatal("failure in starting services in FrontController. Check your services and/or mapping and try again.");
			throw new RuntimeException(e);
		}
		
		this.services = services;
	}
	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
	{
		super.service(request, response);
	}
}
