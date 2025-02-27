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
package org.sonar.php.metrics;

import com.sonar.sslr.api.typed.ActionParser;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

class ComplexityVisitorTest {

  private final ActionParser<Tree> parser = PHPParserBuilder.createParser(PHPLexicalGrammar.TOP_STATEMENT);

  @Test
  void testDeclarations() {
    assertOneComplexityToken("function f() {}", "function");
    assertOneComplexityToken("$f = function() {};", "function");
    assertOneComplexityToken("$f = fn() => 0;", "fn");
    assertThat(complexity("class A {}")).isZero();
    assertOneComplexityToken("class A { public function f() {} }", "function");

    assertThat(complexity("function f() { f(); return; }")).isEqualTo(1);
    assertThat(complexity("function f() { return; f(); }")).isEqualTo(1);
    assertThat(complexity("class A { abstract function f(); }")).isEqualTo(1);
    assertThat(expressionComplexity("function() { return $a && $b; };")).isEqualTo(2);
    assertThat(expressionComplexity("fn() => $a && $b;")).isEqualTo(2);
  }

  @Test
  void testStatements() {
    assertThat(complexity("$a = 0;")).isZero();
    assertOneComplexityToken("if ($a) {}", "if");
    assertOneComplexityToken("if ($a) {} else {}", "if");
    assertThat(complexity("if ($a) {} else if($b) {} else {}")).isEqualTo(2);
    assertThat(complexity("if ($a) {} elseif($b) {} else {}")).isEqualTo(2);
    assertOneComplexityToken("if ($a): endif;", "if");
    assertOneComplexityToken("for (;;) {}", "for");
    assertOneComplexityToken("foreach ($a as $b) {}", "foreach");
    assertOneComplexityToken("while ($a) {}", "while");
    assertOneComplexityToken("do {} while($a);", "do");
    assertThat(complexity("switch ($a) {}")).isZero();
    assertOneComplexityToken("switch ($a) {case 1:}", "case");
    assertThat(complexity("switch ($a) {default:}")).isZero();

    assertThat(complexity("try {}")).isZero();
    assertThat(complexity("try {} catch(E $s) {}")).isZero();
    assertThat(complexity("return 1;")).isZero();
    assertThat(complexity("throw e;")).isZero();
    assertThat(complexity("goto x;")).isZero();
  }

  @Test
  void testExpressions() {
    assertThat(complexity("$a;")).isZero();
    assertThat(complexity("$a + $b;")).isZero();
    assertThat(complexity("$f();")).isZero();

    assertOneComplexityToken("$a || $b;", "||");
    assertOneComplexityToken("$a && $b;", "&&");
    assertOneComplexityToken("$a or $b;", "or");
    assertOneComplexityToken("$a and $b;", "and");
    assertOneComplexityToken("$a ? $b : $c;", "?");
  }

  @Test
  void testWithoutNestedFunctions() {
    assertThat(complexityWithoutNestedFunctions("$a && $b && $c;")).isEqualTo(2);
    assertThat(complexityWithoutNestedFunctions("$a && f(function () { return $a && $b; });")).isEqualTo(1);
    assertThat(complexityWithoutNestedFunctions("$a && f(fn() => $a && $b);")).isEqualTo(1);
    assertThat(complexityWithoutNestedFunctions("function f() { f(function () { return $a && $b; }); $a && $b; }")).isEqualTo(2);
    assertThat(complexityWithoutNestedFunctions("function f() { f(fn() => $a && $b); $a && $b; }")).isEqualTo(2);
    assertThat(complexityWithoutNestedFunctions("function() { f(fn() => $a && $b); $a && $b; };")).isEqualTo(2);
    assertThat(complexityWithoutNestedFunctions("fn() => f(fn() => $a && $b) && $b;")).isEqualTo(2);
  }

  private void assertOneComplexityToken(String codeToParse, String complexityToken) {
    Tree tree = parser.parse(codeToParse);
    List<Tree> trees = ComplexityVisitor.complexityTrees(tree);

    assertThat(trees).hasSize(1);
    assertThat(((SyntaxToken) trees.get(0)).text()).isEqualTo(complexityToken);
  }

  private int complexity(String toParse) {
    Tree tree = parser.parse(toParse);
    return ComplexityVisitor.complexity(tree);
  }

  private int expressionComplexity(String toParse) {
    ExpressionStatementTree tree = (ExpressionStatementTree) parser.parse(toParse);
    return ComplexityVisitor.complexity(tree.expression());
  }

  private int complexityWithoutNestedFunctions(String toParse) {
    Tree tree = parser.parse(toParse);
    if (tree.is(Tree.Kind.EXPRESSION_STATEMENT)) {
      tree = ((ExpressionStatementTree) tree).expression();
    }
    return ComplexityVisitor.complexityNodesWithoutNestedFunctions(tree).size();
  }

}
