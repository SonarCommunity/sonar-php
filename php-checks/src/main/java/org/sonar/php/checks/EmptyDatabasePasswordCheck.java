/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerTree;
import org.sonar.plugins.php.api.tree.expression.ArrayPairTree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.php.checks.utils.CheckUtils.trimQuotes;

@Rule(key = EmptyDatabasePasswordCheck.KEY)
public class EmptyDatabasePasswordCheck extends PHPVisitorCheck {

  public static final String KEY = "S2115";

  private static final String MESSAGE = "Add password protection to this database.";

  @Override
  public void visitFunctionCall(FunctionCallTree functionCall) {
    String functionName = CheckUtils.getLowerCaseFunctionName(functionCall);
    if ("mysqli".equals(functionName) || "mysqli_connect".equals(functionName) || "PDO".equalsIgnoreCase(functionName)) {
      checkPasswordArgument(functionCall, "passwd", 2);
    } else if ("oci_connect".equals(functionName)) {
      checkPasswordArgument(functionCall, "password", 1);
    } else if ("sqlsrv_connect".equals(functionName)) {
      checkSqlServer(functionCall);
    } else if ("pg_connect".equals(functionName)) {
      checkPostgresql(functionCall);
    }
    super.visitFunctionCall(functionCall);
  }

  private void checkPasswordArgument(FunctionCallTree functionCall, String argumentName, int argumentIndex) {
    Optional<CallArgumentTree> argument = CheckUtils.argument(functionCall, argumentName, argumentIndex);
    if (argument.isPresent()) {
      ExpressionTree passwordArgument = argument.get().value();
      if (hasEmptyValue(passwordArgument)) {
        context().newIssue(this, passwordArgument, MESSAGE);
      }
    }
  }

  private static boolean isEmptyLiteral(ExpressionTree expression) {
    if (expression.is(Kind.REGULAR_STRING_LITERAL)) {
      LiteralTree literal = (LiteralTree) expression;
      return literal.value().length() == 2;
    }
    return false;
  }

  private boolean hasEmptyValue(ExpressionTree expression) {
    if (isEmptyLiteral(expression)) {
      return true;
    } else if (expression.is(Kind.VARIABLE_IDENTIFIER)) {
      return CheckUtils.uniqueAssignedValue((VariableIdentifierTree) expression)
        .map(EmptyDatabasePasswordCheck::isEmptyLiteral)
        .orElse(false);
    }
    return false;
  }

  private void checkSqlServer(FunctionCallTree functionCall) {
    Optional<CallArgumentTree> argument = CheckUtils.argument(functionCall, "connectionInfo", 1);
    if (argument.isPresent()) {
      ExpressionTree connectionInfo = argument.get().value();
      ExpressionTree password = sqlServerPassword(connectionInfo);
      if (password != null && hasEmptyValue(password)) {
        context().newIssue(this, password, MESSAGE);
      }
    }
  }

  private ExpressionTree sqlServerPassword(ExpressionTree connectionInfo) {
    if (connectionInfo.is(Kind.ARRAY_INITIALIZER_FUNCTION, Kind.ARRAY_INITIALIZER_BRACKET)) {
      for (ArrayPairTree arrayPairTree : ((ArrayInitializerTree) connectionInfo).arrayPairs()) {
        ExpressionTree key = arrayPairTree.key();
        if (key != null && key.is(Kind.REGULAR_STRING_LITERAL) && "PWD".equals(trimQuotes((LiteralTree) key))) {
          return arrayPairTree.value();
        }
      }
      return null;
    } else if (connectionInfo.is(Kind.VARIABLE_IDENTIFIER)) {
      return CheckUtils.uniqueAssignedValue((VariableIdentifierTree) connectionInfo)
        .map(this::sqlServerPassword)
        .orElse(null);
    }
    return null;
  }

  private void checkPostgresql(FunctionCallTree functionCall) {
    Optional<CallArgumentTree> connectionStringArgument = CheckUtils.argument(functionCall, "connection_string", 0);
    if (!connectionStringArgument.isPresent()) {
      return;
    }
    ExpressionTree connectionString = connectionStringArgument.get().value();
    if (connectionString.is(Kind.VARIABLE_IDENTIFIER)) {
      connectionString = CheckUtils.uniqueAssignedValue((VariableIdentifierTree) connectionString).orElse(connectionString);
    }
    checkPostgresqlConnectionString(connectionString);
  }

  private void checkPostgresqlConnectionString(ExpressionTree connectionString) {
    List<ExpressionTree> concatenationOperands = new ArrayList<>();
    if (connectionString.is(Kind.CONCATENATION)) {
      concatenationOperands(connectionString, concatenationOperands);
    } else {
      concatenationOperands.add(connectionString);
    }

    ExpressionTree connectionStringLastPart = concatenationOperands.get(concatenationOperands.size() - 1);
    Pattern noPasswordPattern = Pattern.compile(".*password\\s*=\\s*");
    Pattern emptyPasswordPattern = Pattern.compile(noPasswordPattern.pattern() + "''.*");

    if (concatenationOperands.stream().anyMatch(e -> isStringLiteralMatching(emptyPasswordPattern, e))
      || isStringLiteralMatching(noPasswordPattern, connectionStringLastPart)) {
      context().newIssue(this, connectionString, MESSAGE);
    }
  }

  private static boolean isStringLiteralMatching(Pattern pattern, ExpressionTree expressionTree) {
    if (expressionTree.is(Kind.REGULAR_STRING_LITERAL)) {
      return pattern.matcher(trimQuotes((LiteralTree) expressionTree)).matches();
    }
    return false;
  }

  private static void concatenationOperands(ExpressionTree expression, List<ExpressionTree> operands) {
    if (expression.is(Kind.CONCATENATION)) {
      BinaryExpressionTree binary = (BinaryExpressionTree) expression;
      concatenationOperands(binary.leftOperand(), operands);
      concatenationOperands(binary.rightOperand(), operands);
    } else {
      operands.add(expression);
    }
  }

}
