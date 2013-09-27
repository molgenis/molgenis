For using this module some steps are needed in your MOLGENIS app:
- add the das Servlet to your WebAppInitializer:

		Dynamic dasServlet = servletContext.addServlet("dasServlet", new uk.ac.ebi.mydas.controller.MydasServlet());
		if (dasServlet == null)
		{
			logger.warn("ServletContext already contains a complete ServletRegistration for servlet 'dasServlet'");
		}
		else
		{
			dasServlet.setLoadOnStartup(2);
			dasServlet.addMapping("/das/*");
		}

- permit all on /das/** in your MolgenisWebAppSecurityConfig
		.antMatchers("/das/**").permitAll()
		
for the example source:
- place the files in the /src/main/resources/MyDas in the webapp folder of your app
