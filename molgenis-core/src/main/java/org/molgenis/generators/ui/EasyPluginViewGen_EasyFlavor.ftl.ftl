<#--#####################################################################-->
<#--                                                                   ##-->
<#--         START OF THE OUTPUT                                       ##-->
<#--                                                                   ##-->
<#--#####################################################################-->
<#--Date:        ${date}
 * Template:	${template}
 * generator:   ${generator} ${version}
 * 
 * THIS FILE IS A TEMPLATE. PLEASE EDIT :-)
-->

package ${package};

import org.molgenis.framework.ui.EasyPluginView;
import org.molgenis.framework.ui.html.*;

public class ${clazzName}View extends EasyPluginView<${clazzName}Model>
{

	public ${clazzName}View(${clazzName}Model model)
	{
		super(model);
	}

	@Override
	public HtmlRenderer getInputs(${clazzName}Model model)
	{
		MolgenisForm f = new MolgenisForm(model);
		
		//we use d.getValue() to render the dates in a nicer formatting.
		DateInput d = new DateInput("date",model.date);
		
		f.add(new Paragraph("desc","currently selected date: "+d.getValue()));
		f.add(new LabelInput("label","Change date: "+d.getValue()));
		f.add(new DateInput("date", model.date));
		f.add(new ActionInput("updateDate","Update date"));
		
		return f;
	}
}