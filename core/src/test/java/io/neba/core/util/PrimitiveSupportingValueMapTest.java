/*
  Copyright 2013 the original author or authors.

  Licensed under the Apache License, Version 2.0 the "License";
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package io.neba.core.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * @author Olaf Otto
 */
@RunWith(MockitoJUnitRunner.class)
public class PrimitiveSupportingValueMapTest {
	@Mock
	private ValueMap wrapped;

	private Object result;

    private PrimitiveSupportingValueMap testee;

    @Before
    public void prepareValueMap() {
        this.testee = new PrimitiveSupportingValueMap(this.wrapped);
    }

    @Test
    public void testPrimitiveBooleanSupport() {
        with(true);
        get(boolean.class);
        assertResultIs(true);
    }

    @Test
    public void testPrimitiveIntSupport() {
        with(1);
        get(int.class);
        assertResultIs(1);
    }

    @Test
    public void testPrimitiveLongSupport() {
        with(1L);
        get(long.class);
        assertResultIs(1L);
    }

    @Test
    public void testPrimitiveDoubleSupport() {
        with(1D);
        get(double.class);
        assertResultIs(1D);
    }

    @Test
    public void testPrimitiveFloatSupport() {
        with(1F);
        get(float.class);
        assertResultIs(1F);
    }

    @Test
    public void testPrimitiveByteSupport() {
        byte b = 1;
        with(b);
        get(byte.class);
        assertResultIs(b);
    }

    @Test
    public void testPrimitiveCharSupport() {
        char c = 1;
        with(c);
        get(char.class);
        assertResultIs(c);
    }

    @Test
    public void testPrimitiveShortSupport() {
        short s = 1;
        with(s);
        get(short.class);
        assertResultIs(s);
    }

    @Test
    public void testDefaultValueIsNotUsedIfValueExists() {
        with(1F);
        get(2F);
        assertResultIs(1F);
    }

    @Test
    public void testFallbackValueIsUsedIfValueDoesNotExist() {
        get(2F);
        assertResultIs(2F);
    }

    @Test
    public void testUntypedGetWithValue() {
        with("JUnitTestValue");
        get();
        assertResultIs("JUnitTestValue");
    }

    @Test
    public void testKeySetDelegation() {
        withKeySet("key");
        assertKeySetIs("key");
    }

    @Test
    public void testValuesDelegation() {
        withValues("value");
        assertValuesAre("value");
    }

    @Test
    public void testEntrySetDelegation() {
        withEntries("key", "value");
        assertEntriesAre("key", "value");
    }

    @Test
    public void testIsEmpty() {
        withEmptyWrappedValueMap();
        assertMapIsEmpty();
    }

    @Test
    public void testSize() {
        withWrappedSize(12);
        assertSizeIs(12);
    }

    @Test
    public void testContainsKey() {
        withWrappedContainingKey("key");
        assertKeyIsContained("key");
    }

    @Test
    public void testContainsValue() {
        withWrappedContainingValue("value");
        assertValueIsContained("value");
    }

    private void assertValueIsContained(String value) {
        assertThat(this.testee.containsValue(value)).isTrue();
    }

    private void withWrappedContainingValue(String value) {
        doReturn(true).when(this.wrapped).containsValue(value);
    }

    private void assertKeyIsContained(String key) {
        assertThat(this.testee.containsKey(key)).isTrue();
    }

    private void withWrappedContainingKey(String key) {
        doReturn(true).when(this.wrapped).containsKey(key);
    }

    private void assertSizeIs(int expected) {
        assertThat(this.testee.size()).isEqualTo(expected);
    }

    private void withWrappedSize(int size) {
        doReturn(size).when(this.wrapped).size();
    }

    private void assertMapIsEmpty() {
        assertThat(this.testee.isEmpty()).isTrue();
    }

    private void withEmptyWrappedValueMap() {
        doReturn(true).when(wrapped).isEmpty();
    }

    private void assertEntriesAre(String key, String value) {
        assertThat(this.testee.entrySet()).extracting("value").contains(value);
        assertThat(this.testee.entrySet()).extracting("key").contains(key);
    }

    @SuppressWarnings("unchecked")
    private void withEntries(String key, String value) {
        Set<Map.Entry<String, String>> entrySet = new HashSet<>();
        Map.Entry entry = mock(Map.Entry.class);
        doReturn(key).when(entry).getKey();
        doReturn(value).when(entry).getValue();
        entrySet.add(entry);

        doReturn(entrySet).when(this.wrapped).entrySet();
    }

    private void assertValuesAre(String value) {
        assertThat(this.testee.values()).containsOnly(value);
    }

    private void withValues(String value) {
        Set<String> values = new HashSet<>();
        values.add(value);
        doReturn(values).when(this.wrapped).values();
    }

    private void assertKeySetIs(String key) {
        assertThat(this.testee.keySet()).containsOnly(key);
    }

    private void withKeySet(String key) {
        Set<String> keySet = new HashSet<>();
        keySet.add(key);
        doReturn(keySet).when(this.wrapped).keySet();
    }

    @Test
    public void testUntypedGetWithoutValue() {
        get();
        assertResultIsNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandlingOfNullArgumentToConstructor() {
        new PrimitiveSupportingValueMap(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullArgumentKeyToGetWithClass() {
        this.testee.get(null, String.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullArgumentClassToGetWithClass() {
        this.testee.get("test", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullArgumentKeyToGetWithDefaultValue() {
        this.testee.get(null, "defaultValue");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullArgumentDefaultValueKeyToGetWithDefaultValue() {
        this.testee.get("test", (Object) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullArgumentToSimpleGet() {
        this.testee.get(null);
    }

    private void assertResultIsNull() {
        assertThat(this.result).isNull();
    }

    private void assertResultIs(Object expected) {
        assertThat(this.result).isEqualTo(expected);
    }

    private <T> void get(Class<T> type) {
        this.result = this.testee.get("test", type);
    }

    private <T> void get(T defaultValue) {
        this.result = this.testee.get("test", defaultValue);
    }

    private void get() {
        this.result = this.testee.get("test");
    }

    private <T> void with(T value) {
        when(this.wrapped.get(eq("test"), eq(typeOf(value)))).thenReturn(value);
        when(this.wrapped.get(eq("test"))).thenReturn(value);
    }
    
	@SuppressWarnings("unchecked")
	private <T> Class<T> typeOf(T value) {
		return (Class<T>) value.getClass();
	}
}
