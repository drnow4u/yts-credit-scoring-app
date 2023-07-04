# Getting Started with Create React App

This project was bootstrapped with [Create React App](https://github.com/facebook/create-react-app).

## Available Scripts

In the project directory, you can run:

### `npm start`

Runs the app in the development mode.
Open [http://localhost:3000](http://localhost:3000) to view it in the browser.

The page will reload if you make edits.
You will also see any lint errors in the console.

### `npm test`

Launches the test runner in the interactive watch mode.
See the section about [running tests](https://facebook.github.io/create-react-app/docs/running-tests) for more information.

### `npm run build`

Builds the app for production to the `build` folder.
It correctly bundles React in production mode and optimizes the build for the best performance.

The build is minified and the filenames include the hashes.
Your app is ready to be deployed!

See the section about [deployment](https://facebook.github.io/create-react-app/docs/deployment) for more information.

### `npm run eject`

**Note: this is a one-way operation. Once you `eject`, you can’t go back!**

If you aren’t satisfied with the build tool and configuration choices, you can `eject` at any time. This command will remove the single build dependency from your project.

Instead, it will copy all the configuration files and the transitive dependencies (webpack, Babel, ESLint, etc) right into your project so you have full control over them. All of the commands except `eject` will still work, but they will point to the copied scripts so you can tweak them. At this point you’re on your own.

You don’t have to ever use `eject`. The curated feature set is suitable for small and middle deployments, and you shouldn’t feel obligated to use this feature. However we understand that this tool wouldn’t be useful if you couldn’t customize it when you are ready for it.

## Learn More

You can learn more in the [Create React App documentation](https://facebook.github.io/create-react-app/docs/getting-started).

To learn React, check out the [React documentation](https://reactjs.org/).

## Switch off mock server in front-end

1. In file [index.tsx](react-app/src/index.tsx) remove and don't commit it:

```typescript
if (process.env.NODE_ENV === "development") {
  const { worker } = require("./test/browser");
  (worker as SetupWorkerApi).start({
    serviceWorker: {
      url: `${homepage}mockServiceWorker.js`,
    },
  });
}
```

remember to remove Service Worker in the browser.

2. You can remove the file `src/setupProxy.js`. That will require you to restart the React dev server.

## Prettier

We use Prettier for automatic code formatting. It should run automatically by using editor plugins (for VSCode, IntelliJ) and also it should be run on staged files before committing code by a git hook.
It can also be run manually, but this shouldn't be necessary in most cases:

```shell
npm run prettier:write
```

For ESLint:

```shell
npx eslint src\ --fix
```

## Workaround for source map parsing warning

To disable warning about missing source maps from libraries, for now please add into your `.env.development.local` file:

```
GENERATE_SOURCEMAP=false
```

More information about this:

https://stackoverflow.com/a/70975849

https://github.com/facebook/create-react-app/pull/11752
