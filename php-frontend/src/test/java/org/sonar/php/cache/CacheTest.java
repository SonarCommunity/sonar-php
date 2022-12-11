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

import java.util.List;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.php.symbols.FunctionSymbolData;
import org.sonar.php.symbols.LocationInFileImpl;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.cache.CacheContext;
import org.sonar.plugins.php.api.cache.PhpReadCache;
import org.sonar.plugins.php.api.cache.PhpWriteCache;
import org.sonar.plugins.php.api.symbols.QualifiedName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class CacheTest {

  private static final String CACHE_KEY_DATA = "php.projectSymbolData.data:projectKey:";
  private static final String CACHE_KEY_STRING_TABLE = "php.projectSymbolData.stringTable:projectKey:";
  private static final String PLUGIN_VERSION = "1.2.3";
  private static final String PROJECT_KEY = "projectKey";
  private static final InputFile DEFAULT_INPUT_FILE = inputFile("default");


  private PhpWriteCache writeCache = mock(PhpWriteCache.class);
  private PhpReadCache readCache = mock(PhpReadCache.class);

  @Test
  public void shouldWriteToCacheOnlyIfItsEnabled() {
    CacheContext context = new CacheContextImpl(true, writeCache, readCache, PLUGIN_VERSION, PROJECT_KEY);
    Cache cache = new Cache(context);
    SymbolTableImpl data = exampleSymbolTable();
    cache.write(DEFAULT_INPUT_FILE, data);

    String cacheFileName = cache.getCacheFileName(DEFAULT_INPUT_FILE);
    SerializationResult binary = SymbolTableSerializer.toBinary(new SerializationInput(data, PLUGIN_VERSION));

    verify(writeCache).writeBytes(CACHE_KEY_DATA + cacheFileName, binary.data());
    verify(writeCache).writeBytes(CACHE_KEY_STRING_TABLE + cacheFileName, binary.stringTable());
  }

  @Test
  public void shouldWriteToCacheOnceIfHashIsIdentical() {
    CacheContext context = new CacheContextImpl(true, writeCache, readCache, PLUGIN_VERSION, PROJECT_KEY);
    Cache cache = new Cache(context);
    SymbolTableImpl data = exampleSymbolTable();

    cache.write(DEFAULT_INPUT_FILE, data);
    cache.write(DEFAULT_INPUT_FILE, data);

    String cacheFileName = cache.getCacheFileName(DEFAULT_INPUT_FILE);
    SerializationResult binary = SymbolTableSerializer.toBinary(new SerializationInput(data, PLUGIN_VERSION));

    verify(writeCache, times(1)).writeBytes(CACHE_KEY_DATA + cacheFileName, binary.data());
  }

  @Test
  public void shouldNotWriteToCacheIfItsDisabled() {
    CacheContext context = new CacheContextImpl(false, writeCache, readCache, PLUGIN_VERSION, PROJECT_KEY);
    Cache cache = new Cache(context);
    SymbolTableImpl data = emptySymbolTable();

    cache.write(DEFAULT_INPUT_FILE, data);

    verifyZeroInteractions(writeCache);
  }

  @Test
  public void shouldReadFromCache() {
    CacheContext context = new CacheContextImpl(true, writeCache, readCache, PLUGIN_VERSION, PROJECT_KEY);
    Cache cache = new Cache(context);
    SymbolTableImpl data = exampleSymbolTable();
    warmupReadCache(cache, data);

    SymbolTableImpl actual = cache.read(DEFAULT_INPUT_FILE);
    assertThat(actual).isEqualToComparingFieldByFieldRecursively(data);
  }

  @Test
  public void shouldReturnNullWhenDataCacheEntryDoesNotExist() {
    CacheContext context = new CacheContextImpl(true, writeCache, readCache, PLUGIN_VERSION, PROJECT_KEY);
    Cache cache = new Cache(context);

    String cacheFileName = cache.getCacheFileName(DEFAULT_INPUT_FILE);

    SymbolTableImpl data = exampleSymbolTable();
    SerializationInput serializationInput = new SerializationInput(data, PLUGIN_VERSION);
    SerializationResult serializationData = SymbolTableSerializer.toBinary(serializationInput);

    when(readCache.readBytes(CACHE_KEY_DATA + cacheFileName)).thenReturn(null);
    when(readCache.readBytes(CACHE_KEY_STRING_TABLE + cacheFileName)).thenReturn(serializationData.stringTable());

    SymbolTableImpl actual = cache.read(DEFAULT_INPUT_FILE);

    assertThat(actual).isNull();
  }

  @Test
  public void shouldReturnNullWhenStringTableCacheEntryDoesNotExist() {
    CacheContext context = new CacheContextImpl(true, writeCache, readCache, PLUGIN_VERSION, PROJECT_KEY);
    Cache cache = new Cache(context);

    String cacheFileName = cache.getCacheFileName(DEFAULT_INPUT_FILE);

    SymbolTableImpl data = exampleSymbolTable();
    SerializationInput serializationInput = new SerializationInput(data, PLUGIN_VERSION);
    SerializationResult serializationData = SymbolTableSerializer.toBinary(serializationInput);

    when(readCache.readBytes(CACHE_KEY_DATA + cacheFileName)).thenReturn(serializationData.data());
    when(readCache.readBytes(CACHE_KEY_STRING_TABLE + cacheFileName)).thenReturn(null);

    SymbolTableImpl actual = cache.read(DEFAULT_INPUT_FILE);

    assertThat(actual).isNull();
  }

  @Test
  public void shouldReturnNullWhenCacheDisabled() {
    CacheContext context = new CacheContextImpl(false, writeCache, readCache, PLUGIN_VERSION, PROJECT_KEY);
    Cache cache = new Cache(context);
    SymbolTableImpl data = exampleSymbolTable();
    warmupReadCache(cache, data);

    SymbolTableImpl actual = cache.read(DEFAULT_INPUT_FILE);

    assertThat(actual).isNull();
  }

  void warmupReadCache(Cache cache, SymbolTableImpl data) {
    SerializationInput serializationInput = new SerializationInput(data, PLUGIN_VERSION);
    SerializationResult serializationData = SymbolTableSerializer.toBinary(serializationInput);

    String cacheFileName = cache.getCacheFileName(DEFAULT_INPUT_FILE);

    when(readCache.readBytes(CACHE_KEY_DATA + cacheFileName)).thenReturn(serializationData.data());
    when(readCache.readBytes(CACHE_KEY_STRING_TABLE + cacheFileName)).thenReturn(serializationData.stringTable());
  }

  private static SymbolTableImpl exampleSymbolTable() {
    SymbolTableImpl data = SymbolTableImpl.create(List.of(), List.of(new FunctionSymbolData(
      new LocationInFileImpl("abc.php", 1, 1, 1, 10),
      QualifiedName.qualifiedName("funcName"),
      List.of(),
      new FunctionSymbolData.FunctionSymbolProperties(false, false)
    )));
    return data;
  }

  private SymbolTableImpl emptySymbolTable() {
    return SymbolTableImpl.create(List.of(), List.of());
  }

  private static InputFile inputFile(String content) {
    return new TestInputFileBuilder("projectKey", "symbols/symbolTable.php")
      .setContents(content)
      .build();
  }
}
