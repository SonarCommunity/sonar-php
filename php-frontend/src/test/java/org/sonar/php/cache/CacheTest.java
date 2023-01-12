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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class CacheTest {

  private static final InputFile DEFAULT_INPUT_FILE = inputFile("default");
  private static final String CACHE_KEY_DATA = "php.projectSymbolData.data:" + DEFAULT_INPUT_FILE.key();
  private static final String CACHE_KEY_STRING_TABLE = "php.projectSymbolData.stringTable:" + DEFAULT_INPUT_FILE.key();
  private static final String CACHE_KEY_HASH = "php.contentHashes:" + DEFAULT_INPUT_FILE.key();
  private static final String PLUGIN_VERSION = "1.2.3";

  private final PhpWriteCache writeCache = mock(PhpWriteCache.class);
  private final PhpReadCache readCache = mock(PhpReadCache.class);

  @Test
  public void shouldWriteToCacheOnlyIfItsEnabled() {
    CacheContext context = new CacheContextImpl(true, writeCache, readCache, PLUGIN_VERSION);
    Cache cache = new Cache(context);
    SymbolTableImpl data = exampleSymbolTable();
    cache.writeFileSymbolTable(DEFAULT_INPUT_FILE, data);

    SerializationResult binary = SymbolTableSerializer.toBinary(new SymbolTableSerializationInput(data, PLUGIN_VERSION));

    verify(writeCache).writeBytes(CACHE_KEY_DATA, binary.data());
    verify(writeCache).writeBytes(CACHE_KEY_STRING_TABLE, binary.stringTable());
  }

  @Test
  public void shouldNotWriteToCacheIfItsDisabled() {
    CacheContext context = new CacheContextImpl(false, writeCache, readCache, PLUGIN_VERSION);
    Cache cache = new Cache(context);
    SymbolTableImpl data = emptySymbolTable();

    cache.writeFileSymbolTable(DEFAULT_INPUT_FILE, data);

    verifyNoInteractions(writeCache);
  }

  @Test
  public void shouldReadFromCache() {
    CacheContext context = new CacheContextImpl(true, writeCache, readCache, PLUGIN_VERSION);
    Cache cache = new Cache(context);
    SymbolTableImpl data = exampleSymbolTable();
    warmupReadCache(data);

    SymbolTableImpl actual = cache.read(DEFAULT_INPUT_FILE);
    assertThat(actual).isEqualToComparingFieldByFieldRecursively(data);
  }

  @Test
  public void shouldReturnNullWhenDataCacheEntryDoesNotExist() {
    CacheContext context = new CacheContextImpl(true, writeCache, readCache, PLUGIN_VERSION);
    Cache cache = new Cache(context);


    SymbolTableImpl data = exampleSymbolTable();
    SymbolTableSerializationInput serializationInput = new SymbolTableSerializationInput(data, PLUGIN_VERSION);
    SerializationResult serializationData = SymbolTableSerializer.toBinary(serializationInput);

    when(readCache.readBytes(CACHE_KEY_DATA)).thenReturn(null);
    when(readCache.readBytes(CACHE_KEY_STRING_TABLE)).thenReturn(serializationData.stringTable());

    SymbolTableImpl actual = cache.read(DEFAULT_INPUT_FILE);

    assertThat(actual).isNull();
  }

  @Test
  public void shouldReturnNullWhenStringTableCacheEntryDoesNotExist() {
    CacheContext context = new CacheContextImpl(true, writeCache, readCache, PLUGIN_VERSION);
    Cache cache = new Cache(context);

    SymbolTableImpl data = exampleSymbolTable();
    SymbolTableSerializationInput serializationInput = new SymbolTableSerializationInput(data, PLUGIN_VERSION);
    SerializationResult serializationData = SymbolTableSerializer.toBinary(serializationInput);

    when(readCache.readBytes(CACHE_KEY_DATA)).thenReturn(serializationData.data());
    when(readCache.readBytes(CACHE_KEY_STRING_TABLE)).thenReturn(null);

    SymbolTableImpl actual = cache.read(DEFAULT_INPUT_FILE);

    assertThat(actual).isNull();
  }

  @Test
  public void shouldReturnNullWhenCacheDisabled() {
    CacheContext context = new CacheContextImpl(false, writeCache, readCache, PLUGIN_VERSION);
    Cache cache = new Cache(context);
    SymbolTableImpl data = exampleSymbolTable();
    warmupReadCache(data);

    SymbolTableImpl actual = cache.read(DEFAULT_INPUT_FILE);

    assertThat(actual).isNull();
  }

  @Test
  public void readFileContentHash() {
    CacheContext context = new CacheContextImpl(true, writeCache, readCache, PLUGIN_VERSION);
    Cache cache = new Cache(context);
    byte[] hash = "hash".getBytes();

    when(readCache.readBytes(CACHE_KEY_HASH)).thenReturn(hash);

    assertThat(cache.readFileContentHash(DEFAULT_INPUT_FILE)).isEqualTo(hash);
  }

  @Test
  public void writeFileContentHash() {
    CacheContext context = new CacheContextImpl(true, writeCache, readCache, PLUGIN_VERSION);
    Cache cache = new Cache(context);
    byte[] hash = "hash".getBytes();
    cache.writeFileContentHash(DEFAULT_INPUT_FILE, hash);

    verify(writeCache).writeBytes(CACHE_KEY_HASH, hash);
  }

  void warmupReadCache(SymbolTableImpl data) {
    SymbolTableSerializationInput serializationInput = new SymbolTableSerializationInput(data, PLUGIN_VERSION);
    SerializationResult serializationData = SymbolTableSerializer.toBinary(serializationInput);

    when(readCache.readBytes(CACHE_KEY_DATA)).thenReturn(serializationData.data());
    when(readCache.readBytes(CACHE_KEY_STRING_TABLE)).thenReturn(serializationData.stringTable());
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
