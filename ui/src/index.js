import { ExamplePluginTab } from 'components/examplePluginTab';
import { IntegrationFormFields } from './components/integrationFormFields';
import { IntegrationSettings } from './components/integrationSettings';

window.RP.registerPlugin({
  name: 'example',
  extensions: [
    {
      name: 'integrationSettings',
      title: 'Example plugin settings',
      type: 'uiExtension:integrationSettings',
      component: IntegrationSettings,
    },
    {
      name: 'integrationFormFields',
      title: 'Example plugin fields',
      type: 'uiExtension:integrationFormFields',
      component: IntegrationFormFields,
    },
    {
      name: 'example',
      title: 'Example plugin',
      type: 'uiExtension:settingsTab',
      component: ExamplePluginTab,
    },
  ],
});
