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
package org.sonar.php.cache;


import org.junit.Test;
import org.sonar.api.batch.sensor.cache.WriteCache;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PhpWriteCacheImplTest {

  private final static byte [] bytes = new byte[] {1, 2, 3};

  @Test
  public void shouldWriteFromWriteCache() {
    WriteCache writeCache = mock(WriteCache.class);
    PhpWriteCacheImpl cache = new PhpWriteCacheImpl(writeCache);

    cache.writeBytes("key", bytes);

    verify(writeCache).write("key", bytes);
  }

  @Test
  public void shouldCallCopyFromPrevious() {
    WriteCache writeCache = mock(WriteCache.class);
    PhpWriteCacheImpl cache = new PhpWriteCacheImpl(writeCache);

    cache.copyFromPrevious("key");

    verify(writeCache).copyFromPrevious("key");
  }
}
