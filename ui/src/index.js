import { AzurePluginTab } from 'components/azurePluginTab';
import { AzureIntegrationFormFields } from './components/azureIntegrationFormFields';
import { AzureIntegrationSettings } from './components/azureIntegrationSettings';

window.RP.registerPlugin({
  name: 'Azure DevOps',
  extensions: [
    {
      name: 'integrationSettings',
      title: 'Azure DevOps plugin settings',
      type: 'uiExtension:integrationSettings',
      component: AzureIntegrationSettings,
    },
    {
      name: 'integrationFormFields',
      title: 'Azure DevOps plugin fields',
      type: 'uiExtension:integrationFormFields',
      component: AzureIntegrationFormFields,
    },
    {
      name: 'Azure DevOps',
      title: 'Azure DevOps',
      type: 'uiExtension:settingsTab',
      component: AzurePluginTab,
    },
  ],
});
