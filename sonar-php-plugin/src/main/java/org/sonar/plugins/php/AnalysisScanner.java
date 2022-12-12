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
package org.sonar.plugins.php;

import com.google.common.base.Throwables;
import com.sonar.sslr.api.RecognitionException;
import java.io.File;
import java.io.InterruptedIOException;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.DurationStatistics;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.cpd.NewCpdTokens;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.php.PHPAnalyzer;
import org.sonar.php.checks.ParsingErrorCheck;
import org.sonar.php.checks.UncatchableExceptionCheck;
import org.sonar.php.checks.utils.PhpUnitCheck;
import org.sonar.php.compat.PhpFileImpl;
import org.sonar.php.metrics.CpdVisitor;
import org.sonar.php.metrics.FileMeasures;
import org.sonar.php.symbols.ProjectSymbolData;
import org.sonar.php.tree.visitors.LegacyIssue;
import org.sonar.plugins.php.api.visitors.FileIssue;
import org.sonar.plugins.php.api.visitors.IssueLocation;
import org.sonar.plugins.php.api.visitors.LineIssue;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PhpIssue;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

class AnalysisScanner extends Scanner {

  private static final Logger LOG = Loggers.get(AnalysisScanner.class);
  private final PHPChecks checks;
  private final FileLinesContextFactory fileLinesContextFactory;
  private final NoSonarFilter noSonarFilter;
  private RuleKey parsingErrorRuleKey;

  private final boolean hasTestFileChecks;
  private PHPAnalyzer phpAnalyzer;


  public AnalysisScanner(SensorContext context,
    PHPChecks checks,
    FileLinesContextFactory fileLinesContextFactory,
    NoSonarFilter noSonarFilter,
    ProjectSymbolData projectSymbolData,
    DurationStatistics statistics) {

    super(context, statistics);
    this.checks = checks;
    this.fileLinesContextFactory = fileLinesContextFactory;
    this.noSonarFilter = noSonarFilter;
    this.parsingErrorRuleKey = getParsingErrorRuleKey();


    List<PHPCheck> allChecks = this.checks.all();
    if (inSonarLint(context)) {
      allChecks = allChecks.stream()
        .filter(e -> !(e instanceof UncatchableExceptionCheck))
        .collect(Collectors.toList());
    }

    List<PHPCheck> testFilesChecks = allChecks.stream().
      filter(PhpUnitCheck.class::isInstance).
      collect(Collectors.toList());
    hasTestFileChecks = !testFilesChecks.isEmpty();

    File workingDir = context.fileSystem().workDir();
    phpAnalyzer = new PHPAnalyzer(allChecks, testFilesChecks, workingDir, projectSymbolData, statistics);
  }

  @Override
  String name() {
    return "PHP rules";
  }

  @Override
  void scanFile(InputFile inputFile) {
    if (fileCanBeSkipped(inputFile) || (inputFile.type() == InputFile.Type.TEST && !hasTestFileChecks)) {
      return;
    }

    try {
      phpAnalyzer.nextFile(new PhpFileImpl(inputFile));

      if (!inSonarLint(context)) {
        phpAnalyzer.getSyntaxHighlighting(context, inputFile).save();
        phpAnalyzer.getSymbolHighlighting(context, inputFile).save();
        if (inputFile.type() == InputFile.Type.MAIN) {
          saveNewFileMeasures(context,
            phpAnalyzer.computeMeasures(fileLinesContextFactory.createFor(inputFile)),
            inputFile);
          saveCpdData(phpAnalyzer.computeCpdTokens(), inputFile, context);
        }
      }

      noSonarFilter.noSonarInFile(inputFile, phpAnalyzer.computeNoSonarLines());
      saveIssues(context, inputFile.type() == InputFile.Type.MAIN ? phpAnalyzer.analyze() : phpAnalyzer.analyzeTest(), inputFile);
    } catch (RecognitionException e) {
      checkInterrupted(e);
      LOG.error("Unable to parse file [{}] at line {}", inputFile.uri(), e.getLine());
      LOG.error(e.getMessage());
      saveParsingIssue(context, e, inputFile);
    }
  }

  private void saveIssues(SensorContext context, List<PhpIssue> issues, InputFile inputFile) {
    for (PhpIssue issue : issues) {
      RuleKey ruleKey = checks.ruleKeyFor(issue.check());
      NewIssue newIssue = context.newIssue()
        .forRule(ruleKey)
        .gap(issue.cost());

      if (issue instanceof LegacyIssue) {
        // todo: this block should be removed as PHPIssue's usages will be removed
        LegacyIssue legacyIssue = (LegacyIssue) issue;

        NewIssueLocation location = newIssue.newLocation()
          .message(legacyIssue.message())
          .on(inputFile);

        if (legacyIssue.line() > 0) {
          location.at(inputFile.selectLine(legacyIssue.line()));
        }

        newIssue.at(location);

      } else if (issue instanceof LineIssue) {

        LineIssue lineIssue = (LineIssue) issue;

        NewIssueLocation location = newIssue.newLocation()
          .message(lineIssue.message())
          .on(inputFile)
          .at(inputFile.selectLine(lineIssue.line()));

        newIssue.at(location);

      } else if (issue instanceof FileIssue) {

        FileIssue fileIssue = (FileIssue) issue;

        NewIssueLocation location = newIssue.newLocation()
          .message(fileIssue.message())
          .on(inputFile);

        newIssue.at(location);

      } else {

        PreciseIssue preciseIssue = (PreciseIssue) issue;

        newIssue.at(newLocation(inputFile, newIssue, preciseIssue.primaryLocation()));
        preciseIssue.secondaryLocations().forEach(secondary ->
          addSecondaryLocation(context, inputFile, newIssue, secondary)
        );
      }

      newIssue.save();
    }
  }

  /**
   * Creates and saves an issue for a parsing error.
   */
  private void saveParsingIssue(SensorContext context, RecognitionException e, InputFile inputFile) {
    if (parsingErrorRuleKey != null) {
      NewIssue issue = context.newIssue();

      NewIssueLocation location = issue.newLocation()
        .message("A parsing error occurred in this file.")
        .on(inputFile)
        .at(inputFile.selectLine(e.getLine()));

      issue
        .forRule(parsingErrorRuleKey)
        .at(location)
        .save();
    }

    context.newAnalysisError()
      .onFile(inputFile)
      .at(inputFile.newPointer(e.getLine(), 0))
      .message(e.getMessage())
      .save();
  }

  @Override
  void logException(Exception e, InputFile file) {
    LOG.error("Could not analyse " + file, e);
  }

  @Override
  void onEnd() {
    phpAnalyzer.terminate();
  }


  private static void saveNewFileMeasures(SensorContext context, FileMeasures fileMeasures, InputFile inputFile) {
    context.<Integer>newMeasure().on(inputFile).withValue(fileMeasures.getLinesOfCodeNumber()).forMetric(CoreMetrics.NCLOC).save();
    context.<Integer>newMeasure().on(inputFile).withValue(fileMeasures.getCommentLinesNumber()).forMetric(CoreMetrics.COMMENT_LINES).save();
    context.<Integer>newMeasure().on(inputFile).withValue(fileMeasures.getClassNumber()).forMetric(CoreMetrics.CLASSES).save();
    context.<Integer>newMeasure().on(inputFile).withValue(fileMeasures.getFunctionNumber()).forMetric(CoreMetrics.FUNCTIONS).save();
    context.<Integer>newMeasure().on(inputFile).withValue(fileMeasures.getStatementNumber()).forMetric(CoreMetrics.STATEMENTS).save();
    context.<Integer>newMeasure().on(inputFile).withValue(fileMeasures.getFileCognitiveComplexity()).forMetric(CoreMetrics.COGNITIVE_COMPLEXITY).save();
    context.<Integer>newMeasure().on(inputFile).withValue(fileMeasures.getFileComplexity()).forMetric(CoreMetrics.COMPLEXITY).save();
  }

  private static void addSecondaryLocation(SensorContext context, InputFile inputFile, NewIssue newIssue, IssueLocation secondary) {
    InputFile file = inputFile;
    String filePath = secondary.filePath();
    if (filePath != null) {
      file = context.fileSystem().inputFile(context.fileSystem().predicates().is(new File(filePath)));
    }
    if (file != null) {
      newIssue.addLocation(newLocation(file, newIssue, secondary));
    }
  }

  private static void saveCpdData(List<CpdVisitor.CpdToken> cpdTokens, InputFile inputFile, SensorContext context) {
    NewCpdTokens newCpdTokens = context.newCpdTokens().onFile(inputFile);

    cpdTokens.forEach(cpdToken -> newCpdTokens.addToken(
      inputFile.newRange(
        cpdToken.syntaxToken().line(),
        cpdToken.syntaxToken().column(),
        cpdToken.syntaxToken().endLine(),
        cpdToken.syntaxToken().endColumn()),
      cpdToken.image()));

    newCpdTokens.save();
  }

  private static NewIssueLocation newLocation(InputFile inputFile, NewIssue issue, IssueLocation location) {
    TextRange range = inputFile.newRange(location.startLine(), location.startLineOffset(), location.endLine(), location.endLineOffset());

    NewIssueLocation newLocation = issue.newLocation()
      .on(inputFile)
      .at(range);

    if (location.message() != null) {
      newLocation.message(location.message());
    }
    return newLocation;
  }

  private static void checkInterrupted(Exception e) {
    Throwable cause = Throwables.getRootCause(e);
    if (cause instanceof InterruptedException || cause instanceof InterruptedIOException) {
      throw new AnalysisException("Analysis cancelled", e);
    }
  }

  private RuleKey getParsingErrorRuleKey() {
    List<RuleKey> keys = checks.all().stream()
      .filter(ParsingErrorCheck.class::isInstance)
      .map(checks::ruleKeyFor)
      .collect(Collectors.toList());

    return keys.isEmpty() ? null : keys.get(0);
  }
}
