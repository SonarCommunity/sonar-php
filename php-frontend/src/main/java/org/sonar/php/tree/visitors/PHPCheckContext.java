/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
package org.sonar.php.tree.visitors;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.php.regex.RegexCache;
import org.sonar.php.regex.RegexCheck;
import org.sonar.php.regex.RegexCheckContext;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.CheckContext;
import org.sonar.plugins.php.api.visitors.FileIssue;
import org.sonar.plugins.php.api.visitors.IssueLocation;
import org.sonar.plugins.php.api.visitors.LineIssue;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PhpFile;
import org.sonar.plugins.php.api.visitors.PhpIssue;
import org.sonar.plugins.php.api.visitors.PreciseIssue;
import org.sonarsource.analyzer.commons.regex.RegexParseResult;
import org.sonarsource.analyzer.commons.regex.ast.FlagSet;
import org.sonarsource.analyzer.commons.regex.ast.RegexSyntaxElement;

public class PHPCheckContext implements CheckContext, RegexCheckContext {

  private final PhpFile file;
  private final CompilationUnitTree tree;
  @Nullable
  private final File workingDirectory;
  private final SymbolTable symbolTable;
  private List<PhpIssue> issues;
  private final RegexCache regexCache;

  public PHPCheckContext(PhpFile file, CompilationUnitTree tree, @Nullable File workingDirectory) {
    this(file, tree, workingDirectory, SymbolTableImpl.create(tree));
  }

  public PHPCheckContext(PhpFile file, CompilationUnitTree tree, @Nullable File workingDirectory, SymbolTable symbolTable) {
    this.file = file;
    this.tree = tree;
    this.workingDirectory = workingDirectory;
    this.symbolTable = symbolTable;
    this.issues = new ArrayList<>();
    this.regexCache = new RegexCache();
  }

  @Override
  public CompilationUnitTree tree() {
    return tree;
  }

  @Override
  public LegacyIssue newIssue(PHPCheck check, String message) {
    LegacyIssue issue = new LegacyIssue(check, message);
    issues.add(issue);

    return issue;
  }

  @Override
  public PreciseIssue newIssue(PHPCheck check, Tree tree, String message) {
    PreciseIssue issue = new PreciseIssue(check, new IssueLocation(tree, message));
    issues.add(issue);

    return issue;
  }

  @Override
  public PreciseIssue newIssue(PHPCheck check, Tree startTree, Tree endTree, String message) {
    PreciseIssue issue = new PreciseIssue(check, new IssueLocation(startTree, endTree, message));
    issues.add(issue);

    return issue;
  }

  @Override
  public PreciseIssue newIssue(RegexCheck regexCheck, RegexSyntaxElement regexSyntaxElement, String message) {
    PreciseIssue issue = new PreciseIssue(regexCheck, new RegexCheck.RegexIssueLocation(regexSyntaxElement, message));
    issues.add(issue);

    return issue;
  }

  @Override
  public RegexParseResult regexForLiteral(FlagSet initialFlags, LiteralTree stringLiteral) {
    return regexCache.getRegexForLiterals(initialFlags, stringLiteral);
  }

  @Override
  public LineIssue newLineIssue(PHPCheck check, int line, String message) {
    LineIssue issue = new LineIssue(check, line, message);
    issues.add(issue);

    return issue;
  }

  @Override
  public FileIssue newFileIssue(PHPCheck check, String message) {
    FileIssue issue = new FileIssue(check, message);
    issues.add(issue);

    return issue;
  }

  @Override
  public PhpFile getPhpFile() {
    return file;
  }

  @Override
  public List<PhpIssue> getIssues() {
    return Collections.unmodifiableList(issues);
  }

  @Override
  public SymbolTable symbolTable() {
    return symbolTable;
  }

  @Override
  public File getWorkingDirectory() {
    return workingDirectory;
  }
}
