/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
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
package org.sonar.php.symbols;

import com.sonar.sslr.api.typed.ActionParser;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Test;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.tree.TreeUtils;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;
import org.sonar.plugins.php.api.visitors.PhpFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.sonar.php.tree.TreeUtils.firstDescendant;
import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;

public class ProjectSymbolTableTest {

  private final ActionParser<Tree> parser = PHPParserBuilder.createParser();

  @Test
  public void superclass_in_different_file() {
    PhpFile file1 = file("file1.php", "<?php namespace ns1; class A {}");
    PhpFile file2 = file("file2.php", "<?php namespace ns1; class C extends B {}");
    PhpFile file3 = file("file3.php", "<?php namespace ns1; class B extends A {}");
    ProjectSymbolData projectSymbolData = buildProjectSymbolData(file1, file2, file3);
    Tree ast = parser.parse(file2.contents());
    SymbolTableImpl.create((CompilationUnitTree) ast, projectSymbolData, file2);
    Optional<ClassDeclarationTree> classDeclarationTree = firstDescendant(ast, ClassDeclarationTree.class);
    ClassSymbol c = Symbols.get(classDeclarationTree.get());
    assertThat(c.qualifiedName()).hasToString("ns1\\c");
    assertThat(c.location()).isEqualTo(new LocationInFileImpl(filePath("file2.php"), 1, 27, 1, 28));
    ClassSymbol b = c.superClass().get();
    assertThat(b.qualifiedName()).hasToString("ns1\\b");
    assertThat(b.location()).isEqualTo(new LocationInFileImpl(filePath("file3.php"), 1, 27, 1, 28));
    ClassSymbol a = b.superClass().get();
    assertThat(a.qualifiedName()).hasToString("ns1\\a");
    assertThat(a.superClass()).isEmpty();
  }

  @Test
  public void implemented_interfaces() {
    PhpFile file2 = file("file2.php", "<?php namespace ns1; class B extends A implements C {}");
    PhpFile file3 = file("file3.php", "<?php namespace ns1; interface C extends D {}");
    ProjectSymbolData projectSymbolData = buildProjectSymbolData(file2, file3);

    Tree ast = parser.parse(file2.contents());
    SymbolTableImpl.create((CompilationUnitTree) ast, projectSymbolData, file2);
    ClassDeclarationTree bDeclaration = firstDescendant(ast, ClassDeclarationTree.class).get();

    ClassSymbol b = Symbols.get(bDeclaration);
    assertThat(b.qualifiedName()).hasToString("ns1\\b");

    ClassSymbol a = b.superClass().get();
    assertThat(a.qualifiedName()).hasToString("ns1\\a");
    assertThat(a.isUnknownSymbol()).isTrue();
    assertThat(a.implementedInterfaces()).isEmpty();

    List<ClassSymbol> bInterfaces = b.implementedInterfaces();
    assertThat(bInterfaces).hasSize(1);
    ClassSymbol c = bInterfaces.get(0);
    assertThat(c.implementedInterfaces()).extracting(ClassSymbol::qualifiedName).containsExactly(qualifiedName("ns1\\d"));
  }

  @Test
  public void catch_clause() {
    PhpFile file1 = file("file1.php", "<?php namespace ns1; class A {}");
    PhpFile file2 = file("file2.php", "<?php namespace ns1; try {} catch (A $a) {}");
    Tree ast = parser.parse(file2.contents());
    SymbolTableImpl.create((CompilationUnitTree) ast, buildProjectSymbolData(file1, file2), file2);
    CatchBlockTree catchBlockTree = firstDescendant(ast, CatchBlockTree.class).get();
    ClassSymbol a = Symbols.getClass(catchBlockTree.exceptionTypes().get(0));
    assertThat(a.location()).isEqualTo(new LocationInFileImpl(filePath("file1.php"), 1, 27, 1, 28));
  }

  @Test
  public void new_expression() {
    PhpFile file1 = file("file1.php", "<?php namespace ns1; class A {}");
    PhpFile file2 = file("file2.php", "<?php namespace ns1;\n new A();\n new A;");
    Tree ast = parser.parse(file2.contents());
    SymbolTableImpl.create((CompilationUnitTree) ast, buildProjectSymbolData(file1, file2), file2);

    List<NewExpressionTree> newExpressions = TreeUtils.descendants(ast)
      .filter(NewExpressionTree.class::isInstance)
      .map(NewExpressionTree.class::cast)
      .collect(Collectors.toList());
    assertThat(newExpressions).extracting(e -> ((PHPTree) e).getLine()).containsExactly(2, 3);
    assertThat(newExpressions).extracting(e -> e.expression().getKind()).containsExactly(Tree.Kind.FUNCTION_CALL, Tree.Kind.NAMESPACE_NAME);
    for (NewExpressionTree newExpression : newExpressions) {
      NamespaceNameTree namespaceName = firstDescendant(newExpression, NamespaceNameTree.class).get();
      assertThat(Symbols.getClass(namespaceName).location()).isEqualTo(new LocationInFileImpl(filePath("file1.php"), 1, 27, 1, 28));
    }
  }

  @Test
  public void non_class_namespace_name() {
    PhpFile file1 = file("file1.php", "<?php namespace ns1; class A {}");
    Tree ast = parser.parse(file1.contents());
    NamespaceNameTree namespaceNameTree = firstDescendant(ast, NamespaceNameTree.class).get();
    assertThatThrownBy(
      () -> Symbols.getClass(namespaceNameTree))
      .isInstanceOf(IllegalStateException.class);
  }

  private ProjectSymbolData buildProjectSymbolData(PhpFile... files) {
    ProjectSymbolData projectSymbolData = new ProjectSymbolData();
    for (PhpFile file : files) {
      Tree ast = parser.parse(file.contents());
      SymbolTableImpl symbolTable = SymbolTableImpl.create((CompilationUnitTree) ast, new ProjectSymbolData(), file);
      symbolTable.classSymbolDatas().forEach(projectSymbolData::add);
    }
    return projectSymbolData;
  }

  private String filePath(String fileName) {
    return path(fileName).toFile().getAbsolutePath();
  }

  private Path path(String fileName) {
    return Paths.get("dir1", fileName);
  }

  private PhpFile file(String name, String content) {
    return new PhpFile() {

      @Override
      public String contents() {
        return content;
      }

      @Override
      public String filename() {
        return name;
      }

      @Override
      public URI uri() {
        return path(name).toUri();
      }
    };
  }
}
