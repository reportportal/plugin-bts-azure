import { AzurePluginTab } from 'components/azurePluginTab';
import { AzureIntegrationFormFields } from './components/azureIntegrationFormFields';
import { AzureIntegrationSettings } from './components/azureIntegrationSettings';

window.RP.registerPlugin({
  name: 'Azure',
  extensions: [
    {
      name: 'integrationSettings',
      title: 'Azure plugin settings',
      type: 'uiExtension:integrationSettings',
      component: AzureIntegrationSettings,
    },
    {
      name: 'integrationFormFields',
      title: 'Azure plugin fields',
      type: 'uiExtension:integrationFormFields',
      component: AzureIntegrationFormFields,
    },
    {
      name: 'Azure',
      title: 'Azure',
      type: 'uiExtension:settingsTab',
      component: AzurePluginTab,
    },
  ],
});
