package com.epam.reportportal.extension.azure.service.utils;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.JSONB;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class JsonbConverter {

  private final ObjectMapper objectMapper;

  public JsonbConverter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public <T> T convert(JSONB jsonb, Class<T> clazz) {
    try {
      return objectMapper.readValue(jsonb.data(), clazz);
    } catch (JsonProcessingException e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, e.getMessage());
    }
  }
}
