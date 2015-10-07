package org.molgenis.rdconnect;

import static java.util.Objects.requireNonNull;
import static org.molgenis.rdconnect.IdCardBiobankIndexerController.URI;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
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

	private final DataService dataService;
	private final IdCardBiobankService biobankMetadataService;

	@Autowired
	public IdCardBiobankIndexerController(DataService dataService, IdCardBiobankService biobankMetadataService)
	{
		super(URI);
		this.biobankMetadataService = requireNonNull(biobankMetadataService);
		this.dataService = requireNonNull(dataService);
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
		Map<String, HashSet<MapEntity>> firsToAdd = new HashMap<String, HashSet<MapEntity>>();

		StreamSupport.stream(biobankMetadataService.getIdCardBiobanks().spliterator(), false)
				.forEach(e -> populateLists(firsToAdd, e));

		dataService.add("rdconnect_regbb", biobankMetadataService.getIdCardBiobanks());
	}

	private void populateLists(Map<String, HashSet<MapEntity>> lists, Entity entity)
	{
		lists.entrySet().forEach(e -> e.getValue().addAll((Collection<? extends MapEntity>) entity.get(e.getKey())));
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
