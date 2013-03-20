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

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.server.MolgenisContext;
import org.molgenis.framework.server.MolgenisService;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.server.services.MolgenisGuiService;
import org.molgenis.framework.ui.ApplicationController;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.EmailService;
import org.molgenis.util.SimpleEmailService;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

public class GuiService extends MolgenisGuiService implements MolgenisService
{
	private static final Logger logger = Logger.getLogger(GuiService.class);
	
	private static final String KEY_APP_NAME = "app.name";

	public GuiService(MolgenisContext mc)
	{
		super(mc);
	}

	@Override
	public ApplicationController createUserInterface()
	{
		ApplicationController app = null;
		try {
			final Database dbForController = super.db;
			app = new ApplicationController(mc)
			{
				private static final long serialVersionUID = 1L;
							
				@Override
				public Database getDatabase()
				{
					return dbForController;
				}
			};

			String appLabel;
			try
			{
				MolgenisSettings molgenisSettings = ApplicationContextProvider.getApplicationContext().getBean(MolgenisSettings.class);
				appLabel = molgenisSettings.getProperty(KEY_APP_NAME, "${model.label}");
			} 
			catch(NoSuchBeanDefinitionException e)
			{
				logger.warn(e);
				appLabel = "${model.label}";
			}
			app.getModel().setLabel(appLabel);		
			app.getModel().setVersion("${version}");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		<#if mail_smtp_user != '' && mail_smtp_au != ''>
		EmailService service = new SimpleEmailService();
		service.setSmtpFromAddress("${mail_smtp_from}");	
		service.setSmtpProtocol("${mail_smtp_protocol}");
		service.setSmtpHostname("${mail_smtp_hostname}");
		service.setSmtpPort(${mail_smtp_port});
		service.setSmtpUser("${mail_smtp_user}");
		service.setSmtpAu("${mail_smtp_au}");	
		app.setEmailService(service);</#if>
		
		<#list model.userinterface.children as subscreen>
			<#assign screentype = Name(subscreen.getType().toString()?lower_case) />
		new ${package}.ui.${JavaName(subscreen)}${screentype}<#if screentype == "Form">Controller</#if>(app);
		</#list>
		return app;
	}
}
