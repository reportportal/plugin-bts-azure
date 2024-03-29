import React, { useEffect, useState } from 'react';
import {
  URL_ATTRIBUTE_KEY,
  PROJECT_ATTRIBUTE_KEY,
  TOKEN_ATTRIBUTE_KEY,
} from 'components/constants';

const authTypeAttributesOptions = [{ value: 'OAUTH', label: 'ApiKey' }];
const DEFAULT_FORM_CONFIG = {
  authType: 'OAUTH',
};

export const IntegrationFormFields = (props) => {
  const { initialize, disabled, initialData, ...extensionProps } = props;
  const {
    components: { FieldErrorHint, FieldElement, FieldText, Dropdown, FieldTextFlex },
    validators: { requiredField, btsUrl, btsProjectKey, btsIntegrationName },
  } = extensionProps;
  useEffect(() => {
    initialize(initialData);
  }, []);

  const [authTypeState, setAuthTypeState] = useState(!initialData[TOKEN_ATTRIBUTE_KEY]);

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
      <FieldElement
        name="integrationName"
        label="Integration Name"
        validate={btsIntegrationName}
        disabled={disabled}
        isRequired
      >
        <FieldErrorHint provideHint={false}>
          <FieldText defaultWidth={false} />
        </FieldErrorHint>
      </FieldElement>
      <FieldElement
        name={URL_ATTRIBUTE_KEY}
        label="Link to BTS"
        validate={btsUrl}
        disabled={disabled}
        isRequired
      >
        <FieldErrorHint provideHint={false}>
          <FieldText defaultWidth={false} />
        </FieldErrorHint>
      </FieldElement>
      <FieldElement
        name={PROJECT_ATTRIBUTE_KEY}
        label="Project key in BTS"
        validate={btsProjectKey}
        disabled={disabled}
        isRequired
      >
        <FieldErrorHint provideHint={false}>
          <FieldText defaultWidth={false} />
        </FieldErrorHint>
      </FieldElement>
      <FieldElement name="authType" disabled={disabled} label="Authorization type">
        <FieldErrorHint provideHint={false}>
          <Dropdown
            disabled={disabled}
            value={authTypeState}
            onChange={onChangeAuthTypeAttributesMode}
            options={authTypeAttributesOptions}
            defaultWidth={false}
          />
        </FieldErrorHint>
      </FieldElement>
      <FieldElement
        name={TOKEN_ATTRIBUTE_KEY}
        disabled={disabled}
        label="Token"
        validate={requiredField}
        isRequired
      >
        <FieldErrorHint provideHint={false}>
          <FieldTextFlex />
        </FieldErrorHint>
      </FieldElement>
    </>
  );
};
IntegrationFormFields.defaultProps = {
  initialData: DEFAULT_FORM_CONFIG,
};
