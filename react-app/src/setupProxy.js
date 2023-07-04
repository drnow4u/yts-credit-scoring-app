const fs = require("fs");

const faker = require("@faker-js/faker");
const createProxyMiddleware = require("http-proxy-middleware");

const { homepage } = JSON.parse(fs.readFileSync("./package.json"));

const publicBase = `${homepage}api`;
const adminBase = `${homepage}api/admin`;

module.exports = function (app) {
  if (
    process.env.NODE_ENV === "development" &&
    process.env.REACT_APP_DISABLE_MSW !== "true"
  ) {
    app.use(`${adminBase}/oauth2/github`, (_req, res) => {
      res.status(301);
      res.set("Location", `${adminBase}/oauth2/fake_idp_redirect`);
      res.end();
    });
    app.use(`${adminBase}/oauth2/fake_idp_redirect`, (_req, res) => {
      res.status(301);
      res.set(
        "Location",
        `${homepage}admin/oauth2/callback/github?code=${faker.random.alphaNumeric(
          16
        )}&state=${faker.random.alphaNumeric(16)}`
      );
      res.end();
    });

    app.use(`${publicBase}/example_bank_login`, (_req, res) => {
      res.status(301);
      res.set(
        "Location",
        `${homepage}site-connect-callback?state=abc123&code=abc123`
      );
      res.end();
    });
  } else {
    app.use(
      "/api",
      createProxyMiddleware({
        target: "http://localhost:8080",
        changeOrigin: false,

        toProxy: true,
        hostRewrite: "localhost:3000",
      })
    );
  }
};
