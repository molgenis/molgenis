# Settings
There are different items where you can configure settings in MOLGENIS:
- [Application settings](#application-settings)
- [Authentication settings](#authentication-settings)
- [Mail settings](#mail-settings)
- [DataExplorer settings](../finding-data/guide-explore.md)
- OpenCPU settings

## Application settings
There are a three main sections in the application settings.

- General settings
- Tracking settings
- [ReCaptcha settings](#recaptcha-settings)

### ReCaptcha settings
ReCaptcha is a service currently only provided by Google. It basically works on determining a BOT-score. The value of this score is based on the request send to the server. The request is validated based on host-key comparison and other variables which are part of the inner workings of reCaptcha.
To set it up you need to configure an account on [ReCaptcha](https://www.google.com/recaptcha) (Check [Adding a domain at reCaptcha](#adding-domain-at-recaptcha)). Secondly you need to define properties in MOLGENIS (please check: [Configure reCaptcha in MOLGENIS](#configure-recaptcha-in-molgenis)).

#### Adding domain at reCaptcha
You have to configure a domain at [ReCaptcha](https://www.google.com/recaptcha). If you follow the this [documentation](https://developers.google.com/recaptcha/docs/v3) you can register your domain to use reCaptcha.

#### Configure reCaptcha in MOLGENIS
Filling out the settings in the *Application settings* in MOLGENIS.

You have to configure 6 properties:

- *ReCaptcha secret*: Can be obtained by configuring your domain in [ReCaptcha from Google](https://www.google.com/recaptcha) 
- *ReCaptcha site*: Can be obtained by configuring your domain in [ReCaptcha from Google](https://www.google.com/recaptcha)
- *Enable reCaptcha*: If set to true then the reCaptcha is enabled in MOLGENIS
- *Verification URI*: Fires a request for verification if the user is a BOT or not
- *BOT threshold*: A number between 0.0 and 1.0. 1.0 is most likely a human and 0.0 is most likely a BOT. The threshold is compare to the score that reCaptcha calculates. 

> note: Make sure you have your [Mail settings](#mail-settings) configured as well.

## Mail settings
You should configure an email server to interact with your users for things like lost password recovery.
You can find the mail settings in the Admin menu, under Settings.
At the top of the page, type "Mail settings" into the selection box.

For backwards compatibility, the default settings are filled with the values provided in `molgenis-server.properties` 
under the keys `mail.username` and `mail.password`, but the values provided here will override those settings.

For basic configuration, you only need to provide the username and password fields with a valid Gmail username and password.
But you may also specify a different (non-Gmail) SMTP server.

If you've filled in a username and password, the settings will be validated when you save them, by making a connection
with the mail server. If you do not want the settings to be tested at all, you can set `testConnection` to false.

#### JavaMail properties
By default, the following low-level JavaMail properties, needed to interact with the Gmail SMTP server, are set:
```
mail.smtp.starttls.enable=true
mail.smtp.quitwait=false
```

You may override these properties or add additional properties and override these defaults by adding entities to the 
```JavaMailProperty``` repository in the Data Explorer. Each key may be provided at most once.
For a list of valid keys, check https://javamail.java.net/nonav/docs/api/

> E.g. Add an entity with key ```mail.debug``` and value ```true``` if you'd like to debug the mail dialog with the server.

#### Mail server without authentication
When you want to setup a mail server without authentication you need to add the following properties:
```properties
mail.smtp.auth=false
mail.from=noreply@domain.ext
```

You can add them by entering them in the mail property table. Or override them in the environment or molgenis-server.properties.

*Environment:*

```bash
#!/bin/bash

export mail.smtp.auth=false
export mail.from=noreply@domain.ext 
```

*molgenis-server.properties:*
```properties
mail.smtp.auth=false
mail.from=noreply@domain.ext 
```

You need to add the ```mail.from``` property otherwise the server resolves to ```root@localhost```.

## Authentication settings
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
 
