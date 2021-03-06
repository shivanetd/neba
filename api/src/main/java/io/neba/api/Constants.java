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

package io.neba.api;

/**
 * Overall constants for the NEBA API.
 * 
 * @author Olaf Otto
 */
public class Constants {
    /**
     * Models provided by the {@link io.neba.api.tags.DefineObjectsTag} tag and models
     * returned from controllers (i.e. stored in a ModelAndView) are provided
     * under this key.
     */
    public static final String MODEL = "m";

    /**
     * This virtual resource type is considered the root of the type hierarchy
     * of all synthetic resource types. This supports mapping a resource model
     * to all resources <em>including</em> synthetic resources.
     */
    public static final String SYNTHETIC_RESOURCETYPE_ROOT = "neba:syntheticResourcetypeRoot";
    
    private Constants() {
    }
}
