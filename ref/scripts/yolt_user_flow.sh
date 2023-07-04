#!/usr/bin/env bash

# Script based on examples from https://developer.yolt.com/docs/getting-started
# To run it you have to install: https://github.com/smallstep/cli, jq
# Compatibility: WSL and WLS2. For WSL install https://github.com/wslutilities/wslu and `export BROWSER=wslview`
# In case of LF/CR problem run  `dos2unix ./yolt_user_flow.sh`
# If your mTLS and signing keys are not in current directory set variable KEY_CERT_PATH
# If your YOLT API host is not https://api.sandbox.yolt.io set variable YTS_API_HOST
# private-key.pem - signing private key file name
# tls-private-key.pem - tls private key file name
# tls-certificate.pem - tls certificate


CLIENT_ID=7da0a120-8cb7-4a6d-9973-166d931ba6e6
REQUEST_TOKEN_PUBLIC_KEY_ID=f9aa84b2-a5b4-4e63-b30f-aaa816d38191
REDIRECT_URL_ID=1114ef2f-52c7-4ccb-902a-0825aaa26a57
YTS_API_HOST="${YTS_API_HOST:-https://api.sandbox.yolt.io}"
KEY_CERT_PATH="${KEY_CERT_PATH:-./}"

#Uncomment if verbose output needed
#set -ex

REQUEST_TOKEN=$(step crypto jwt sign --iss $CLIENT_ID \
  --kid $REQUEST_TOKEN_PUBLIC_KEY_ID \
  --key $KEY_CERT_PATH/private-key.pem \
  --alg RS512 \
  --jti=`uuidgen` \
  --subtle)

ACCESS_TOKEN=$(curl $YTS_API_HOST/v1/tokens \
  --silent \
  --cert $KEY_CERT_PATH/tls-certificate.pem \
  --key $KEY_CERT_PATH/tls-private-key.pem \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&request_token=$REQUEST_TOKEN"  | jq '.access_token' |  sed 's/"//g')

SITE_ID=$(curl $YTS_API_HOST/site-management/v2/sites \
  --silent \
  --cert $KEY_CERT_PATH/tls-certificate.pem \
  --key $KEY_CERT_PATH/tls-private-key.pem \
  --header "Content-Type: application/json" \
  --header "Authorization: Bearer $ACCESS_TOKEN" | jq  '.[0].id' |  sed 's/"//g')

echo "Site id: $SITE_ID"

USER_ID=$(curl -X POST $YTS_API_HOST/v2/users \
  --silent \
  --cert $KEY_CERT_PATH/tls-certificate.pem \
  --key $KEY_CERT_PATH/tls-private-key.pem \
  --header "Content-Type: application/json" \
  --header "Authorization: Bearer $ACCESS_TOKEN" | jq  '.id' |  sed 's/"//g')

echo "User id: $USER_ID"

REDIRECT_URL=$(curl -X POST "$YTS_API_HOST/v1/users/$USER_ID/connect?site=$SITE_ID&redirectUrlId=$REDIRECT_URL_ID" \
  --silent \
  --cert $KEY_CERT_PATH/tls-certificate.pem \
  --key $KEY_CERT_PATH/tls-private-key.pem \
  --header "Content-Type: application/json" \
  --header "Authorization: Bearer $ACCESS_TOKEN" \
  --header "PSU-IP-Address: ff39:6773:c03c:48e8:5b49:492a:d198:4b05" | jq  '.redirect.url' |  sed 's/"//g')

echo "Redirect url: $REDIRECT_URL"
$BROWSER "$REDIRECT_URL"

echo "Copy redirect back URL from browser: "
read redirectback

user_site_response=$(curl -X POST "$YTS_API_HOST/v1/users/$USER_ID/user-sites" \
  --silent \
  --cert $KEY_CERT_PATH/tls-certificate.pem \
  --key $KEY_CERT_PATH/tls-private-key.pem \
  --header "Content-Type: application/json" \
  --header "Authorization: Bearer $ACCESS_TOKEN" \
  --data "{\"redirectUrl\": \"$redirectback\", \"loginType\": \"URL\"}" \
  --header "PSU-IP-Address: ff39:6773:c03c:48e8:5b49:492a:d198:4b05")

#echo $user_site_response | jq
USER_SITE_ID=$(echo "$user_site_response" | jq '.userSiteId' |  sed 's/"//g')
CONNECTION_STATUS=$(echo "$user_site_response" | jq '.userSite.connectionStatus' |  sed 's/"//g')

echo "User site id: $USER_SITE_ID"
echo "Connection status: $CONNECTION_STATUS"

while [ "$CONNECTION_STATUS" != "CONNECTED" ]
do
  echo "Checking user site connection status: "
  user_site_status_response=$(curl "$YTS_API_HOST/v1/users/$USER_ID/user-sites/$USER_SITE_ID" \
    --silent \
    --cert $KEY_CERT_PATH/tls-certificate.pem \
    --key $KEY_CERT_PATH/tls-private-key.pem \
    --header "Authorization: Bearer $ACCESS_TOKEN")

  CONNECTION_STATUS=$(echo "$user_site_status_response" | jq '.connectionStatus' |  sed 's/"//g')
  echo "Connection status recheck: $CONNECTION_STATUS"
done

echo "Fetching accounts"

ACCOUNT_ID=$(curl "$YTS_API_HOST/v1/users/$USER_ID/accounts" \
  --silent \
  --cert $KEY_CERT_PATH/tls-certificate.pem \
  --key $KEY_CERT_PATH/tls-private-key.pem \
  --header "Authorization: Bearer $ACCESS_TOKEN"   | jq '.[0].id' |  sed 's/"//g')

echo "Fetching transactions for first account id: $ACCOUNT_ID"

curl "$YTS_API_HOST/v1/users/$USER_ID/transactions?accountIds=$ACCOUNT_ID" \
  --silent \
  --cert $KEY_CERT_PATH/tls-certificate.pem \
  --key $KEY_CERT_PATH/tls-private-key.pem \
  --header "Authorization: Bearer $ACCESS_TOKEN" | jq
