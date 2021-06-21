export const IntegrationFormFields = (props) => {
  const {
    initialize,
    disabled,
    lineAlign,
    initialData,
    ...extensionProps
  } = props;
  const {
    lib: { React },
    components: { IntegrationFormField, FieldErrorHint, Input },
    validators: { requiredField }
  } = extensionProps;
  React.useEffect(() => {
    initialize(initialData);
  }, []);

  return (
    <>
      <IntegrationFormField
        name="integrationName"
        disabled={disabled}
        label="Integration Name"
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
        label="URL"
        validate={requiredField}
        lineAlign={lineAlign}
      >
        <FieldErrorHint>
          <Input mobileDisabled />
        </FieldErrorHint>
      </IntegrationFormField>
    </>
  );
};
