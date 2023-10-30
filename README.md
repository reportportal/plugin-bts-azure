# Azure DevOps bug tracking system integration for ReportPortal

## UI

Preconditions:
- Install Node.js (version 14 is recommended).

Install the dependencies: `npm install`

Run in dev mode:
```bash
npm run dev # Run webpack in dev watch mode
npm run start # Serve built files
```

_Available only from RP v23.3_: use
```javascript
window.RP.overrideExtension(pluginName, url);
```
function call in browser to override the plugin UI assets in favor of your local development changes, f.e.
```javascript
window.RP.overrideExtension('Azure DevOps', 'http://localhost:9090');
```

Build the UI source code: `npm run build`

**How it works**: [UI plugin docs](https://github.com/reportportal/service-ui/blob/master/docs/14-plugins.md).

## Build the plugin

Preconditions:
- Install JDK version 11.
- Specify version number in gradle.properties file.

**Note:** Versions in the _develop_ branch are not release versions and must be postfixed with `NEXT_RELEASE_VERSION-SNAPSHOT-NUMBER_OF_BUILD (Example: 5.3.6-SNAPSHOT-1)`

Build the plugin: `gradlew build`
