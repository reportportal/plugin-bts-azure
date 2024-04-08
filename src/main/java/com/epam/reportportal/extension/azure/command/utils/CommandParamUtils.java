package com.epam.reportportal.extension.azure.command.utils;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Path;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class CommandParamUtils {

  public static final String ID_PARAM = "id";
  public static final String PROJECT_ID_PARAM = "projectId";
  public static final String ENTITY_PARAM = "entity";
  public static final String URL_PARAM = "url";

  private CommandParamUtils() {
    //static only
  }

  public static Long retrieveLong(Map<String, Object> params, String param) {
    return ofNullable(params.get(param)).map(String::valueOf).map(CommandParamUtils::safeParseLong)
        .orElseThrow(() -> new ReportPortalException(
            ErrorType.BAD_REQUEST_ERROR,
            Suppliers.formattedSupplier("Parameter '{}' was not provided", param).get()
        ));
  }

  public static Long safeParseLong(String param) {
    try {
      return Long.parseLong(param);
    } catch (NumberFormatException ex) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, ex.getMessage());
    }
  }

  public static void handleValidatorConstraints(Set<ConstraintViolation<?>> constraintViolations) {
    if (constraintViolations != null && !constraintViolations.isEmpty()) {
      StringBuilder messageBuilder = new StringBuilder();
      for (ConstraintViolation<?> constraintViolation : constraintViolations) {
        messageBuilder.append("[");
        messageBuilder.append("Incorrect value in request '");
        messageBuilder.append(constraintViolation.getInvalidValue());
        messageBuilder.append("' in field '");
        Iterator<Path.Node> iterator = constraintViolation.getPropertyPath().iterator();
        messageBuilder.append(iterator.hasNext() ? iterator.next().getName() : "");
        messageBuilder.append("'.]");
      }
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, messageBuilder.toString());
    }
  }
}
