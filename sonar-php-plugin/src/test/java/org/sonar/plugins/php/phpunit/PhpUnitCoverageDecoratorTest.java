/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * dev@sonar.codehaus.org
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
package org.sonar.plugins.php.phpunit;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.File;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class PhpUnitCoverageDecoratorTest {

  private File file;
  private Project project;
  private PhpUnitCoverageDecorator decorator;

  @Before
  public void init() {
    file = new File("Foo");
    project = mock(Project.class);
    decorator = new PhpUnitCoverageDecorator();
  }

  @Test
  public void testDecorate() throws Exception {
    DecoratorContext context = mock(DecoratorContext.class);
    when(context.getMeasure(CoreMetrics.NCLOC)).thenReturn(new Measure(CoreMetrics.NCLOC, 100.0));
    decorator.decorate(file, context);

    verify(context, times(1)).saveMeasure(CoreMetrics.LINE_COVERAGE, 0.0);
    verify(context, times(1)).saveMeasure(CoreMetrics.LINES_TO_COVER, 100.0);
    verify(context, times(1)).saveMeasure(CoreMetrics.UNCOVERED_LINES, 100.0);
  }

  @Test
  public void testDecorateWithNoNCLOC() throws Exception {
    DecoratorContext context = mock(DecoratorContext.class);
    when(context.getMeasure(CoreMetrics.NCLOC)).thenReturn(null);
    decorator.decorate(file, context);

    verify(context, times(1)).saveMeasure(CoreMetrics.LINE_COVERAGE, 0.0);
    verify(context, never()).saveMeasure(eq(CoreMetrics.LINES_TO_COVER), anyDouble());
    verify(context, never()).saveMeasure(eq(CoreMetrics.UNCOVERED_LINES), anyDouble());
  }

  @Test
  public void testDontDecorateIfNotFile() throws Exception {
    DecoratorContext context = mock(DecoratorContext.class);
    decorator.decorate(new Project("Foo"), context);

    verify(context, never()).saveMeasure(any(Metric.class), anyDouble());
  }

  @Test
  public void testDontDecorateIfFileAlreadyHasLineCoverageMeasure() throws Exception {
    DecoratorContext context = mock(DecoratorContext.class);
    when(context.getMeasure(CoreMetrics.LINE_COVERAGE)).thenReturn(new Measure());

    decorator.decorate(file, context);
    verify(context, never()).saveMeasure(any(Metric.class), anyDouble());
  }

  @Test
  public void testDependedUponMetrics() throws Exception {
    List<Metric> metrics = Arrays.asList(CoreMetrics.COVERAGE, CoreMetrics.LINE_COVERAGE, CoreMetrics.LINES_TO_COVER,
      CoreMetrics.UNCOVERED_LINES);

    assertThat(decorator.generatesCoverageMetrics(), equalTo(metrics));
  }

  @Test
  public void testShouldExecuteOnProject() throws Exception {
    ProjectFileSystem fileSystem = mock(ProjectFileSystem.class);
    when(project.getFileSystem()).thenReturn(fileSystem);

    when(fileSystem.mainFiles("php")).thenReturn(ImmutableList.<InputFile>of());
    assertFalse(decorator.shouldExecuteOnProject(project));

    when(fileSystem.mainFiles("php")).thenReturn(ImmutableList.<InputFile>of(mock(InputFile.class)));
    assertTrue(decorator.shouldExecuteOnProject(project));
  }
}
