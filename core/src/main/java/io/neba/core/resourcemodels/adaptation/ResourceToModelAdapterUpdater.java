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

package io.neba.core.resourcemodels.adaptation;

import io.neba.core.resourcemodels.registration.ModelRegistry;
import io.neba.core.util.OsgiModelSource;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.apache.commons.lang3.ClassUtils.getAllInterfaces;
import static org.apache.commons.lang3.ClassUtils.getAllSuperclasses;
import static org.apache.sling.api.adapter.AdapterFactory.ADAPTABLE_CLASSES;
import static org.apache.sling.api.adapter.AdapterFactory.ADAPTER_CLASSES;
import static org.osgi.framework.Bundle.ACTIVE;
import static org.osgi.framework.Bundle.STARTING;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * An {@link AdapterFactory} provides the {@link AdapterFactory#ADAPTABLE_CLASSES type(s) it adapts from}
 * and the {@link AdapterFactory#ADAPTER_CLASSES types it can adapt to} as OSGi service
 * properties. This information is used by {@link org.apache.sling.api.adapter.Adaptable} types to
 * {@link org.apache.sling.api.adapter.Adaptable#adaptTo(Class) adapt to}
 * other types, i.e. is essentially a factory pattern.
 * <br />
 * This service registers the {@link ResourceToModelAdapter} as
 * an {@link AdapterFactory} OSGi service and dynamically updates the before mentioned
 * service properties with regard to the resource models detected by the
 * {@link io.neba.core.resourcemodels.registration.ModelRegistrar}.
 * This enables direct {@link Resource#adaptTo(Class) adaptation} to the resource
 * models without having to provide all available models as service metadata at build time.
 *
 * @author Olaf Otto
 */
@Component(service = ResourceToModelAdapterUpdater.class)
public class ResourceToModelAdapterUpdater {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    private ModelRegistry registry;
    @Reference
    private ResourceToModelAdapter adapter;

    private BundleContext context = null;
    private ServiceRegistration resourceToModelAdapterRegistration = null;
    private ExecutorService executorService;

    @Activate
    protected void activate(BundleContext context) {
        this.context = context;
        this.executorService = newSingleThreadExecutor();
        registerModelAdapter();
    }

    @Deactivate
    protected void deActivate() {
        this.executorService.shutdownNow();
    }

    public void refresh() {
        this.executorService.execute(() -> {
            if (isModelAdapterUpdatable()) {
                updateModelAdapter();
            }
        });
    }

    void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * Depending on the bundle lifecycle, an OSGi service may not always be
     * updatable.
     *
     * @return true if the {@link ResourceToModelAdapter} OSGi service may be altered.
     */
    private boolean isModelAdapterUpdatable() {
        int bundleState = this.context.getBundle().getState();
        return bundleState == ACTIVE || bundleState == STARTING;
    }

    /**
     * Sling does not detect changes to the state of an {@link AdapterFactory} service. Their
     * properties are only read when the service is registered. Thus
     * the service is unregistered and re-registered when changing its properties
     * (e.g. adding new adaptable types).
     */
    private void updateModelAdapter() {
        unregisterModelAdapter();
        registerModelAdapter();
    }

    /**
     * {@link BundleContext#registerService(String, Object, Dictionary) Registers}
     * the {@link ResourceToModelAdapter}, i.e. publishes it as an OSGi service.
     */
    private void registerModelAdapter() {
        Dictionary<String, Object> properties = createResourceToModelAdapterProperties();
        String serviceClassName = AdapterFactory.class.getName();
        this.resourceToModelAdapterRegistration = this.context.registerService(serviceClassName, this.adapter, properties);
    }

    private void unregisterModelAdapter() {
        try {
            this.resourceToModelAdapterRegistration.unregister();
        } catch (IllegalStateException e) {
            this.logger.info("The resource to model adapter was already unregistered, ignoring.", e);
        }
    }

    private Dictionary<String, Object> createResourceToModelAdapterProperties() {
        Dictionary<String, Object> properties = new Hashtable<>();
        Set<String> fullyQualifiedNamesOfRegisteredModels = getAdapterTypeNames();
        properties.put(ADAPTER_CLASSES, fullyQualifiedNamesOfRegisteredModels.toArray());
        properties.put(ADAPTABLE_CLASSES, new String[]{Resource.class.getName()});
        properties.put(SERVICE_VENDOR, "neba.io");
        properties.put(SERVICE_DESCRIPTION, "Adapts Resources to @ResourceModels.");
        return properties;
    }

    /**
     * Obtains all {@link OsgiModelSource model sources} from the
     * {@link io.neba.core.resourcemodels.registration.ModelRegistrar} and adds the {@link OsgiModelSource#getModelType()
     * model type name} as well as the type name of all of its superclasses and
     * interfaces to the set.
     *
     * @return never null but rather an empty set.
     * @see org.apache.commons.lang3.ClassUtils#getAllInterfaces(Class)
     * @see org.apache.commons.lang3.ClassUtils#getAllSuperclasses(Class)
     */
    @SuppressWarnings("unchecked")
    private Set<String> getAdapterTypeNames() {
        List<OsgiModelSource<?>> modelSources = this.registry.getModelSources();
        Set<String> modelNames = new HashSet<>();
        for (OsgiModelSource<?> source : modelSources) {
            Class<?> c = source.getModelType();
            modelNames.add(c.getName());
            modelNames.addAll(toClassnameList(getAllInterfaces(c)));
            List<Class<?>> allSuperclasses = getAllSuperclasses(c);
            // Remove Object.class - it is always the topmost element.
            allSuperclasses.remove(allSuperclasses.size() - 1);
            modelNames.addAll(toClassnameList(allSuperclasses));
        }
        return modelNames;
    }

    private Collection<String> toClassnameList(List<Class<?>> l) {
        List<String> classNames = new ArrayList<>(l.size());
        classNames.addAll(l.stream().map(Class::getName).collect(Collectors.toList()));
        return classNames;
    }
}