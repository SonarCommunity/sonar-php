package org.sonar.php.parser.declaration;

import org.junit.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

public class EnumDeclarationTest {

  @Test
  public void test() {
    assertThat(PHPLexicalGrammar.ENUM_DECLARATION)
      .matches("enum A {}")
      .matches("enum A { case A; }")
      .matches("enum A {\n case A;\n case B; }")
      .notMatches("enum A {")
      .notMatches("enum A { case A}")
    ;
  }
}
