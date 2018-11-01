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

package io.neba.core.resourcemodels.metadata;

import io.neba.core.resourcemodels.mapping.testmodels.OtherTestResourceModel;
import io.neba.core.resourcemodels.mapping.testmodels.TestResourceModel;
import io.neba.core.util.OsgiModelSource;
import java.util.Collection;
import net.sf.cglib.proxy.NoOp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.Bundle;


import static net.sf.cglib.proxy.Enhancer.create;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Olaf Otto
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceModelMetaDataRegistrarTest {
    private ResourceModelMetaData metadata;
    private Collection<ResourceModelMetaData> allMetaData;

    private long bundleId = 123L;

    @InjectMocks
    private ResourceModelMetaDataRegistrar testee;

    @Test(expected = IllegalArgumentException.class)
    public void testHandlingOfNullTypeArgument() {
        getMetaDataFor(null);
    }

    @Test(expected = IllegalStateException.class)
    public void testRetrievalOfNonRegisteredResourceModel() {
        getMetaDataFor(getClass());
    }

    @Test
    public void testRetrievalOfRegisteredModelTypes() {
        addModelType(TestResourceModel.class);
        getMetaDataFor(TestResourceModel.class);
        assertMetadataIsNotNull();
    }

    @Test(expected = IllegalStateException.class)
    public void testCacheIsEmptyUponShutdown() {
        addModelType(TestResourceModel.class);
        tearDown();
        getMetaDataFor(TestResourceModel.class);
    }

    @Test(expected = IllegalStateException.class)
    public void testModelMetadataIsGoneAfterRemovalOfBundle() {
        addModelType(TestResourceModel.class);
        removeBundle();
        getMetaDataFor(TestResourceModel.class);
    }

    @Test
    public void testAdditionAsCglibProxyTypeAndRetrievalAsUserType() {
        addModelType(createCglibProxy(TestResourceModel.class));
        getMetaDataFor(TestResourceModel.class);
        assertMetadataIsNotNull();
    }

    @Test
    public void testAdditionAsUserTypeAndRetrievalAsCglibProxyType() {
        addModelType(TestResourceModel.class);
        getMetaDataFor(createCglibProxy(TestResourceModel.class));
        assertMetadataIsNotNull();
    }

    @Test
    public void testAdditionAndRetrievalAsCglibProxyType() {
        addModelType(createCglibProxy(TestResourceModel.class));
        getMetaDataFor(createCglibProxy(TestResourceModel.class));
        assertMetadataIsNotNull();
    }

    @Test
    public void testRetrievalOfAllRegisteredMetaData() {
        addModelType(TestResourceModel.class);
        addModelType(OtherTestResourceModel.class);
        getAllMetadata();

        assertAllMetadataConsistsOfMetadataFor(TestResourceModel.class, OtherTestResourceModel.class);
    }

    @Test
    public void testRetrievalOfAllRegisteredMetaDataIsShallowCopy() {
        addModelType(TestResourceModel.class);
        addModelType(OtherTestResourceModel.class);

        getAllMetadata();
        clearAllMetaData();
        getAllMetadata();

        assertAllMetadataConsistsOfMetadataFor(TestResourceModel.class, OtherTestResourceModel.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullValuesAreNotToleratedForBundleDeRegistration() {
        this.testee.removeMetadataForModelsIn(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullValuesAreNotToleratedForModelRegistration() {
        this.testee.register(null);
    }

    private void clearAllMetaData() {
        this.allMetaData.clear();
    }

    private void assertAllMetadataConsistsOfMetadataFor(Class<?>... types) {
        assertThat(this.allMetaData).extracting("typeName").containsOnly((Object[]) typeNamesOf(types));
    }

    private void getAllMetadata() {
        this.allMetaData = this.testee.get();
    }

    private Class<?> createCglibProxy(Class<TestResourceModel> modelType) {
        return create(modelType, NoOp.INSTANCE).getClass();
    }

    private void removeBundle() {
        Bundle bundle = mock(Bundle.class);
        when(bundle.getBundleId()).thenReturn(this.bundleId);
        this.testee.removeMetadataForModelsIn(bundle);
    }

    private void tearDown() {
        this.testee.deactivate();
    }

    private void assertMetadataIsNotNull() {
        assertThat(this.metadata).isNotNull();
    }

    private void addModelType(Class<?> modelType) {
        @SuppressWarnings("unchecked")
        OsgiModelSource<Object> source = mock(OsgiModelSource.class);
        doReturn(modelType).when(source).getModelType();
        doReturn(this.bundleId).when(source).getBundleId();
        this.testee.register(source);
    }

    private void getMetaDataFor(Class<?> modelType) {
        this.metadata = this.testee.get(modelType);
    }

    private String[] typeNamesOf(Class<?>... types) {
        if (types == null) {
            return null;
        }

        String[] names = new String[types.length];
        for (int i = 0; i < types.length; ++i) {
            names[i] = types[i].getCanonicalName();
        }
        return names;
    }
}

