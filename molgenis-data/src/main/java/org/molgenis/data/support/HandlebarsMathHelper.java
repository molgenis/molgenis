package org.molgenis.data.support;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Handlebars Math Helper
 *
 * based on: https://github.com/mrhanlon/handlebars-math-helper/
 *
 * changes:
 *  - Some refactoring
 *  - Remove deprecated functions
 *  - Added some extra unit tests
 *
 */

public class HandlebarsMathHelper implements Helper<Object> {
  @Override
  public CharSequence apply(final Object value, Options options) throws IOException, IllegalArgumentException {
    if (options.params.length >= 2) {
      return calculateResult(value, options);
    } else {
      throw new IOException("MathHelper requires three parameters");
    }
  }

  private String calculateResult(Object value, Options options) {
    Operator operator = Operator.fromSymbol(options.param(0).toString());
    if (operator == null) {
      throw new IllegalArgumentException("Unknown operation '" + options.param(0) + "'");
    }

    if (value == null || options.param(1) == null) {
      throw new IllegalArgumentException("Cannot perform operations on null values");
    }

    BigDecimal firstValue = new BigDecimal(value.toString());
    BigDecimal secondValue = new BigDecimal(options.param(1).toString());
    MathContext mathContext = new MathContext(16, RoundingMode.HALF_UP);

    BigDecimal result = switch (operator) {
      case add -> firstValue.add(secondValue, mathContext);
      case subtract -> firstValue.subtract(secondValue, mathContext);
      case multiply -> firstValue.multiply(secondValue, mathContext);
      case divide -> firstValue.divide(secondValue, mathContext);
      case mod -> firstValue.remainder(secondValue, mathContext);
    };
    return result.toString();
  }

  public enum Operator {
    add("+"),
    subtract("-"),
    multiply("*"),
    divide("/"),
    mod("%");

    Operator(String symbol) {
    }

    public static Operator fromSymbol(String symbol) {
      return switch (symbol) {
        case "+" -> add;
        case "-" -> subtract;
        case "*" -> multiply;
        case "/" -> divide;
        case "%" -> mod;
        default -> null;
      };
    }
  }
}