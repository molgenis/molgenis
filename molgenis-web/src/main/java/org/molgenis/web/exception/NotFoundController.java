package org.molgenis.web.exception;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.molgenis.web.exception.NotFoundController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping(value = URI)
public class NotFoundController
{
	public static final String ERROR_MESSAGE = "The page you’re looking for can't be found.";

	public static final String URI = "/404";

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		model.addAttribute("error_message", ERROR_MESSAGE);
		return "view-error";
	}
}
