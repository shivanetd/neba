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

import org.assertj.core.api.AbstractAssert;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Olaf Otto
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ConcurrentMultiValueMapAssert extends AbstractAssert<ConcurrentMultiValueMapAssert, ConcurrentDistinctMultiValueMap> {

    public static ConcurrentMultiValueMapAssert assertThat(ConcurrentDistinctMultiValueMap<?, ?> map) {
        return new ConcurrentMultiValueMapAssert(map);
    }

    public ConcurrentMultiValueMapAssert contains(Object key, Object value) {
        Collection<?> values = valuesForKey(key);
        if (!values.contains(value)) {
            failWithMessage(values + " does not contain the value " + value + ".");
        }
        return myself;
    }

    public ConcurrentMultiValueMapAssert contains(Object key, Object... values) {
        Collection<?> containedValues = valuesForKey(key);
        if (!containedValues.containsAll(Arrays.asList(values))) {
            failWithMessage(Arrays.toString(values) + " do not contain the values " + Arrays.toString(values) + ".");
        }
        return myself;
    }

    public ConcurrentMultiValueMapAssert containsOnly(Object key, Object... values) {
        Collection<?> containedValues = valuesForKey(key);
        if (containedValues.size() != values.length || !containedValues.containsAll(Arrays.asList(values))) {
            failWithMessage("Expected " + containedValues + " to only contain " + Arrays.toString(values) + ".");
        }
        return myself;
    }

    void containsExactlyOneValueFor(Object key) {
        Collection<?> values = valuesOrFail(key);
        if (values.size() != 1) {
            failWithMessage("Expected exactly one value for " + key + ", but got " + values + ".");
        }
    }

    void doesNotContain(Object key) {
        if (this.actual.get(key) != null) {
            failWithMessage(this.actual + " does contains the key " + key + ".");
        }
    }

    private Collection<?> valuesForKey(Object key) {
        return valuesOrFail(key);
    }

    private ConcurrentMultiValueMapAssert(ConcurrentDistinctMultiValueMap map) {
        super(map, ConcurrentMultiValueMapAssert.class);
    }

    private Collection<?> valuesOrFail(Object key) {
        Collection<?> values = this.actual.get(key);
        if (values == null) {
            failWithMessage(this.actual + " does not contain the key " + key + ".");
        }
        return values;
    }
}
