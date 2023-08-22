/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.php.utils.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class IteratorUtilsTest {

  @Test
  void testReturnSingletonIterator() {
    Iterator<String> singletonIterator = IteratorUtils.iteratorOf("element");

    assertThat(singletonIterator.hasNext()).isTrue();
    assertThat(singletonIterator.next()).isEqualTo("element");
    assertThat(singletonIterator.hasNext()).isFalse();

    assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(singletonIterator::next);
  }

  @Test
  void testReturnIteratorOfMultipleElements() {
    Iterator<String> singletonIterator = IteratorUtils.iteratorOf("element1", "element2");

    assertThat(singletonIterator.hasNext()).isTrue();
    assertThat(singletonIterator.next()).isEqualTo("element1");
    assertThat(singletonIterator.hasNext()).isTrue();
    assertThat(singletonIterator.next()).isEqualTo("element2");
    assertThat(singletonIterator.hasNext()).isFalse();
    assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(singletonIterator::next);
  }

  @Test
  void testReturnConcatenatedIterators() {
    Iterator<String> firstIterator = IteratorUtils.iteratorOf("firstElement");
    Iterator<String> secondIterator = IteratorUtils.iteratorOf("secondElement", "thirdElement");
    Iterator<String> iterator = IteratorUtils.concat(firstIterator, secondIterator);

    assertThat(iterator.hasNext()).isTrue();
    assertThat(iterator.next()).isEqualTo("firstElement");
    assertThat(iterator.next()).isEqualTo("secondElement");
    assertThat(iterator.next()).isEqualTo("thirdElement");
    assertThat(iterator.hasNext()).isFalse();
    assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(iterator::next);
  }

  @Test
  void testIteratorConcatWithNull() {
    Iterator<String> iterator = IteratorUtils.concat((Iterator<String>) null);

    assertThat(iterator.hasNext()).isFalse();
    assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(iterator::next);
  }
}
