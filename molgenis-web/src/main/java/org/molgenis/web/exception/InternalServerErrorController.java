package org.molgenis.web.exception;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.molgenis.web.exception.InternalServerErrorController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping(value = URI)
public class InternalServerErrorController
{
	public static final String ERROR_MESSAGE = "Something went wrong.";

	public static final String URI = "/500";

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		model.addAttribute("error_message", ERROR_MESSAGE);
		return "view-error";
	}
}
