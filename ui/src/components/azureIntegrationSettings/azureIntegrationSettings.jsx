import { BtsAuthFieldsInfo } from 'components/btsAuthFieldsInfo';
import { hideModalAction } from 'components/modal';
import { URL_ATTRIBUTE_KEY, PROJECT_ATTRIBUTE_KEY } from 'components/constants';

export const AzureIntegrationSettings = (props) => {
  const { data, goToPreviousPage, onUpdate, isGlobal, ...extensionProps } = props;
  const {
    lib: { React, useDispatch },
    actions: { showModalAction },
    components: { IntegrationSettings: IntegrationSettingsContainer, BtsPropertiesForIssueForm },
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
  ];

  const getConfirmationFunc = (testConnection) => (integrationData, integrationMetaData) => {
    onUpdate(
      integrationData,
      () => {
        dispatch(hideModalAction());
        testConnection();
      },
      integrationMetaData,
    );
  };

  const editAuthorizationClickHandler = (testConnection) => {
    const {
      data: { name, integrationParameters, integrationType },
    } = props;

    dispatch(
      showModalAction({
        id: 'addIntegrationModal',
        data: {
          onConfirm: getConfirmationFunc(testConnection),
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

  const getDefectFormFields = (fields, checkedFieldsIds, values) =>
    fields
      .filter((item) => item.required || checkedFieldsIds[item.id])
      .map((item) => ({ ...item, value: values[item.id] }));

  const getEditAuthConfig = () => ({
    content: <BtsAuthFieldsInfo fieldsConfig={authFieldsConfig} />,
    onClick: editAuthorizationClickHandler,
  });

  const onSubmit = (integrationData, callback, metaData) => {
    const { fields, checkedFieldsIds = {}, ...meta } = metaData;
    const defectFormFields = getDefectFormFields(fields, checkedFieldsIds, integrationData);

    props.onUpdate({ defectFormFields }, callback, meta);
  };

  return (
    <IntegrationSettingsContainer
      data={data}
      onUpdate={onSubmit}
      goToPreviousPage={goToPreviousPage}
      editAuthConfig={getEditAuthConfig()}
      isGlobal={isGlobal}
      formFieldsComponent={BtsPropertiesForIssueForm}
      formKey="BTS_FIELDS_FORM"
      isEmptyConfiguration={
        !data.integrationParameters.defectFormFields ||
        !data.integrationParameters.defectFormFields.length
      }
    />
  );
};
