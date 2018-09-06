package org.molgenis.core.ui.admin.usermanager;

import static org.molgenis.core.ui.admin.usermanager.UserManagerController.URI;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.molgenis.web.PluginController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Api("User manager")
@Controller
@RequestMapping(URI)
public class UserManagerController extends PluginController {
  public static final String URI = PluginController.PLUGIN_URI_PREFIX + "usermanager";
  private final UserManagerService pluginUserManagerService;

  public UserManagerController(UserManagerService pluginUserManagerService) {
    super(URI);
    if (pluginUserManagerService == null) {
      throw new IllegalArgumentException("PluginUserManagerService is null");
    }
    this.pluginUserManagerService = pluginUserManagerService;
  }

  @ApiOperation("Return user manager view")
  @ApiResponses({@ApiResponse(code = 200, message = "Return the user manager view")})
  @GetMapping
  public String init(Model model) {
    model.addAttribute("users", this.pluginUserManagerService.getAllUsers());

    return "view-usermanager";
  }

  @ApiOperation("Sets activation status for a user")
  @ApiResponses({
    @ApiResponse(code = 200, message = "Ok", response = ActivationResponse.class),
    @ApiResponse(
        code = 404,
        message = "If response doesn't have success set to true, the user wasn't found",
        response = ActivationResponse.class)
  })
  @PutMapping("/activation")
  @ResponseStatus(HttpStatus.OK)
  public @ResponseBody ActivationResponse activation(@RequestBody Activation activation) {
    ActivationResponse activationResponse = new ActivationResponse();
    activationResponse.setId(activation.getId());
    pluginUserManagerService.setActivationUser(activation.getId(), activation.getActive());
    activationResponse.setSuccess(true);
    return activationResponse;
  }

  public class ActivationResponse {
    private boolean success = false;
    private String id;

    public boolean isSuccess() {
      return success;
    }

    public void setSuccess(boolean success) {
      this.success = success;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }
  }

  public class Activation {
    private String id;
    private Boolean active;

    Activation(String id, Boolean active) {
      this.id = id;
      this.active = active;
    }

    /** @return the id */
    public String getId() {
      return id;
    }

    /** @param id the id to set */
    public void setId(String id) {
      this.id = id;
    }

    /** @return the active */
    public Boolean getActive() {
      return active;
    }

    /** @param active the active to set */
    public void setActive(Boolean active) {
      this.active = active;
    }
  }
}
