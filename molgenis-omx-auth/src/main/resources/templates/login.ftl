<form id="login-form" class="form-horizontal">
  <div class="control-group" style="margin-bottom: 0px;">
    <label class="control-label" for="inputUsername">Username</label>
    <div class="controls">
      <input type="text" id="inputUsername" name="username" placeholder="Username">
    </div>
  </div>
  <div class="control-group" style="margin-bottom: 0px;">
    <label class="control-label" for="inputPassword">Password</label>
    <div class="controls">
      <input type="password" id="inputPassword" name="password" placeholder="Password">
    </div>
  </div>
  <div class="control-group" style="margin-bottom: 0px;">
    <div class="controls">
      <button id="login-button" type="submit" class="btn btn-primary">Sign in</button>
    </div>
  </div>
</form>
<script type="text/javascript">
    $('#login-button').click(function(e) {
    	e.preventDefault();
    	var modal = $('#login-form').parents('.modal');
        $.ajax({
            type: 'POST',
            url:  '/login',
            data: $('#login-form').serialize(),
            success: function () {
            	if(modal) {
            		modal.data('modal').options.authenticated = true;
            		modal.modal('hide');
            	}
            },
            error: function() {
            	$('#inputPassword').after($('<p class="text-error">The username or password you entered is incorrect.</p>'));
            	if(modal) {
            		modal.data('modal').options.authenticated = false;
            	}
            }
        });
    });
</script>