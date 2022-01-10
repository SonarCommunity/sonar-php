/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2022 SonarSource SA
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
package org.sonar.php.checks.utils;

import com.sonar.sslr.api.typed.ActionParser;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.Test;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.tree.impl.VariableIdentifierTreeImpl;
import org.sonar.php.tree.impl.expression.LiteralTreeImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.tree.symbols.SymbolImpl;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonarsource.analyzer.commons.checks.coverage.UtilityClass;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.php.checks.utils.CheckUtils.argument;
import static org.sonar.php.checks.utils.CheckUtils.functionName;
import static org.sonar.php.checks.utils.CheckUtils.isMethodInheritedFromClassOrInterface;
import static org.sonar.php.checks.utils.CheckUtils.isStringLiteralWithValue;
import static org.sonar.php.checks.utils.CheckUtils.lowerCaseFunctionName;
import static org.sonar.php.checks.utils.CheckUtils.trimQuotes;

public class CheckUtilsTest {

  private ActionParser<Tree> parser = PHPParserBuilder.createParser(PHPLexicalGrammar.TOP_STATEMENT);

  @Test
  public void utility_class() throws Exception {
    UtilityClass.assertGoodPractice(CheckUtils.class);
  }

  @Test
  public void skipParenthesis() throws Exception {
    ExpressionTree expr;

    expr = expressionFromStatement("42;");
    assertThat(CheckUtils.skipParenthesis(expr)).isEqualTo(expr);

    expr = expressionFromStatement("(42);");
    assertThat(CheckUtils.skipParenthesis(expr)).isEqualTo(((ParenthesisedExpressionTree) expr).expression());

    expr = expressionFromStatement("((((((42))))));");
    assertThat(CheckUtils.skipParenthesis(expr).is(Tree.Kind.NUMERIC_LITERAL)).isTrue();
    assertThat(((LiteralTree) CheckUtils.skipParenthesis(expr)).value()).isEqualTo("42");
  }

  @Test
  public void function_name() throws Exception {
    ExpressionTree root = expressionFromStatement("A::run(2);");
    assertThat(root.is(Tree.Kind.FUNCTION_CALL)).isTrue();
    FunctionCallTree call = (FunctionCallTree) root;
    assertThat(CheckUtils.getFunctionName(call)).isEqualTo("A::run");
    assertThat(CheckUtils.getLowerCaseFunctionName(call)).isEqualTo("a::run");
    assertThat(CheckUtils.getLowerCaseFunctionName((FunctionCallTree)expressionFromStatement("$var(2);"))).isNull();

    root = ((ExpressionStatementTree) parseMethodStatement("$this->run(2);")).expression();
    assertThat(root.is(Tree.Kind.FUNCTION_CALL)).isTrue();
    call = (FunctionCallTree) root;
    assertThat(CheckUtils.getFunctionName(call)).isEqualTo("Wrapper::run");

    root = ((ExpressionStatementTree) parseMethodStatement("$foo->run(2);")).expression();
    assertThat(root.is(Tree.Kind.FUNCTION_CALL)).isTrue();
    call = (FunctionCallTree) root;
    assertThat(CheckUtils.getFunctionName(call)).isNull();
  }

  @Test
  public void no_function_name() throws Exception {
    ExpressionTree root = expressionFromStatement("$name(2);");
    assertThat(root.is(Tree.Kind.FUNCTION_CALL)).isTrue();
    FunctionCallTree call = (FunctionCallTree) root;
    assertThat(CheckUtils.getFunctionName(call)).isNull();
  }

  @Test
  public void for_condition() throws Exception {
    Tree tree = PHPParserBuilder.createParser().parse("<?= for(;;) {} ?>");
    ForStatementTree forStatement = (ForStatementTree) ((CompilationUnitTree) tree).script().statements().get(0);
    assertThat(CheckUtils.getForCondition(forStatement)).isNull();

    tree = PHPParserBuilder.createParser().parse("<?= for(;true;) {} ?>");
    forStatement = (ForStatementTree) ((CompilationUnitTree) tree).script().statements().get(0);
    assertThat(CheckUtils.getForCondition(forStatement).getKind()).isEqualTo(Tree.Kind.BOOLEAN_LITERAL);

    tree = PHPParserBuilder.createParser().parse("<?= for(;$a == 0, true;) {} ?>");
    forStatement = (ForStatementTree) ((CompilationUnitTree) tree).script().statements().get(0);
    assertThat(CheckUtils.getForCondition(forStatement).getKind()).isEqualTo(Tree.Kind.BOOLEAN_LITERAL);
  }

  @Test
  public void trim_quotes() throws Exception {
    assertThat(trimQuotes("")).isEqualTo("");
    assertThat(trimQuotes("'")).isEqualTo("'");
    assertThat(trimQuotes("''")).isEqualTo("");
    assertThat(trimQuotes("\"\"")).isEqualTo("");
    assertThat(trimQuotes("\"abc\"")).isEqualTo("abc");
    assertThat(trimQuotes("'abc'")).isEqualTo("abc");
    assertThat(trimQuotes("abc")).isEqualTo("abc");
  }

  @Test
  public void trim_quotes_literal() {
    assertThat(trimQuotes((LiteralTree) expressionFromStatement("\"abc\";"))).isEqualTo("abc");
    assertThat(trimQuotes((LiteralTree) expressionFromStatement("'abc';"))).isEqualTo("abc");
    assertThat(trimQuotes((LiteralTree) expressionFromStatement("'';"))).isEqualTo("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void trim_quotes_literal_on_non_string() {
    trimQuotes((LiteralTree) expressionFromStatement("42;"));
  }

  @Test
  public void is_false_value() throws Exception {
    assertThat(createLiterals(Tree.Kind.BOOLEAN_LITERAL, "false", "False", "FALSE")
      .allMatch(CheckUtils::isFalseValue)).isTrue();

    assertThat(createLiterals(Tree.Kind.NUMERIC_LITERAL, "0", "0.0")
      .allMatch(CheckUtils::isFalseValue)).isTrue();

    assertThat(createLiterals(Tree.Kind.REGULAR_STRING_LITERAL, "\"0\"", "'0'", "''")
      .allMatch(CheckUtils::isFalseValue)).isTrue();

    assertThat((createLiterals(Tree.Kind.NULL_LITERAL, "NULL"))
      .allMatch(CheckUtils::isFalseValue)).isTrue();

    VariableIdentifierTreeImpl variableIdentifierTree = new VariableIdentifierTreeImpl(
      new InternalSyntaxToken(1, 1, "var", Collections.emptyList(), 1, false));
    assertThat(CheckUtils.isFalseValue(variableIdentifierTree)).isFalse();
  }

  @Test
  public void is_true_value() throws Exception {
    assertThat(createLiterals(Tree.Kind.BOOLEAN_LITERAL, "true", "True", "TRUE")
      .allMatch(CheckUtils::isTrueValue)).isTrue();

    assertThat(createLiterals(Tree.Kind.NUMERIC_LITERAL, "1", "-1", "3.14")
      .allMatch(CheckUtils::isTrueValue)).isTrue();

    assertThat(createLiterals(Tree.Kind.REGULAR_STRING_LITERAL, "\"abc\"", "'1'", "'false'", "'0.0'")
      .allMatch(CheckUtils::isTrueValue)).isTrue();

    assertThat((createLiterals(Tree.Kind.NULL_LITERAL, "NULL"))
      .allMatch(CheckUtils::isTrueValue)).isFalse();

    VariableIdentifierTreeImpl variableIdentifierTree = new VariableIdentifierTreeImpl(
      new InternalSyntaxToken(1, 1, "var", Collections.emptyList(), 1, false));
    assertThat(CheckUtils.isTrueValue(variableIdentifierTree)).isFalse();
  }

  @Test
  public void is_string_literal_with_value() throws Exception {
    assertThat(createLiterals(Tree.Kind.REGULAR_STRING_LITERAL, "\"foo\"", "\"Foo\"", "\"FOO\"")
      .allMatch(literalTree -> isStringLiteralWithValue(literalTree, "foo"))).isTrue();

    assertThat(createLiterals(Tree.Kind.REGULAR_STRING_LITERAL, "\"foo\"")
      .allMatch(literalTree -> isStringLiteralWithValue(literalTree, "bar"))).isFalse();

    assertThat(createLiterals(Tree.Kind.BOOLEAN_LITERAL, "true")
      .allMatch(literalTree -> isStringLiteralWithValue(literalTree, "bar"))).isFalse();

    assertThat(isStringLiteralWithValue(null, "foo")).isFalse();
  }

  @Test
  public void is_null_or_empty_string() {
    assertThat(createLiterals(Tree.Kind.NULL_LITERAL, "NULL")
        .allMatch(CheckUtils::isNullOrEmptyString)).isTrue();

    assertThat(createLiterals(Tree.Kind.REGULAR_STRING_LITERAL, "", "   ")
        .allMatch(CheckUtils::isNullOrEmptyString)).isTrue();

    assertThat(createLiterals(Tree.Kind.REGULAR_STRING_LITERAL, "x", "  .  ")
        .allMatch(CheckUtils::isNullOrEmptyString)).isFalse();

    assertThat(createLiterals(Tree.Kind.BOOLEAN_LITERAL, "true", "false")
        .allMatch(CheckUtils::isNullOrEmptyString)).isFalse();
  }

  @Test
  public void has_annotation_of_function() {
    FunctionDeclarationTree tree = (FunctionDeclarationTree) parse("/**\n * @annotation\n */\nfunction foo(){}");
    assertThat(CheckUtils.hasAnnotation(tree, "@annotation")).isTrue();
    assertThat(CheckUtils.hasAnnotation(tree, "annotation")).isTrue();
    assertThat(CheckUtils.hasAnnotation(tree, "other_annotation")).isFalse();

    tree = (FunctionDeclarationTree) parse("/**\n * annotation\n */\nfunction foo(){}");
    assertThat(CheckUtils.hasAnnotation(tree, "@annotation")).isFalse();
    assertThat(CheckUtils.hasAnnotation(tree, "annotation")).isFalse();
  }

  @Test
  public void has_modifier() {
    ClassMemberTree tree = parseClassMember("abstract protected function foo(){}");
    assertThat(CheckUtils.hasModifier(tree, "abstract")).isTrue();
    assertThat(CheckUtils.hasModifier(tree, "protected")).isTrue();

    tree = parseClassMember("use MyTrait;");
    assertThat(CheckUtils.hasModifier(tree, "private")).isFalse();
  }


  @Test
  public void is_public() {
    ClassMemberTree tree = parseClassMember("public function foo(){}");
    assertThat(CheckUtils.isPublic(tree)).isTrue();

    tree = parseClassMember("public $field = null;");
    assertThat(CheckUtils.isPublic(tree)).isTrue();

    tree = parseClassMember("private $field = null;");
    assertThat(CheckUtils.isPublic(tree)).isFalse();

    tree = parseClassMember("protected function foo(){}");
    assertThat(CheckUtils.isPublic(tree)).isFalse();

    tree = parseClassMember("function foo(){}");
    assertThat(CheckUtils.isPublic(tree)).isTrue();

    tree = parseClassMember("use MyTrait;");
    assertThat(CheckUtils.isPublic(tree)).isFalse();
  }

  @Test
  public void pure_function_name() {
    FunctionCallTree functionCall = (FunctionCallTree) expressionFromStatement("fooBar();");
    assertThat(functionName(functionCall)).isEqualTo("fooBar");
    assertThat(functionName(functionCall)).isNotEqualTo("foobar");

    functionCall = (FunctionCallTree) expressionFromStatement("$this->fooBar();");
    assertThat(functionName(functionCall)).isEqualTo("fooBar");

    functionCall = (FunctionCallTree) expressionFromStatement("self::fooBar();");
    assertThat(functionName(functionCall)).isEqualTo("fooBar");

    functionCall = (FunctionCallTree) expressionFromStatement("self::$foo();");
    assertThat(functionName(functionCall)).isNull();
  }

  @Test
  public void pure_lower_case_function_name() {
    FunctionCallTree functionCall = (FunctionCallTree) expressionFromStatement("fooBar();");
    assertThat(lowerCaseFunctionName(functionCall)).isEqualTo("foobar");
    assertThat(lowerCaseFunctionName(functionCall)).isNotEqualTo("fooBar");

    functionCall = (FunctionCallTree) expressionFromStatement("$this->fooBar();");
    assertThat(lowerCaseFunctionName(functionCall)).isEqualTo("foobar");

    functionCall = (FunctionCallTree) expressionFromStatement("self::fooBar();");
    assertThat(lowerCaseFunctionName(functionCall)).isEqualTo("foobar");

    functionCall = (FunctionCallTree) expressionFromStatement("self::$foo();");
    assertThat(lowerCaseFunctionName(functionCall)).isNull();
  }

  @Test
  public void test_named_argument_retrieval() {
    Tree tree = expressionFromStatement("f(self::$p1, a: $p2);");
    FunctionCallTree callTree = (FunctionCallTree) tree;

    assertThat(callTree.callArguments()).hasSize(2);
    assertThat(callTree.callArguments().get(0).name()).isNull();
    assertThat(argument(callTree, "someName", 0)).contains(callTree.callArguments().get(0));
    assertThat(argument(callTree, "a", 0)).contains(callTree.callArguments().get(1));
    assertThat(argument(callTree, "someName", 2)).isEmpty();
  }

  @Test
  public void hasNamedArgument() {
    assertThat(CheckUtils.hasNamedArgument((FunctionCallTree) expressionFromStatement("foo();"))).isFalse();
    assertThat(CheckUtils.hasNamedArgument((FunctionCallTree) expressionFromStatement("foo($a, $b);"))).isFalse();
    assertThat(CheckUtils.hasNamedArgument((FunctionCallTree) expressionFromStatement("foo($a, b: $b);"))).isTrue();
  }

  @Test
  public void uniqueAssignedValue() {
    VariableIdentifierTreeImpl var = mock(VariableIdentifierTreeImpl.class);
    SymbolImpl symbol = mock(SymbolImpl.class);
    when(symbol.uniqueAssignedValue()).thenReturn(Optional.of(mock(ExpressionTree.class)));
    assertThat(CheckUtils.uniqueAssignedValue(var)).isNotPresent();
    when(var.symbol()).thenReturn(symbol);
    assertThat(CheckUtils.uniqueAssignedValue(var)).isPresent();
  }

  @Test
  public void arrayValue() {
    assertThat(CheckUtils.arrayValue((ArrayInitializerTree) expressionFromStatement("array('key' => 'value');"), "key")).isPresent();
    assertThat(CheckUtils.arrayValue((ArrayInitializerTree) expressionFromStatement("array('other_key' => 'value');"), "key")).isNotPresent();
    assertThat(CheckUtils.arrayValue((ArrayInitializerTree) expressionFromStatement("array($key => 'value');"), "key")).isNotPresent();
    assertThat(CheckUtils.arrayValue((ArrayInitializerTree) expressionFromStatement("array('value');"), "key")).isNotPresent();
  }

  @Test
  public void noClass_methodInheritedFromClassOrInterface() {
    MethodDeclarationTree method = (MethodDeclarationTree) ((ClassDeclarationTree) parse("trait Wrapper{public function foo() {}}")).members().get(0);
    assertThat(isMethodInheritedFromClassOrInterface(QualifiedName.qualifiedName("A\\B"), method)).isFalse();
  }

  private static Stream<LiteralTree> createLiterals(Tree.Kind kind, String... values) {
    return Arrays.stream(values).map(value -> new LiteralTreeImpl(kind,
      new InternalSyntaxToken(1, 1, value, Collections.emptyList(), 0, false)));
  }

  private ExpressionTree expressionFromStatement(String statement) {
    return ((ExpressionStatementTree) parse(statement)).expression();
  }

  private Tree parse(String toParse) {
    return parser.parse(toParse);
  }

  private List<ClassMemberTree> parseClassMembers(String toParse) {
    return ((ClassDeclarationTree) parse("class Wrapper{" + toParse+ "}")).members();
  }

  private ClassMemberTree parseClassMember(String toParse) {
    return parseClassMembers(toParse).get(0);
  }

  private List<StatementTree> parseMethodStatements(String toParse) {
    MethodDeclarationTree method = (MethodDeclarationTree) parseClassMembers("public function wrapperMethod() {"+ toParse +"}").get(0);
    return ((BlockTree) method.body()).statements();
  }

  private StatementTree parseMethodStatement(String toParse) {
    return parseMethodStatements(toParse).get(0);
  }
}
