const { CleanWebpackPlugin } = require('clean-webpack-plugin');

module.exports = function override(config, env) {
  if (env === "production") {
    config.plugins = (config.plugins || []).concat([
      new CleanWebpackPlugin({
        cleanAfterEveryBuildPatterns: ['mockServiceWorker.js'],
        cleanOnceBeforeBuildPatterns: []
      })
    ]);
  }
  return config;
}
