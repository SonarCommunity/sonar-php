/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.tree.impl.declaration;

import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;

import static org.fest.assertions.Assertions.assertThat;

public class ClassDeclarationTreeTest extends PHPTreeModelTest {

  @Test
  public void full_class_declaration() throws Exception {
    ClassDeclarationTree tree = parse("final class A extends B implements C, D { public $var; }", PHPLexicalGrammar.CLASS_DECLARATION);

    assertThat(tree.is(Kind.CLASS_DECLARATION)).isTrue();
    assertThat(tree.modifierToken().text()).isEqualTo("final");
    assertThat(tree.classEntryTypeToken().text()).isEqualTo("class");
    assertThat(tree.name().name()).isEqualTo("A");
    assertThat(tree.extendsToken().text()).isEqualTo("extends");
    assertThat(tree.superClass().name().name()).isEqualTo("B");
    assertThat(tree.implementsToken().text()).isEqualTo("implements");
    assertThat(tree.superInterfaces()).hasSize(2);
    assertThat(tree.members()).hasSize(1);
  }

  @Test
  public void simple_class_declaration() throws Exception {
    ClassDeclarationTree tree = parse("class A { }", PHPLexicalGrammar.CLASS_DECLARATION);

    assertThat(tree.is(Kind.CLASS_DECLARATION)).isTrue();
    assertThat(tree.modifierToken()).isNull();
    assertThat(tree.extendsToken()).isNull();
    assertThat(tree.superClass()).isNull();
    assertThat(tree.implementsToken()).isNull();
    assertThat(tree.superInterfaces()).hasSize(0);
    assertThat(tree.members()).hasSize(0);
  }

  @Test
  public void trait_declaration() throws Exception {
    ClassDeclarationTree tree = parse("trait A { function foo(){} }", PHPLexicalGrammar.TRAIT_DECLARATION);

    assertThat(tree.is(Kind.TRAIT_DECLARATION)).isTrue();
    assertThat(tree.modifierToken()).isNull();
    assertThat(tree.classEntryTypeToken().text()).isEqualTo("trait");
    assertThat(tree.name().name()).isEqualTo("A");
    assertThat(tree.extendsToken()).isNull();
    assertThat(tree.superClass()).isNull();
    assertThat(tree.implementsToken()).isNull();
    assertThat(tree.superInterfaces()).hasSize(0);
    assertThat(tree.members()).hasSize(1);
  }

  @Test
  public void interface_declaration() throws Exception {
    ClassDeclarationTree tree = parse("interface A extends B, C { function foo(); }", PHPLexicalGrammar.INTERFACE_DECLARATION);

    assertThat(tree.is(Kind.INTERFACE_DECLARATION)).isTrue();
    assertThat(tree.modifierToken()).isNull();
    assertThat(tree.classEntryTypeToken().text()).isEqualTo("interface");
    assertThat(tree.name().name()).isEqualTo("A");
    assertThat(tree.extendsToken()).isNotNull();
    assertThat(tree.superClass()).isNull();
    assertThat(tree.implementsToken()).isNull();
    assertThat(tree.superInterfaces()).hasSize(2);
    assertThat(tree.members()).hasSize(1);
  }

}
