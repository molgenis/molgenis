# Authentication

In case data is access controlled, you need to authenticate yourself. In MOLGENIS you can sign in using three methods.
 * Username/password sign in
 * Single sign-on
 * Token-authentication

## Username/password sign in
The default way to authenticate in MOLGENIS is to click 'Sign in'. You can register a new account by using the 'Sign-up'-link. If there is no 'Sign-up'-link, you'll have to contact the administrator to register an account.
>note: If you want to use reCaptcha when you enable 'Sign up' then you can configure that [here](../guide-settings.md).  

### Two-factor authentication
If you have an existing MOLGENIS-account you can secure it with two-factor authentication, depending on the server's settings.

**prompted:** Two-factor authentication cannot be combined with single sign-on

When two-factor authentication is enabled and you sign in for the first time, you will be promted to secure your account with an authenticator app. The authentication can be configured by scanning a QR-code.

![two-factor authentication activation](../../../images/molgenis_two-factor-authentication_activation.png?raw=true, "two-factor authentication activation")

You have to scan the QR-code with an authenticator-app. Examples of authenticator-apps are:

 * **Android**
   * [Google Authenticator](https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2)
   * [Authy](https://play.google.com/store/apps/details?id=com.authy.authy)
   * [DUO](https://play.google.com/store/apps/details?id=com.duosecurity.duomobile&hl=nl)
 * **iPhone**
   * [Google Authenticator](https://itunes.apple.com/app/google-authenticator/id388497605?mt=8)
   * [Authy](https://itunes.apple.com/app/authy/id494168017?mt=8)
   * [DUO](https://itunes.apple.com/app/duo-mobile/id422663827?mt=8)

When the QR-code is scanned, your authenticator-app will create an account for MOLGENIS and also generate a verification code for that account.
You have to fill in the verification code in de box below the QR-code. If you have entered the verification code you will be redirected to the
*Account-Security*-tab. This will show the recovery-codes.

![two-factor authentication recoverycodes](../../../images/molgenis_two-factor-authentication_recoverycodes.png?raw=true, "two-factor authentication recoverycodes")

Make sure you store the recovery codes somewhere (not in MOLGENIS) so you can access them when you for example loose your phone (see [TROUBLESHOOTING](#TROUBLESHOOTING)).
Each time you sign in, you will have to enter the verification code.

![two-factor authentication configured](../../../images/molgenis_two-factor-authentication_configured.png?raw=true, "two-factor authentication configured")

Depending on the server's settings, you can enable, disable and reset your two-factor authentication in your account settings (under Security).

### TROUBLESHOOTING  
When you have lost your phone or misplaced it, you have to use one of the recovery codes to unlock your account. You can view your recovery codes
in the *Account-Security*-tab. Make sure to store the recovery codes somewhere outside MOLGENIS. You can click on the 'Enter a recovery code'-link,
in the screen where you have to enter the verification code. You can then enter the recovery code to unlock your account.

## Single sign-on
In addition to username/password authentication MOLGENIS supports authentication with identity providers that support [OpenID Connect](https://openid.net/connect/)
such as [Google](https://developers.google.com/identity/protocols/OpenIDConnect) and [SURFconext](https://wiki.surfnet.nl/display/surfconextdev/OpenID+Connect+reference).

Once enabled the sign in dialog will display additional sign in options:

![Single sign-on dialog](../../../images/security/authentication/signin_dialog_oidc.png?raw=true, "Single sign-on dialog")

Selecting e.g. Google will redirect the browser to their website so the user can authenticate there and return to MOLGENIS once authentication has successfully completed.
The permissions you have once authenticated are the default user permissions set by an administrator.   

### Configuring single sign-on   
As administrator single sign-on configuration consists of the following one-time steps:
1. Configuring one or more OpenID Connect client
2. Modify authentication settings

#### Configuring OpenID Connect clients
OpenID Connect clients can be configured by adding entities to the 'OIDC client' entity type (e.g. using the data explorer, importer or REST API).
The required information is provided by the identity provider. MOLGENIS requires at least the 'sub' and 'email' claims and ideally the
'given_name' and 'family_name' claims as well. These claimes are requested by specifying the scopes 'openid,email,profile'.  

#### Activating OpenID Connect clients
Configured OpenID Connect clients can be selected in the 'Authentication settings' part of the 'Settings manager' plugin. Activating clients
requires 'Allow users to sign up' to be set to 'Yes' and 'Sign up moderation' to be set to 'No'.

![Single sign-on authentication settings](../../../images/security/authentication/authentication_settings_oidc.png?raw=true, "Single sign-on authentication settings")

### Advanced: OpenID Connect user to MOLGENIS user mapping
When a user signs in through e.g. Google the user is automatically mapped to a MOLGENIS user based on the email address. If no MOLGENIS user exists
a new MOLGENIS user is created. The mapping from an OpenID client user to MOLGENIS user is persisted in the 'OIDC user mapping' entity type and can
be modified by an administrator if required. Multiple mappings to the same user are allowed such that when a user signs in with e.g. either a Google
and SURFconext account will be identified as the same MOLGENIS user.

## Token-authentication
When you use the REST API you have to authenticate using a token. There are 3 ways you can generate a token.
 * Create a token via the REST API v1 /login route (only available without two-factor authentication)
 * Create a token via the UI (e.g. DataExplorer)

### Create a token via REST API v1/login
When you create a POST request to v1/login you have to put the username and password in JSON in the body of the request.

```
{
  username: #username#,
  password: #password#
}
```

You can't login in via this route when two-factor authentication is enabled for the current user.

### Automatically generated token
When you run scripts in MOLGENIS a token is generated automatically with the credentials of the current user.

### Manually created token
When you want to manage your tokens manually there are different methods in MOLGENIS to do that. We now explore on of the ways
to create new tokens. You can create manually tokens in the DataExplorer. When you search on "*token*", you can edit the
existing tokens.

![Token search](../../../images/molgenis_token_search.png?raw=true, "Token search")

When you click on the add-button you can manually assign a token to a user. This token can be used to access the API's
of MOLGENIS.

![Token creation](../../../images/molgenis_token_creation.png?raw=true, "Token creation")
