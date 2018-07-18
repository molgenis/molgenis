# Administrator features





# Specific settings

## Mail settings
You should configure an email server to interact with your users for things like lost password recovery.
You can find the mail settings in the Admin menu, under Settings.
At the top of the page, type "Mail settings" into the selection box.

For backwards compatability, the default settings are filled with the values provided in `molgenis-server.properties` 
under the keys `mail.username` and `mail.password`, but the values provided here will override those settings.

For basic configuration, you only need to provide the username and password fields with a valid Gmail username and password.
But you may also specify a different (non-Gmail) SMTP server.

If you've filled in a username and password, the settings will be validated when you save them, by making a connection
with the mail server. If you do not want the settings to be tested at all, you can set `testConnection` to false.

### JavaMail properties
By default, the following low-level JavaMail properties, needed to interact with the Gmail SMTP server, are set:
```
mail.smtp.starttls.enable=true
mail.smtp.quitwait=false
mail.smtp.auth=true
```
You may override these properties or add additional properties and override these defaults by adding entities to the 
```JavaMailProperty``` repository in the Data Explorer. Each key may be provided at most once.
For a list of valid keys, check https://javamail.java.net/nonav/docs/api/

> E.g. Add an entity with key ```mail.debug``` and value ```true``` if you'd like to debug the mail dialog with the server.


# Authentication
When you are a superuser in MOLGENIS, you can configure three authentication methods: 
 * Username/password signin *(default)*
 * Google-sigin
 * Token-authentication
 
**Username/password signin (default)**  
In the App Settings:
 * Set user-moderation to user can sign up
 * Set two factor authentication: 
   * Disabled: users CAN NOT use two-factor-authentication
   * Enforced: users MUST use two-factor-authentication
   * Enabled: users CAN use two-factor-authentication
   
**prompted:** The default setting for tow-factor-authentication for a fresh installation *"Disabled"*. When two-factor-authentication is *"Enabled"* it is default disabled for the users.  

**Google-signin**  
The Google-signin setting is used to enable the possibility for users to login with their Google-account. 
 
**Token-authentication**
No specific superuser settings are necessary for the implementation of token-authentication.
 
