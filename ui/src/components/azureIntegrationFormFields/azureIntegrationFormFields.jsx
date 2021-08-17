import {
  URL_ATTRIBUTE_KEY,
  PROJECT_ATTRIBUTE_KEY,
  TOKEN_ATTRIBUTE_KEY,
} from 'components/constants';

const authTypeAttributesOptions = [{ value: true, label: 'ApiKey' }];

export const AzureIntegrationFormFields = (props) => {
  const { initialize, disabled, lineAlign, initialData, ...extensionProps } = props;
  const {
    lib: { React },
    components: { IntegrationFormField, FieldErrorHint, Input, InputTextArea, InputDropdown },
    validators: { requiredField, btsUrl, btsProject, btsIntegrationName },
  } = extensionProps;
  React.useEffect(() => {
    initialize(initialData);
  }, []);

  const [authTypeState, setAuthTypeState] = React.useState(!initialData[TOKEN_ATTRIBUTE_KEY]);

  const onChangeAuthTypeAttributesMode = (value) => {
    if (value === authTypeState) {
      return;
    }
    setAuthTypeState(value);

    if (!value) {
      props.change(TOKEN_ATTRIBUTE_KEY, '');
    }
  };

  return (
    <>
      <IntegrationFormField
        name="integrationName"
        disabled={disabled}
        label="Integration Name"
        required
        maxLength="55"
        validate={btsIntegrationName}
        lineAlign={lineAlign}
      >
        <FieldErrorHint>
          <Input mobileDisabled />
        </FieldErrorHint>
      </IntegrationFormField>
      <IntegrationFormField
        name={URL_ATTRIBUTE_KEY}
        disabled={disabled}
        label="Link to BTS"
        required
        validate={btsUrl}
        lineAlign={lineAlign}
      >
        <FieldErrorHint>
          <Input mobileDisabled />
        </FieldErrorHint>
      </IntegrationFormField>
      <IntegrationFormField
        name={PROJECT_ATTRIBUTE_KEY}
        disabled={disabled}
        label="Project name in BTS"
        required
        maxLength="55"
        validate={btsProject}
        lineAlign={lineAlign}
      >
        <FieldErrorHint>
          <Input mobileDisabled />
        </FieldErrorHint>
      </IntegrationFormField>
      <IntegrationFormField
        name="authType"
        disabled={disabled}
        label="Authorization type"
        lineAlign={lineAlign}
        withoutProvider
      >
        <FieldErrorHint>
          <InputDropdown
            mobileDisabled
            disabled={disabled}
            value={authTypeState}
            onChange={onChangeAuthTypeAttributesMode}
            options={authTypeAttributesOptions}
          />
        </FieldErrorHint>
      </IntegrationFormField>
      <IntegrationFormField
        name={TOKEN_ATTRIBUTE_KEY}
        disabled={disabled}
        label="Token"
        required
        validate={requiredField}
        lineAlign={lineAlign}
      >
        <FieldErrorHint>
          <InputTextArea type="text" mobileDisabled />
        </FieldErrorHint>
      </IntegrationFormField>
    </>
  );
};

AzureIntegrationFormFields.defaultProps = {
  initialData: {},
};
