# User authentication in MOLGENIS
In MOLGENIS you can sign in using three methods. 
 * Username/password sign in *(default)*
 * Google-sign in
 * Token-authentication

## Username/password sign in
The default way to authenticate in MOLGENIS is to click 'Sign in'. You can register a new account by using the 'Sign-up'-link. If there is no 'Sign-up'-link, you'll have to contact the administrator to register an account.

### two-factor authentication
If you have an existing MOLGENIS-account you can secure it with two-factor authentication, depending on the server's settings. 

**prompted:** you can not use Google-Authentication in combination with two-factor authentication in MOLGENIS

When two-factor authentication is enabled and you sign in for the first time, you will be promted to secure your account with an authenticator app. The authentication can be configured by scanning a QR-code.

![two-factor authentication activation](../images/molgenis_two-factor-authentication_activation.png?raw=true, "two-factor authentication activation")

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

![two-factor authentication recoverycodes](../images/molgenis_two-factor-authentication_recoverycodes.png?raw=true, "two-factor authentication recoverycodes")

Make sure you store the recovery codes somewhere (not in MOLGENIS) so you can access them when you for example loose your phone (see [TROUBLESHOOTING](#TROUBLESHOOTING)). 
Each time you sign in, you will have to enter the verification code.
  
![two-factor authentication configured](../images/molgenis_two-factor-authentication_configured.png?raw=true, "two-factor authentication configured")

Depending on the server's settings, you can enable, disable and reset your two-factor authentication in your account settings (under Security).

###TROUBLESHOOTING  
When you have lost your phone or misplaced it, you have to use one of the recovery codes to unlock your account. You can view your recovery codes 
in the *Account-Security*-tab. Make sure to store the recovery codes somewhere outside MOLGENIS. You can click on the 'Enter a recovery code'-link, 
in the screen where you have to enter the verification code. You can then enter the recovery code to unlock your account.

## Google-sign in
When your administrator has enabled 'Google Sign in' you can use your Google-account to authenticate in MOLGENIS. In the login-screen you can 
see an additional button. Click on it to authenticate with your Google-account. 

![Google_sign in](../images/molgenis_google_signin.png?raw=true, "Google Sign in")

This will create a user which has only rights to the *Account*-tab. You have to ask your administrator to set the permissions you need in MOLGENIS.

When your administrator has enabled 'Google Sign in' you can use your Google-account to authenticate in MOLGENIS. In the login screen you can 
see an additional button. Click it to authenticate with your Google account.

### Link your Google-account
When you already have a MOLGENIS account you can link it to your Google account. This way you can use your Google account to authenticate in MOLGENIS. 
Before you 'Sign in' with your Google account, make sure your MOLGENIS account has the same email address as your Google account. This way the 
MOLGENIS account will automatically be linked to your Google account.

When you've already signed in with your Google account and did not use the same email address as your MOLGENIS account you have to contact your administrator.

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
 
![Token search](../images/molgenis_token_search.png?raw=true, "Token search")

When you click on the add-button you can manually assign a token to a user. This token can be used to access the API's 
of MOLGENIS.

![Token creation](../images/molgenis_token_creation.png?raw=true, "Token creation")