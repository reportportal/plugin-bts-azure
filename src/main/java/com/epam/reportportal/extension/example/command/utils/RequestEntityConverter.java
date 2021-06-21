package com.epam.reportportal.extension.example.command.utils;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

import static java.util.Optional.ofNullable;

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
		})
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
						Suppliers.formattedSupplier("Parameter '{}' was not provided", key).get()
				));
	}
}
