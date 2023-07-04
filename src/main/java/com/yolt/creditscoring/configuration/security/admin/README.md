## Admins / OAuth2

### The flow / custom additions to spring security.
It starts with the granting (redirect, auth_code) flow.
I don't know the reason, but we chose to keep the state about the 'authorisation' request in an encrypted JWT as cookie on the clients side.
For this The [HttpCookieOauth2AuthorizationRequestRepository](HttpCookieOAuth2AuthorizationRequestRepository.java) can be consulted.

Then the user is authenticated, and we receive an identifier(idpId) + email of the user.
Then we verify whether this user (idpId) is a clientAdmin. Additionally, for microsoft, we check if the CfaAdmin security group role is present.
See [CustomOAuth2UserService.java](CustomOAuth2UserService.java)

Some information about the user is stored in an encrypted jwt that is sent to the client.
Upon use, the encypted token is checked in [ClientAdminJwtAuthorizationFilter.java](ClientAdminJwtAuthorizationFilter.java)


### CFA Admin / Client Admin / Both!

Users that log in through OIDC could have 2 roles/identities:
1) ClientAdmin
2) CfaAdmin

See main readme for their description.
A peculiar requirement, is that once a users logs in, he/she could also have both(!) roles / identities. Note that at time of writing, this is only possible for microsoft, because only Yolt employees can be CFA Admin on their microsoft account that is controlled by yolt. 

This makes it rather complex, because this means that the user that is logged in, can take 2 different identities.
Currently, the existing code heavily relies on a [ClientAdminPrincipal.java](ClientAdminPrincipal.java).
A 'clientAdmin' is some user that acts on behalf of a client, and has a clientId. A CFA admin has nothing to do with a client and does not have a clientId.
The current functionality needs a client-admin (mostly clientId).
Therefore, the authenticated user is 'upcasted' 'just in time' before it is injected in a controller to take the correct identity.

An alternative approach would be to create a more generic 'user' that has either 'client-admin' attributes 'cfa-admin' attributes of both.
However, that requires a lot of refactoring, and leaves it up to the controller/application-code to do all the checking/validation.
Currently, the endpoints are not reusable because a cfa-admin will interact with /api/management to manage clients, and a 
client-admin will interact with /api/admin. Therefore, the current option to cast the authenticated user to the correct identity before it enters the controller is the easier option for now.
