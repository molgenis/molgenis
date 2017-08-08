# User authentication in MOLGENIS
In MOLGENIS you can authenticate in MOLGENIS using 2 methods. 
 * Default-authentication
 * Google-Authentication

## Default-authentication
The default way to authenticate in MOLGENIS is to click on 'Sign in'. You can register when the administrator has 
enabled the 'Sign up' setting. If you can't register via the 'Sign up'-link you have to contact the administrator. 
In this case only the administrator can sign up new users. 

### 2-factor-authentication
If you have an existing MOLGENIS-account you can (or have to) secure it with 2-factor-authentication. 
There are 3 states of 2-factor-authentication in MOLGENIS.

 * **Disabled**: user CAN NOT configure 2 factor authentication
 * **Enforced**: users HAVE TO configure 2 factor authentication
 * **Enabled**: users CAN configure 2 factor authentication

*note: you can not use Google-Authentication in combination with 2-factor-authentication in MOLGENIS*

When 2-factor-authentication is **enforced** it is not possible to disable 2-factor-authentication. MOLGENIS will automatically
redirect you to the 'Configure 2-factor-authentication'-entry point. You have to scan the QR-code with an authenticator-app. 
Examples of authenticator-apps are:

 * **Android**
   * [Google Authenticator](https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2)
   * [Authy](https://play.google.com/store/apps/details?id=com.authy.authy)
   * [DUO](https://play.google.com/store/apps/details?id=com.duosecurity.duomobile&hl=nl)
 * **iPhone**
   * [Google Authenticator](https://itunes.apple.com/app/google-authenticator/id388497605?mt=8)
   * [Authy](https://itunes.apple.com/app/authy/id494168017?mt=8)
   * [DUO](https://itunes.apple.com/app/duo-mobile/id422663827?mt=8)
 
When the QR-code is scanned, your authenticator-app will create an account for MOLGENIS and also generate a verification code for that account. 
You have to fill in the verification code in de box below the QR-code. If you have entered the verification code you will be redirected to 
the *Account-Security*-tab. This will show the recovery-codes. Make sure you have stored the recovery codes somewhere (not in MOLGENIS) so you 
can access when you for example lost your phone (see TROUBLESHOOTING). Each time you sign in, you will have to enter the verification code.  

When 2-factor-authentication is **enabled**, you can enable or disable it in the *Account-Security*-tab. It is also possible to reset the
2-factor-authentication-secret. The configuration process is the same as when 2-factor-authentication is enforced.

**TROUBLESHOOTING**  
When you have lost your phone or misplaced it, you have to use one of the recovery-codes to unlock your account. You can view your recovery codes 
in the *Account-Security*-tab. Make sure to store the recovery codes somewhere outside MOLGENIS. You can click on the 'Enter a recovery code'-link, 
in the screen where you have to enter the verification code. You can then enter the recovery code to unlock your account.

## Google-Authentication
When your administrator has enabled 'Google Sign in' you can use your Google-account to authenticate in MOLGENIS. In the login-screen you can 
see an additional button. Click on it to authenticate with your Google-account. This will create a user which has only rights to the *Account*-tab. 
You have to ask your administrator to set the permissions you need in MOLGENIS.

### Link your Google-account
When you already have a MOLGENIS-account you can link it to your Google-account. This way you can use your Google-account to authenticate in MOLGENIS. 
Before you 'Sign in' with your Google-account, make sure your MOLGENIS-account has the same email address as your Google-account. This way the 
MOLGENIS-account will be automatically be linked to the Google-account.

When you already signed in with your Google-account and did not use the same email address as your MOLGENIS-account you have to contact your administrator. 
Only the administrator can fill in the Google-User-Account-ID. You will have to provide the Google-User-Account-ID to the administrator.