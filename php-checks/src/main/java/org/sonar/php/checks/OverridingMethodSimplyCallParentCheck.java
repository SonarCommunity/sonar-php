/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.checks;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.symbols.MethodSymbol;
import org.sonar.php.symbols.Parameter;
import org.sonar.php.symbols.Symbols;
import org.sonar.php.tree.symbols.HasClassSymbol;
import org.sonar.php.tree.symbols.HasMethodSymbol;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = OverridingMethodSimplyCallParentCheck.KEY)
public class OverridingMethodSimplyCallParentCheck extends PHPVisitorCheck {

  public static final String KEY = "S1185";
  private static final String MESSAGE = "Remove this method \"%s\" to simply inherit it.";

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    super.visitClassDeclaration(tree);
    visitClass(tree);
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    super.visitAnonymousClass(tree);
    visitClass(tree);
  }

  private void visitClass(ClassTree tree) {
    if (tree.superClass() != null) {
      ClassSymbol superClassSymbol = ((HasClassSymbol) tree).symbol().superClass().get();
      for (ClassMemberTree member : tree.members()) {
        if (member.is(Kind.METHOD_DECLARATION)) {
          checkMethod((MethodDeclarationTree) member, superClassSymbol);
        }
      }
    }
  }

  private void checkMethod(MethodDeclarationTree method, ClassSymbol superClass) {
    if (method.body().is(Kind.BLOCK)) {
      BlockTree blockTree = (BlockTree) method.body();

      if (blockTree.statements().size() == 1) {
        StatementTree statementTree = blockTree.statements().get(0);

        ExpressionTree expressionTree = null;
        if (statementTree.is(Kind.EXPRESSION_STATEMENT)) {
          expressionTree = ((ExpressionStatementTree) statementTree).expression();

        } else if (statementTree.is(Kind.RETURN_STATEMENT)) {
          expressionTree = ((ReturnStatementTree) statementTree).expression();
        }

        checkExpression(expressionTree, method, superClass);
      }
    }
  }

  private void checkExpression(@Nullable ExpressionTree expressionTree, MethodDeclarationTree method, ClassSymbol superClass) {
    if (expressionTree != null && expressionTree.is(Kind.FUNCTION_CALL)) {
      FunctionCallTree functionCallTree = (FunctionCallTree) expressionTree;

      if (functionCallTree.callee().is(Kind.CLASS_MEMBER_ACCESS)) {
        MemberAccessTree memberAccessTree = (MemberAccessTree) functionCallTree.callee();

        String methodName = method.name().text();
        boolean sameMethodName = memberAccessTree.member().toString().equals(methodName);

        MethodSymbol correspondingMethodFromSuper = superClass.declaredMethods().stream()
          .filter(ms -> ms.name().equalsIgnoreCase(methodName) && hasSameParameterList(method, ms))
          .findFirst()
          .orElse(null);
        // doesn't check `__construct`
        // MethodSymbol ms2 = ((MethodSymbolImpl) Symbols.get(method)).getBaseMethod();
        boolean shouldCheckMethodFromSuper = !superClass.isUnknownSymbol() && correspondingMethodFromSuper != null;

        if (isSuperClassReference(memberAccessTree.object(), superClass.qualifiedName().toString()) &&
          sameMethodName &&
          isFunctionCalledWithSameArgumentsAsDeclared(functionCallTree, method) &&
          (superClass.isUnknownSymbol() || correspondingMethodFromSuper != null &&
            hasSameVisibilityAs(method, correspondingMethodFromSuper) &&
            hasSameParameterList(method, correspondingMethodFromSuper))) {
          String message = String.format(MESSAGE, methodName);
          context().newIssue(this, method.name(), message);
        }
      }
    }
  }

  private boolean hasSameParameterList(MethodDeclarationTree method, MethodSymbol other) {
    if (Symbols.get(method).isUnknownSymbol() || other.isUnknownSymbol()) {
      return false;
    }

    List<Parameter> parameters = ((HasMethodSymbol) method).symbol().parameters();
    List<Parameter> parametersFromSuper = other.parameters();
    if (parameters.size() != parametersFromSuper.size()) {
      return false;
    }
    for (int i = 0; i < parameters.size(); ++i) {
      if (!parameters.get(i).equals(parametersFromSuper.get(i))) {
        return false;
      }
    }
    return true;
  }

  private boolean hasSameVisibilityAs(MethodDeclarationTree method, MethodSymbol other) {
    if (Symbols.get(method).isUnknownSymbol() || other.isUnknownSymbol()) {
      return false;
    }

    return Symbols.get(method).visibility().equals(other.visibility());
  }

  private static boolean isFunctionCalledWithSameArgumentsAsDeclared(FunctionCallTree functionCallTree, MethodDeclarationTree method) {
    List<String> argumentNames = new ArrayList<>();
    for (CallArgumentTree argument : functionCallTree.callArguments()) {
      if (!argument.value().is(Kind.VARIABLE_IDENTIFIER) || argument.name() != null) {
        return false;
      }
      argumentNames.add(((VariableIdentifierTree) argument.value()).variableExpression().text());
    }

    List<String> parameterNames = new ArrayList<>();
    for (ParameterTree parameter : method.parameters().parameters()) {
      if (parameter.initValue() != null) {
        return false;
      }
      parameterNames.add(parameter.variableIdentifier().variableExpression().text());
    }

    return argumentNames.equals(parameterNames);
  }

  private static boolean isSuperClassReference(ExpressionTree tree, String superClass) {
    String str = tree.toString();
    return superClass.equalsIgnoreCase(str) || "parent".equalsIgnoreCase(str);
  }

}
