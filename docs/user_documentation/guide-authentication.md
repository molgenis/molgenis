# User authentication in MOLGENIS
In MOLGENIS you can sign in using three methods. 
 * Default-authentication
 * Google-authentication
 * Token-authentication

## Default-authentication
The default way to authenticate in MOLGENIS is to click 'Sign in'. You can register a new account by using the 'Sign-up'-link. If there is no 'Sign-up'-link, you'll have to contact the administrator to register an account.

### 2-factor-authentication
If you have an existing MOLGENIS-account you can secure it with two factor authentication, depending on the server's settings. 

*note: you can not use Google-Authentication in combination with two factor authentication in MOLGENIS*

When two factor authentication is enabled and you sign in for the first time, you will be promted to secure your account with an authenticator app. The authentication can be configured by scanning a QR-code.

![2 factor activation](../images/molgenis_2fa_activation.png?raw=true, "2 factor activation")

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

![2 factor recoverycodes](../images/molgenis_2fa_recoverycodes.png?raw=true, "2 factor recoverycodes")

Make sure you have stored the recovery codes somewhere (not in MOLGENIS) so you can access them when you for example lost your phone (see [TROUBLESHOOTING](#TROUBLESHOOTING)). 
Each time you sign in, you will have to enter the verification code.
  
![2 factor configured](../images/molgenis_2fa_configured.png?raw=true, "2 factor configured")

Depending on the server's settings, you can enable, disable and reset your two factor authentication in your account settings (under Security).

###TROUBLESHOOTING  
When you have lost your phone or misplaced it, you have to use one of the recovery-codes to unlock your account. You can view your recovery codes 
in the *Account-Security*-tab. Make sure to store the recovery codes somewhere outside MOLGENIS. You can click on the 'Enter a recovery code'-link, 
in the screen where you have to enter the verification code. You can then enter the recovery code to unlock your account.

## Google-Authentication
When your administrator has enabled 'Google Sign in' you can use your Google-account to authenticate in MOLGENIS. In the login-screen you can 
see an additional button. Click on it to authenticate with your Google-account. 

![Google_sigin](../images/molgenis_google_signin.png?raw=true, "Google Sign in")

This will create a user which has only rights to the *Account*-tab. You have to ask your administrator to set the permissions you need in MOLGENIS.

When your administrator has enabled 'Google Sign in' you can use your Google-account to authenticate in MOLGENIS. In the login screen you can 
see an additional button. Click it to authenticate with your Google account.

### Link your Google-account
When you already have a MOLGENIS account you can link it to your Google account. This way you can use your Google account to authenticate in MOLGENIS. 
Before you 'Sign in' with your Google account, make sure your MOLGENIS account has the same email address as your Google account. This way the 
MOLGENIS account will automatically be linked to your Google account.

When you've already signed in with your Google account and did not use the same email address as your MOLGENIS account you have to contact your administrator.

## Token-authentication
When you use the RESTAPI you have to authenticate using a MOLGENIS-token. There are 3 ways you can generate a MOLGENIS-token.
 * Create a MOLGENIS-token via the RESTAPI v1 /login route (only available without 2-factor-authentication)
 * Create a MOLGENIS-token automatically in MOLGENIS
 * Create a MOLGENIS-token manually (via the DataExplorer)

### Create a MOLGENIS-token via RESTAPI v1/login
When you create a POST request to v1/login you have to put the username and password in JSON in the body of the request. 

```
{
  username: #username#,
  password: #password#
}
```

You can't login in via this route when 2-factor-authentication is enabled for the current user.

### Automatically generated MOLGENIS-token
When you run scripts in MOLGENIS a token is generated automatically with the credentials of the current user.
 
### Manually created MOLGENIS-token
When you want to manage your tokens manually you can use the DataExplorer. When you search on "*token*", you can 
edit the existing MOLGENIS-tokens.
 
![Token search](../images/molgenis_token_search.png?raw=true, "Token search")

When you click on the add-button you can manually assign a token to a user. This token can be used to access the API's 
of MOLGENIS.