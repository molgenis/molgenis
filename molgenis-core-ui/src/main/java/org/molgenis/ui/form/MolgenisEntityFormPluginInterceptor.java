package org.molgenis.ui.form;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.molgenis.ui.MolgenisPluginAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Puts the correct plugin_id in the model
 */
public class MolgenisEntityFormPluginInterceptor extends HandlerInterceptorAdapter
{
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception
	{
		Map<String, Object> model = modelAndView.getModel();
		EntityForm entityForm = (EntityForm) model.get(MolgenisEntityFormPluginController.ENTITY_FORM_MODEL_ATTRIBUTE);
		StringBuilder pluginId = new StringBuilder(MolgenisEntityFormPluginController.PLUGIN_NAME_PREFIX
				+ entityForm.getMetaData().getName());

		if (request.getParameter("subForms") != null)
		{
			String[] subForms = request.getParameterValues("subForms");
			for (int i = 0; i < subForms.length; i++)
			{
				if (i == 0)
				{
					pluginId.append("?");
				}
				else
				{
					pluginId.append("&");
				}
				pluginId.append("subForms=").append(subForms[i]);
			}
		}

		model.put(MolgenisPluginAttributes.KEY_PLUGIN_ID, pluginId.toString());
	}
}
