import { AzureIntegrationFormFields } from '../azureIntegrationFormFields';

export const AzureIntegrationSettings = (props) => {
  const { data, goToPreviousPage, onUpdate, isGlobal, ...extensionProps } = props;
  const {
    lib: { React },
    components: { IntegrationSettings: IntegrationSettingsContainer },
  } = extensionProps;

  return (
    <IntegrationSettingsContainer
      data={data}
      onUpdate={onUpdate}
      goToPreviousPage={goToPreviousPage}
      isGlobal={isGlobal}
      formFieldsComponent={(rest) => <AzureIntegrationFormFields {...extensionProps} {...rest} />}
    />
  );
};
