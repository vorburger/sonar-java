/*
 * SonarQube Java
 * Copyright (C) 2012 SonarSource
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
package org.sonar.java;

import com.google.common.base.Charsets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.InputFileUtils;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.squidbridge.api.CodeVisitor;

import java.io.File;
import java.util.Collections;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MeasurerTest {

  private static final int NB_OF_METRICS = 9;
  private SensorContext context;
  private JavaSquid squid;
  private File baseDir;

  @Before
  public void setUp() throws Exception {
    JavaConfiguration conf = new JavaConfiguration(Charsets.UTF_8);
    context = mock(SensorContext.class);
    Project sonarProject = mock(Project.class);
    ProjectFileSystem pfs = mock(ProjectFileSystem.class);
    baseDir = new File("src/test/files/metrics");
    when(sonarProject.getFileSystem()).thenReturn(pfs);
    when(pfs.getBasedir()).thenReturn(baseDir);
    Measurer measurer = new Measurer(sonarProject, context);
    squid = new JavaSquid(conf, null, measurer, new CodeVisitor[0]);
  }

  @Test
  public void verify_lines_metric() {
    checkMetric("Lines.java", "lines", 7.0);
  }

  @Test
  public void verify_methods_metric() {
    checkMetric("Methods.java", "functions", 7.0);
  }

  @Test
  public void verify_public_api_metric() {
    checkMetric("Comments.java", "public_api", 2.0);
  }

  @Test
  public void verify_public_api_density_metric() {
    checkMetric("Comments.java", "public_documented_api_density", 100.0);
  }

  @Test
  public void verify_public_undocumented_api() {
    checkMetric("Comments.java", "public_undocumented_api", 0.0);
  }

  @Test
  public void verify_class_metric() {
    checkMetric("Classes.java", "classes", 4.0);
  }

  private void checkMetric(String filename, String metric, double expectedValue) {
    InputFile sourceFile = InputFileUtils.create(baseDir, new File(baseDir, filename));
    squid.scan(Collections.singleton(sourceFile), Collections.<InputFile>emptyList(), Collections.<File>emptyList());
    ArgumentCaptor<Measure> captor = ArgumentCaptor.forClass(Measure.class);
    ArgumentCaptor<org.sonar.api.resources.File> sonarFilescaptor = ArgumentCaptor.forClass(org.sonar.api.resources.File.class);
    verify(context, times(NB_OF_METRICS)).saveMeasure(sonarFilescaptor.capture(), captor.capture());
    int checkedMetrics = 0;
    for (Measure measure : captor.getAllValues()) {
      if (metric.equals(measure.getMetricKey())) {
        assertThat(measure.getValue()).isEqualTo(expectedValue);
        checkedMetrics++;
      }
    }
    assertThat(checkedMetrics).isEqualTo(1);
  }
}