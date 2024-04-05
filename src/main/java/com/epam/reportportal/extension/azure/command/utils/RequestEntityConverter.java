package com.epam.reportportal.extension.azure.command.utils;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class RequestEntityConverter {

  private final ObjectMapper objectMapper;

  public RequestEntityConverter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public <T> T getEntity(String key, Map<String, Object> params, Class<T> clazz) {
    return ofNullable(params.get(key)).map(entity -> {
      try {
        return objectMapper.readValue(objectMapper.writeValueAsString(entity), clazz);
      } catch (JsonProcessingException e) {
        throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, e.getMessage());
      }
    }).orElseThrow(() -> new ReportPortalException(
        ErrorType.BAD_REQUEST_ERROR,
        Suppliers.formattedSupplier("Parameter '{}' was not provided", key).get()
    ));
  }
}
