package org.molgenis.ui.freemarker;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Maps;

import freemarker.core.Environment;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.StringModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class FormLinkDirectiveTest
{
	private FormLinkDirective directive;
	private StringWriter envWriter;
	private Template fakeTemplate;

	@BeforeMethod
	public void setUp()
	{
		directive = new FormLinkDirective();
		envWriter = new StringWriter();
		fakeTemplate = Template
				.getPlainTextTemplate("name", "content", new Configuration(Configuration.VERSION_2_3_21));
	}

	@Test
	public void execute() throws TemplateException, IOException
	{
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setAttribute("javax.servlet.forward.request_uri", "dataexplorer");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		StringModel person = new StringModel(new Person(), BeansWrapper.getDefaultInstance());

		Map<String, Object> params = Maps.newHashMap();
		params.put("entity", person);
		params.put("class", "class1 class2");

		directive.execute(new Environment(fakeTemplate, null, envWriter), params, new TemplateModel[0],
				new TemplateDirectiveBody()
				{
					@Override
					public void render(Writer out) throws TemplateException, IOException
					{
						out.write("form");
					}

				});

		assertEquals(envWriter.toString(),
				"<a href='/menu/entities/form.Person/99?back=dataexplorer' class='class1 class2' >form</a>");
	}

	private static class Person extends MapEntity
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Object getIdValue()
		{
			return 99;
		}

		@Override
		public EntityMetaData getEntityMetaData()
		{
			return new DefaultEntityMetaData("Person", MapEntity.class);
		}
	}
}
