import { ExamplePluginTab } from 'components/examplePluginTab';
import { IntegrationFormFields } from './components/integrationFormFields';
import { IntegrationSettings } from './components/integrationSettings';

window.RP.registerPlugin({
  name: 'Azure',
  extensions: [
    {
      name: 'integrationSettings',
      title: 'Azure plugin settings',
      type: 'uiExtension:integrationSettings',
      component: IntegrationSettings,
    },
    {
      name: 'integrationFormFields',
      title: 'Azure plugin fields',
      type: 'uiExtension:integrationFormFields',
      component: IntegrationFormFields,
    },
    {
      name: 'Azure',
      title: 'Azure',
      type: 'uiExtension:settingsTab',
      component: ExamplePluginTab,
    },
  ],
});
