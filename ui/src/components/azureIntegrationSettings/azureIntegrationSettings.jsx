import { BtsAuthFieldsInfo } from 'components/btsAuthFieldsInfo';
import { hideModalAction } from 'components/modal';
import {
  URL_ATTRIBUTE_KEY,
  PROJECT_ATTRIBUTE_KEY,
  USERNAME_ATTRIBUTE_KEY,
  CHECKBOX_ATTRIBUTE_KEY,
} from 'components/constants';
import { AzureIntegrationFormFields } from '../azureIntegrationFormFields';

export const AzureIntegrationSettings = (props) => {
  const { data, goToPreviousPage, onUpdate, isGlobal, ...extensionProps } = props;
  const {
    lib: { React, useDispatch },
    actions: { showModalAction },
    components: { IntegrationSettings: IntegrationSettingsContainer },
  } = extensionProps;

  const dispatch = useDispatch();

  const authFieldsConfig = [
    {
      value: data.integrationParameters[URL_ATTRIBUTE_KEY],
      message: 'Link to BTS',
    },
    {
      value: data.integrationParameters[PROJECT_ATTRIBUTE_KEY],
      message: 'Project Name',
    },

    data.integrationParameters[USERNAME_ATTRIBUTE_KEY]
      ? {
          value: data.integrationParameters[USERNAME_ATTRIBUTE_KEY],
          message: 'Authorized by',
        }
      : {
          value: data.integrationParameters[CHECKBOX_ATTRIBUTE_KEY] ? 'Yes' : 'No',
          message: 'Allow users to post issues using this Token',
        },
  ];

  const getConfirmationFunc = () => (integrationData, integrationMetaData) => {
    onUpdate(
      integrationData,
      () => {
        dispatch(hideModalAction());
      },
      integrationMetaData,
    );
  };

  const editAuthorizationClickHandler = () => {
    const {
      data: { name, integrationParameters, integrationType },
    } = props;

    dispatch(
      showModalAction({
        id: 'addIntegrationModal',
        data: {
          onConfirm: getConfirmationFunc(),
          instanceType: integrationType.name,
          customProps: {
            initialData: {
              ...integrationParameters,
              integrationName: name,
            },
            editAuthMode: true,
          },
        },
      }),
    );
  };

  const getEditAuthConfig = () => ({
    content: <BtsAuthFieldsInfo fieldsConfig={authFieldsConfig} />,
    onClick: editAuthorizationClickHandler,
  });

  return (
    <IntegrationSettingsContainer
      data={data}
      onUpdate={onUpdate}
      goToPreviousPage={goToPreviousPage}
      editAuthConfig={getEditAuthConfig()}
      isGlobal={isGlobal}
      formFieldsComponent={(rest) => <AzureIntegrationFormFields {...extensionProps} {...rest} />}
    />
  );
};
