import {
  USERNAME_ATTRIBUTE_KEY,
  PASSWORD_ATTRIBUTE_KEY,
  TOKEN_ATTRIBUTE_KEY,
} from 'components/constants';

const authTypeAttributesOptions = [
  { value: true, label: 'Basic' },
  { value: false, label: 'ApiKey' },
];

export const AzureIntegrationFormFields = (props) => {
  const { initialize, disabled, lineAlign, initialData, ...extensionProps } = props;
  const {
    lib: { React },
    components: {
      IntegrationFormField,
      FieldErrorHint,
      Input,
      InputTextArea,
      InputDropdown,
      InputCheckbox,
    },
    validators: { requiredField },
  } = extensionProps;
  React.useEffect(() => {
    initialize(initialData);
  }, []);

  const [checked, setChecked] = React.useState(true);

  const [authTypeState, setAuthTypeState] = React.useState(true);

  const onChangeAuthTypeAttributesMode = (value) => {
    if (value === authTypeState) {
      return;
    }
    setAuthTypeState(value);

    if (value) {
      props.change(TOKEN_ATTRIBUTE_KEY, '');
    } else {
      props.change(USERNAME_ATTRIBUTE_KEY, '');
      props.change(PASSWORD_ATTRIBUTE_KEY, '');
    }
  };

  return (
    <>
      <IntegrationFormField
        name="integrationName"
        disabled={disabled}
        label="Integration Name"
        required
        validate={requiredField}
        lineAlign={lineAlign}
      >
        <FieldErrorHint>
          <Input mobileDisabled />
        </FieldErrorHint>
      </IntegrationFormField>
      <IntegrationFormField
        name="url"
        disabled={disabled}
        label="Link to BTS"
        required
        validate={requiredField}
        lineAlign={lineAlign}
      >
        <FieldErrorHint>
          <Input mobileDisabled />
        </FieldErrorHint>
      </IntegrationFormField>
      <IntegrationFormField
        name="project"
        disabled={disabled}
        label="Project name in BTS"
        required
        validate={requiredField}
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
      {authTypeState ? (
        <>
          <IntegrationFormField
            name={USERNAME_ATTRIBUTE_KEY}
            disabled={disabled}
            label="BTS Username"
            required
            validate={requiredField}
            lineAlign={lineAlign}
          >
            <FieldErrorHint>
              <Input type="text" mobileDisabled />
            </FieldErrorHint>
          </IntegrationFormField>
          <IntegrationFormField
            name={PASSWORD_ATTRIBUTE_KEY}
            disabled={disabled}
            label="BTS Password"
            required
            validate={requiredField}
            lineAlign={lineAlign}
          >
            <FieldErrorHint>
              <Input type="password" mobileDisabled />
            </FieldErrorHint>
          </IntegrationFormField>
        </>
      ) : (
        <>
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
          <IntegrationFormField name="postAllowed" disabled={disabled} lineAlign={lineAlign}>
            <InputCheckbox mobileDisabled value={checked} onChange={() => setChecked(!checked)}>
              Allow users to post issues using this Token
            </InputCheckbox>
          </IntegrationFormField>
        </>
      )}
    </>
  );
};
