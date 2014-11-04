package org.molgenis.ui.freemarker;

import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.Map;

import org.molgenis.data.DataConverter;
import org.molgenis.data.Entity;
import org.molgenis.ui.MolgenisUiUtils;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;

public class FormLinkDirective implements TemplateDirectiveModel
{
	@Override
	public void execute(Environment env, @SuppressWarnings("rawtypes") Map params, TemplateModel[] loopVars,
			TemplateDirectiveBody body) throws TemplateException, IOException
	{
		if (!params.containsKey("entity")) throw new TemplateModelException("Missing 'entity' param");

		Entity entity = (Entity) DeepUnwrap.unwrap((TemplateModel) params.get("entity"));
		String cssClass = DataConverter.toString(params.get("class"));
		String formUri = String.format("/menu/entities/form.%s/%d?back=%s", entity.getEntityMetaData().getName(),
				entity.getIdValue(), URLEncoder.encode(MolgenisUiUtils.getCurrentUri(), "UTF-8"));

		Writer w = env.getOut();
		w.write("<a href='" + formUri + "'");
		if (cssClass != null)
		{
			w.write(" class='" + cssClass + "'");
		}
		w.write(" >");
		body.render(w);
		w.write("</a>");
	}
}
