# YTS Credit Scoring App

# Table of contents
1. [Introduction](#introduction)
2. [Application information](#application-information)
   1. [Actors on CFA](#actors-on-cfa)
   2. [Context diagram](#context-diagram)
3. [For the yolt maintainer / standby-er: impact downtime](#for-the-yolt-maintainer--standby-er-impact-downtime)
4. [Running locally](#running-locally-against-local-environment)
    1. [Prerequisite](#prerequisite)
    2. [Vault secrets](#vault-secrets)
    4. [Database setup](#database-setup)
    6. [Run front-end served from back-end](#run-front-end-served-from-back-end)
    7. [Run back-end and front-end separate](#run-back-end-and-front-end-separate)
5. [Running locally against team5 Postgres](#running-locally-against-team5-postgres)
6. [New client setup](#new-client-setup)
7. [Client management](#client-management)
8. [Client onboarding - required information](#client-onboarding---required-information)
   1. [client admin OIDC](#client-admin-oidc)
      1. [Github](#github)
      2. [Google](#google)
      3. [Microsoft](#microsoft)
9. [Client onboarding - checklist](#client-onboarding---checklist)
10. [Release procedure](#release-procedure)
11. [Backup DTA RDS with Docker](#backup-dta-rds-with-docker)
12. [Estimate API](#estimate-api)
13. [CFA Server to server API](#cfa-server-to-server-api)
    1. [Invite user](#invite-user)
    2. [Fetch user's report](#fetch-users-report)

## Introduction
The YTS Credit scoring app, also goes by the name Cash Flow Analyser (CFA).   
This is an application positioned on top of the YTS (AIS) API and offers clients the ability to generate a credit scoring report of their customers/prospects.

This application has a UI for 'client-admins', through which they can invite a user by email to initiate a credit scoring assessment. After the user
gave consent to his/her data, a credit scoring report is generated which can be downloaded by the client-admin. The main purpose is
to give clients insights into their users/prospects financial situation without having to integrate with API's. This is a "SaaS" like application.
However, it also does offer the capability to invite a user and download a report by an API, rather than using the UI.


This application is deployed "outside" the YTS Environment and acts as a non licensed client for YTS Backend.

## Application Information

### Actors on CFA
Users in CFA:

| Actor                 | role               | endpoints       | description                                                                                                                                  |
|-----------------------|--------------------|-----------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| Users                 | CREDIT_SCORE_USER  | /api/user       | They are the end users for which we will do a credit check. They interact through the browser with /api/user                                 |
| Client admins         | CLIENT_ADMIN       | /api/admin      | They are employees of a company that is our client. A client (company) uses CFA to do a credit check on their customers (our 'users')        |
| CFA admins            | CFA_ADMIN          | /api/management | They are 'super admins' within yolt, who can manage clients and the client-admins.                                                           |
| Client / Client token | CLIENT_TOKEN       | /api/customer   | A client (application) can also interact directly with our API. A client-admin can create an api token which is used to communicate with CFA |

##### Users
Users are local in CFA.

#### Client admins
Client admins and CFA admins both log in through OIDC.
A OIDC user is a client admin if that is configured in CFA. A client-admin record is created for the identifier (idpId) of the oidc user.
See [client-admin-oidc](#client-admin-oidc) for more info.

##### CFA Admins
Client admins and CFA admins both log in through OIDC.
currently, it is only possible to be CFA Admin for users that log in with microsoft. Only microsoft users within the yolt-tenante, with a 'CFA admin' security group
can act as a CFA Admin.
Unfortunately, there is only 1 yolt tenant, and 1 group. This means that you are CFA Admin on PRD and all other environments, or not.
In order to test functionality on test-environments, developers that don't have the CFA admin in AD, can add their idpID to the application properties
credit-scoring.test-admins.microsoftIds. This whitelist is only used on test environments.

##### Client token
see description table above.

### Context diagram

See [contxt-diagram.puml](context-diagram.puml). You can render it in your IDE, or by copy pasting it in [https://www.planttext.com](https://www.planttext.com)

The idea behind yts-credit-scoring-app is that it was positioned outside YTS. However, it currently leverages the existing k8s infrastructure, so it is still deployed
as one of the services on our kubernetes cluster.
yts-creditscoring-app acts like a client of YTS. It has a client-id `28d0b528-ae51-4224-8dbd-8603bbc09c20`, and uses the API through the client-proxy.
Note that it does not use ingress, which terminates TLS for other YTS-clients, because this was not feasible due to network routing on k8s. So, it bypasses ingress,
but still calls client-proxy.

## For the yolt maintainer / standby-er: impact downtime

If the yts-API, or the yts-credit-scoring-app is down, this has direct impact on all the clients (examples in [Introduction](#introduction) ) using the yts-credit-scoring-app, and should be resolved as soon as possible.
In case there is a partial outage, and the impact cannot be assessed, please contact the transactions-team. To assess impact on [production](https://cashflow-analyser.yolt.io/admin/login) a test client can be used, but only some members
of the transaction team have access as a client-admin.

## Running locally against local environment

### Prerequisite
- Java 15
- Maven
- Docker (for local Postgres setup)
- Node.js 15 and npm (for frontend)
- Vault secrets added to /vault/secrets catalog

### Vault secrets
In order to run the application locally there is a requirement to create the following path `/vault/secrets` on the local 
hard drive with the required secrets.

In Git Bash on ING Manage Windows computer:

```shell
export HOME=/c/Users/{YOUR_CK}/
vault-helper login --role devops
vault-helper k8s --cluster=team5
```

Before you copy secrets from pod check is only one pod running with name prefix `yts-credit-scoring-app-`:
```shell
kubectl -n ycs get pods | grep yts-credit-scoring-app
```

All the secrets can be copied from the team environment with the use of below command:

Make sure you in `c:\vault\secrets`
```shell
CFA_POD=$(kubectl -n ycs get pods | grep yts-credit-scoring-app | awk '{print $1}')
kubectl -n ycs cp -c vault-agent $CFA_POD:vault/secrets .
```

Now you need additional secrets or overwrite secrets from the team-environment because you run it locally:
- /vault/secrets/rds needs to be overwritten to connect with your local database instead of the team-database:
  [rds](local-dev/rds)
- Normally, CFA connects to client-proxy which does not require a client certificate. However, if you want to develop locally,
you cannot target the client-proxy directly. You can only use the yolt-api as an external party through ingress that requires a client
certificate. Therefore, you need to add 2 more secrets, a client-certificate key and certificate (which shouldn't be secret..)
Add these 2 files to your `c:\vault\secrets`:
[yts-app-tls-cert](local-dev/yts-app-tls-cert)
[yts-app-tls-key](local-dev/yts-app-tls-key)
If they expire, or if you want to target a different environment, you should change the private key and cert. (the files contain them base64 encoded)
Note that these client certificates are created by a developer while making a connection to the yolt-api. See
https://developer.yolt.com/docs/connect-to-yolt-tutorial
The public part (certificates) of the registered client certificates can be found in APY and dev portal.
- github client-id and client-secret have to be changed. This is because in github every clientId+clientSecret can only have 1 redirectUrl.
So, there is only 1 clientId and secret that will redirect you to localhost.
A valid clientId and secret that will redirect to localhost:3000 can be found here:
[github-client-secret](local-dev/github-client-secret)
[github-client-id](local-dev/github-client-id)
If you need to redirect to another host/port other than localhost:3000, you can create an app in github:
https://github.com/settings/apps.
Then, base64 encode the client-id / secret and put them in the files.

### Other remarks
- aws - optional: using the `local` profile will not send the email invitation and instead print it in the logs.
  In order to use the AWS service locally set the property `credit-scoring.amazon-ses.enabled` for local profile needs to be set to `true`.


### Database setup

For local development the Postgres database can be set up on Docker with the usage script
added into "scripts" catalog (docker-compose.yml).
Below is the command that should be executed in that folder:
```shell script
docker-compose up
```
Additionally, the database can be also set up manually. The database url, username, password
should be extracted from application-local.yml file.
Then export data from a team environment to your local postgres:
```bash
docker exec -it local_postgres bash
export PGPASSWORD="<your password from vault-helper rds -env=team5>"
export PGUSERNAME="<your username from vault-helper rds -env=team5>"
pg_dump --inserts --host=rds.team5.yolt.io --dbname=yts-credit-scoring-app --username=$PGUSERNAME -w > rds-team5.sql
psql yts-credit-scoring-app --username=yts-apps < rds-team5.sql
```

### Environments variables

#### Users table polling interval

`REACT_APP_USERS_TABLE_POLLING_INTERVAL` if exists is defining polling interval for users list on dashboard in ms. Removing this variable will disable polling.

### Run front-end served from back-end

1. Run command:
 `mvn clean spring-boot:run -Dspring-boot.run.profiles=local -Pfrontend-build`
  If you need to rebuild front-end without restarting back-end in the second console run:
  `mvn process-resources` and refresh browser's window.

### Run back-end and front-end separate

Run command to serve only REST API content from Spring Boot:
`mvn clean spring-boot:run -Dspring-boot.run.profiles=local,local-npm -P!frontend-copy-build`

and then to run front-end from development server:
`npm run start:no-msw`

To disable mock service worker for frontend

### Run front-end with mocks

Run command to serve only REST API content from Spring Boot:
`mvn clean spring-boot:run -Dspring-boot.run.profiles=local,local-npm -P!frontend-copy-build`

To run only front-end using mocks instead of real api:
`npm run start`

### Enable auto-polling for Users table on localhost

To enable polling for dashboard, add to environment file value:

`REACT_APP_USERS_TABLE_POLLING_INTERVAL`

Like in `env.production` file

For example to enable polling for local development add line:

`REACT_APP_USERS_TABLE_POLLING_INTERVAL=5000`

to file:

`.env.development.local`

If this file don't exist, create it. Remember it shouldn't be added into code repository (it's on ignore files list)

## Running locally against team5 Postgres

Note that when running against team5 we should and will not run Flyway updates.
If you want to test that, please do it against your local Postgres.

`mvn clean spring-boot:run -Dspring-boot.run.profiles=local,local-team5 -Pfrontend-build`


## New client setup
`scripts` catalog contains the following scripts for client initiation:
- [new_client_local_dev.sql](ref/scripts/new_client_local_dev_script.sql) - script that inits the database with test client, can be use for local development to set up
database. Requires only Github User ID to be placed in the script. 
- [new_client_init_script.sql](ref/scripts/new_client_init_script.sql) - script can be used as a template for adding new production clients, all values should be
changed accordingly the onboarding client.

## Client management
The application does not currently have any dashboard for client management.
In order to be able to manage client data on production environment, class [ClientManagementUseCase](src/main/java/com/yolt/creditscoring/service/client/ClientManagementUseCase.java)
can be used. The code is executing at the start of the application updating the client data accordingly. It also enables client onboarding,
instead of using script - [new_client_init_script.sql](ref/scripts/new_client_init_script.sql).

## Client onboarding - required information
To onboard a new client in the application the following information need to be gathered:

- Client name (mandatory) - Will be displayed in all user screens, email messages etc.
- Client logo (optional) - Will be displayed in the email template and consent page. If not provided email will not have the logo and consent page logo will be replaced by the client name.
- Client site tag - Will be used to narrow down the site results e.g. "NL" for only Dutch banks.
- Default Language - Default language for client that should be set on consent page.
- Additional consent text (optional) - Displayed on consent page, empty if not provided.
- Additional report text (optional) - Displayed on report page, empty if not provided.
- Email template (mandatory) - The invitation email template. The name of the file in [template folder](src/main/resources/mail) both for html and text.
- Email subject and sender (optional) - If not provided the default values will be used (subject:"Uitnodiging voor de Cashflow Analyser" sender:"Cashflow Analyser <no-reply-cashflow-analyser@yolt.com>")
- Legal documents (Terms and conditions, Privacy Policy) (mandatory) - content of the legal documents, example template can be found in [legal documents folder](ref/legaldocuments).
- Client admin users (mandatory) - Oauth provider IDs and emails of client admin users.
- Redirect URL - if client would like for a user to be redirected to client site after report calculation.
- PD score feature - feature toggle for client to calculate "Probability of default". It should be a business decision if we want to enable this feature for given client.
- Signature Verification Feature Toggle - feature toggle for enabling the signature report verification. Should be always set to true. Setting it to false should be done in cases when the signature is not working correctly for some reason.
- Category Feature Toggle - feature toggle for enabling the categories in credit report. Default set to false, should be a business decision if we want to enable this feature for given client.
- Overview Feature Toggle - feature toggle for enabling overview in credit report view.
- Months Feature Toggle - feature toggle for enabling monthly report in credit report view.
- API Token Feature Toggle

### client admin OIDC

#### Github
In order to get the Github ID the user needs to provide us with the Github username.
The Github ID can be fetched from Github API: https://api.github.com/users/client_user_github_username
The email address does not have to match the email address in the Github registration.

#### Google
To get the Google ID the user needs to manually get it from Google website:
https://developers.google.com/people/api/rest/v1/people/get?apix_params=%7B%22resourceName%22%3A%22people%2Fme%22%2C%22personFields%22%3A%22names%22%7D

When executing the endpoint the ID of an account will be returned in a response metadata.

The email provided by the user needs to match exactly with the one used in Google.

#### Microsoft
To get the Microsoft ID the user needs to manually get it from Microsoft website:
https://developer.microsoft.com/en-us/graph/graph-explorer

When executing the endpoint the ID of an account will be returned in a response.

The email provided by the user needs to match exactly with the one used in Microsoft.

For Outlook accounts the Id should be a 16 character word, that needs to me stored as a UUID format in the database
with 0 characters as prefix.
For example if returned id would be `af34d3f221c200d5`, in the database it needs to be stored as `00000000-0000-0000-af34-d3f221c200d5`.

Document [Yolt Cashflow Analyser - set up guide](https://yolt.sharepoint.com/:w:/r/sites/YTS/Shared%20Documents/2%20Cashflow%20Analyser/Yolt%20Cashflow%20Analyser%20-%20set%20up%20guide.docx?d=wccf48c603e0e44849d9ada83262b0956&csf=1&web=1&e=EjgYtr) can be distributed to clients.

## Client onboarding - checklist

1. Copy image used in e-mail template to [react-app/public/mail](react-app/public/mail). Verify the image used in the email is between 50px and 60px in height
2. Create MR similar to [Onboard FinancieringsGilde](https://git.yolt.io/backend/yts-credit-scoring-app/-/merge_requests/846/diffs)
   **Remarks** Don't use `-` in the template file name.
   * Set *Term and Condition* date to date when document was created (not on-boarding date)
   * Set *Privacy Policy* date to date when document was created (not on-boarding date)
3. Temporarily set a client admin account on *team5* and *yfb-acc* to onboarded client (only change admin's client-id) and when brief testing is finished for the new client change ids back to previous state.   
4. Add client to Grafana with similar MR to https://git.yolt.io/infra/dashboards/-/merge_requests/318
5. Check is the *Term and Condition* correctly displayed without any strange looking characters.
6. Check is the *Privacy Policy* correctly displayed without any strange looking characters.
7. Check is in user invitation e-mail link pointing to right client.
8. Ask Admin to check it before client will be informed.

## Release procedure

1. Check already existing error on `yfb-acc` in [Kibana ACC](https://kibana.yfb-acc.yolt.io/app/discover#/?_g=(filters:!(),refreshInterval:(pause:!t,value:0),time:(from:now-24h,to:now))&_a=(columns:!(app.level,app.message),filters:!(('$state':(store:appState),meta:(alias:!n,disabled:!f,index:kubernetes-ycs,key:app.pod,negate:!f,params:(query:yts-credit-scoring-app),type:phrase),query:(match_phrase:(app.pod:yts-credit-scoring-app)))),index:kubernetes-ycs,interval:auto,query:(language:kuery,query:'app.level:WARN%20OR%20app.level:ERROR'),sort:!(app.timestamp,desc)))   
2. In Gitlab pipeline deploy *master* to `yfb-acc`
3. Login into [admin dashboard ACC](https://cashflow-analyser.ycs.yfb-acc.yolt.io/admin/login)
4. Invite user
5. Open invitation link in e-mail in a private mode in the browser
6. Continue user journey
7. As an admin check credit report
8. Check new errors and warnings in [Kibana ACC](https://kibana.yfb-acc.yolt.io/app/discover#/?_g=(filters:!(),refreshInterval:(pause:!t,value:0),time:(from:now-24h,to:now))&_a=(columns:!(app.level,app.message),filters:!(('$state':(store:appState),meta:(alias:!n,disabled:!f,index:kubernetes-ycs,key:app.pod,negate:!f,params:(query:yts-credit-scoring-app),type:phrase),query:(match_phrase:(app.pod:yts-credit-scoring-app)))),index:kubernetes-ycs,interval:auto,query:(language:kuery,query:'app.level:WARN%20OR%20app.level:ERROR'),sort:!(app.timestamp,desc)))
9. Reviewer clicks *good to go to prod* in Gitlab's pipeline
10. Check already existing error on `yfb-sandbox` in [Kibana Sandbox](https://kibana.yfb-sandbox.yolt.io/app/discover#/?_g=(filters:!(),refreshInterval:(pause:!t,value:0),time:(from:now-24h,to:now))&_a=(columns:!(app.level,app.message,app.pod),filters:!(('$state':(store:appState),meta:(alias:!n,disabled:!f,index:kubernetes-ycs,key:app.pod,negate:!f,params:(query:yts-credit-scoring-app),type:phrase),query:(match_phrase:(app.pod:yts-credit-scoring-app)))),index:kubernetes-ycs,interval:auto,query:(language:kuery,query:'app.level:WARN%20OR%20app.level:ERROR'),sort:!(!(app.timestamp,desc),!('@timestamp',desc))))
11. In pipeline in column *Deploy to prd* click only `yfb-sandbox` manual job
12. Check status of the pod in [Kubernetes Dashboard](https://kube-dashboard.yolt.io/?envs=yfb-ext-prd,yfb-sandbox,yfb-acc,team5&namespaces=default,ycs&teams=all,yts-apps)
13. Switch off VPN connection
14. Login into [admin dashboard Sandbox](https://cashflow-analyser.sandbox.yolt.io/admin/login)
15. Invite user
16. Open invitation link in e-mail in a private mode in the browser
17. Continue user journey
18. Switch on VPN connection
19. Check new errors and warnings in [Kibana Sandbox](https://kibana.yfb-sandbox.yolt.io/app/discover#/?_g=(filters:!(),refreshInterval:(pause:!t,value:0),time:(from:now-24h,to:now))&_a=(columns:!(app.level,app.message,app.pod),filters:!(('$state':(store:appState),meta:(alias:!n,disabled:!f,index:kubernetes-ycs,key:app.pod,negate:!f,params:(query:yts-credit-scoring-app),type:phrase),query:(match_phrase:(app.pod:yts-credit-scoring-app)))),index:kubernetes-ycs,interval:auto,query:(language:kuery,query:'app.level:WARN%20OR%20app.level:ERROR'),sort:!(!(app.timestamp,desc),!('@timestamp',desc))))
20. Check already existing error on `yfb-ext-prd` in [Kibana PRD](https://kibana.yfb-ext-prd.yolt.io/app/discover#/?_g=(filters:!(),refreshInterval:(pause:!t,value:0),time:(from:now-24h,to:now))&_a=(columns:!(app.level,app.message,app.pod),filters:!(('$state':(store:appState),meta:(alias:!n,disabled:!f,index:kubernetes-ycs,key:app.pod,negate:!f,params:(query:yts-credit-scoring-app),type:phrase),query:(match_phrase:(app.pod:yts-credit-scoring-app)))),index:kubernetes-ycs,interval:auto,query:(language:kuery,query:'app.level:WARN%20OR%20app.level:ERROR'),sort:!(!(app.timestamp,desc),!('@timestamp',desc)))). 
21. In pipeline in column *Deploy to prd* click only `yfb-ext-prd` manual job
22. Check status of the pod in [Kubernetes Dashboard](https://kube-dashboard.yolt.io/?envs=yfb-ext-prd,yfb-sandbox,yfb-acc,team5&namespaces=default,ycs&teams=all,yts-apps)
23. Switch off VPN connection
24. Login into [admin dashboard PRD](https://cashflow-analyser.yolt.io/admin/login)
25. Invite user
26. Open invitation link in e-mail in a private mode in the browser
27. Continue user journey
28. If you don't have account in any of provided banks please ask someone who has it (ask to switch off VPN). 
29. Switch on VPN connection
30. Check new errors and warnings in [Kibana PRD](https://kibana.yfb-ext-prd.yolt.io/app/discover#/?_g=(filters:!(),refreshInterval:(pause:!t,value:0),time:(from:now-24h,to:now))&_a=(columns:!(app.level,app.message,app.pod),filters:!(('$state':(store:appState),meta:(alias:!n,disabled:!f,index:kubernetes-ycs,key:app.pod,negate:!f,params:(query:yts-credit-scoring-app),type:phrase),query:(match_phrase:(app.pod:yts-credit-scoring-app)))),index:kubernetes-ycs,interval:auto,query:(language:kuery,query:'app.level:WARN%20OR%20app.level:ERROR'),sort:!(!(app.timestamp,desc),!('@timestamp',desc))))
31. Update status of ticket in [JiRA](https://yolt.atlassian.net/jira/software/projects/YTSAPP/boards/138)

## Backup DTA RDS with Docker:

First, you need to obtain credentials with Vault.

```bash
docker exec -it local_postgres bash
export PGPASSWORD="password"
pg_dump --inserts --host=rds.team5.yolt.io --dbname=yts-credit-scoring-app --username=v-oidc-mar-devops-dM3whufJLueYWMvTOvty-16330 -w > rds-team5.sql
exit
docker cp local_postgres:/rds-team5.sql rds-team5.sql
```

## Estimate API

Simple query:

```shell
curl -v -i --user user -X POST https://estimate.azurefd.net/credit-score -H "Content-Type: application/json" -H 'Accept:application/json' -x squid.team5.yolt.io:3128 --data '{"referenceId":"12345","current_balance":{"unscaledValue":5901,"scale":2},"transactions":[{"id":"12345","amount":{"unscaledValue":1000,"scale":0},"currencyCode":"EUR","dateBooked":"2021-06-29"}]}'
```

In the folder [ref/estimate](ref/estimate) is sample request payload:

```shell
curl -v -i --user user  -X POST https://estimate.azurefd.net/credit-score -H "Content-Type: application/json" -H 'Accept:application/json' -x squid.team5.yolt.io:3128 --data-binary @/tmp/estimate_heeavy.json
```

The file with the payload has to be uploaded to pod with `kubectl`.

## CFA Server to server API

### Invite user

```shell
curl -i http://localhost:8080/api/customer/users/invite --data '{"name": "somename", "email":"somename@yolt.eu", "clientEmailId":"$CLIENT_EMAIL_ID"}' --header "Content-Type: application/json"  --header "Authorization: Bearer $TOKEN"
```
where:
* `CLIENT_EMAIL_ID` - optional field when client has 1 invitation e-mail template. Mandatory for multiple templates. To obtain it for a given environment given admin access to one of them, please use Inspect element functionality to see what clientEmailId is being sent in /api/admin/users/invite call when inviting user from GUI at /admin/dashboard.
* `TOKEN` - mandatory field. Token created at http://localhost:3000/admin/settings


### Fetch user's report

To get content of user report:

```shell
curl -i https://cashflow-analyser.ycs.yfb-acc.yolt.io/api/customer/v1/users/$USER_ID/report --header "Authorization: Bearer $TOKEN"
```
where:
* `USER_ID` - user's UUID visible in report e.g. http://localhost:3000/admin/report/f37a154a-7b6b-4894-91c3-41ea25ab5a39 or received when invitation was created
* `TOKEN` - mandatory field. Token created at http://localhost:3000/admin/settings

## Fetch OpenAPI file for M2M comunication

1. In [FrontendWebSecurityConfig.java](src/main/java/com/yolt/creditscoring/configuration/security/FrontendWebSecurityConfig.java) add `"/v3/api-docs.yaml"` in `antMatchers` method
2. Build package e.g. `./mvnw clean package -P-frontend-build -P-frontend-check-outdated -DskipTests`
3. Run application with Spring Boot profile `openapi` e.g. `./mvnw spring-boot:run -Dspring-boot.run.profiles=local,openapi -P-frontend-build -P-frontend-check-outdated`
4. Run `curl http://localhost:8080/v3/api-docs.yaml > ./doc/cfa-openapi.yaml`
5. Revert changes

