package org.molgenis.rdconnect;

import static java.util.Objects.requireNonNull;
import static org.molgenis.rdconnect.IdCardBiobankIndexerController.URI;

import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.util.ErrorMessageResponse;
import org.molgenis.util.ErrorMessageResponse.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(URI)
public class IdCardBiobankIndexerController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(IdCardBiobankIndexerController.class);

	public static final String ID = "idcardbiobankindexer";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final IdCardBiobankService biobankMetadataService;
	private final IdCardBiobankRepository idCardBiobankRepository;

	@Autowired
	public IdCardBiobankIndexerController(IdCardBiobankService idCardBiobankService,
			IdCardBiobankRepository idCardBiobankRepository)
	{
		super(URI);
		this.biobankMetadataService = requireNonNull(idCardBiobankService);
		this.idCardBiobankRepository = requireNonNull(idCardBiobankRepository);
	}

	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	public String init(Model model) throws Exception
	{
		return "view-idcardbiobankindexer";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/reindex")
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@ResponseStatus(HttpStatus.OK)
	public void refreshMetadata(Model model) throws Exception
	{
		idCardBiobankRepository.index(biobankMetadataService.getIdCardBiobanks());
	}

	@ExceptionHandler(value = Throwable.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorMessageResponse handleThrowable(Throwable t)
	{
		LOG.error("", t);
		return new ErrorMessageResponse(new ErrorMessage(t.getMessage()));
	}
}
