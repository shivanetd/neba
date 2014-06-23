/**
 * Copyright 2013 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
**/

package io.neba.api.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * <p>
 * Provides a {@link java.util.Collection}, {@link java.util.List} or {@link java.util.Set}
 * of children of the resource. The resource might be the current resource itself,
 * a resource designated by a {@link io.neba.api.annotations.Path} annotation,
 * or a {@link io.neba.api.annotations.Reference referenced} resource.
 * </p>
 * <br/>
 *
 * Children of the current resource
 * <pre>
 * &#64;{@link Children}
 * private List&lt;Resource&gt; children;
 * </pre>
 *
 * Children of the current resource adapted to Page
 * <pre>
 * &#64;{@link Children}
 * private List&lt;Page&gt; pages;
 * </pre>
 *
 * Children of the resource at the path "/path/to/resource"
 * <pre>
 * &#64;{@link Path}("/path/to/resource")
 * &#64;{@link Children}
 * private List&lt;Page&gt; pages;
 * </pre>
 *
 * Children of the resource referenced in the property named "pages"
 * <pre>
 * &#64;{@link Reference}
 * &#64;{@link Children}
 * private List&lt;Page&gt; pages;
 * </pre>
 *
 * Children of the resource referenced in the property named "propContainingReference"
 * <pre>
 * &#64;{@link Path}("propContainingReference")
 * &#64;{@link Reference}
 * &#64;{@link Children}
 * private List&lt;Page&gt; pages;
 * </pre>
 *
 * @author Olaf Otto
 * @author Daniel Rey
 */
@Documented
@Target(FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Children {
}