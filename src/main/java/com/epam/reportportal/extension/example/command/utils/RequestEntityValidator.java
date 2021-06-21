package com.epam.reportportal.extension.example.command.utils;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class RequestEntityValidator {

	public static final String VALIDATION_EXCEPTION_DELIMITER = ", ";

	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

	private RequestEntityValidator() {
		//static only
	}

	public static <T> void validate(T entity) {
		final List<String> errors = VALIDATOR.validate(entity)
				.stream()
				.map(it -> it.getPropertyPath() + " " + it.getMessage())
				.collect(Collectors.toList());
		if (!errors.isEmpty()) {
			throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, String.join(VALIDATION_EXCEPTION_DELIMITER, errors));
		}
	}
}
