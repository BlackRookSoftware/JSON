/*******************************************************************************
 * Copyright (c) 2019-2023 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.json.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.blackrook.json.JSONConversionException;
import com.blackrook.json.JSONObject;

/**
 * Provides a hint to the default deserializer that the generic type for
 * a collection takes a specific single type (Lists, Sets). 
 * <p>Java's Generics are not reified as class data, and cannot be queried via reflection, hence why this hint exists.
 * <p> If the type to deserialize to is an interface or abstract class, the following
 * implementations are used for the underlying collection type (searched in this order):
 * <ul>
 *     <li><b>List&lt;T&gt;</b> - ArrayList&lt;T&gt;</li>
 *     <li><b>SortedSet&lt;T&gt;</b> - TreeSet&lt;T&gt;</li>
 *     <li><b>Set&lt;T&gt;</b> - HashSet&lt;T&gt;</li>
 *     <li><b>Iterable&lt;T&gt;</b> - ArrayList&lt;T&gt;</li>
 *     <li><b>Collection&lt;T&gt;</b> - ArrayList&lt;T&gt;</li>
 * </ul>
 * If not a Collection, a {@link JSONConversionException} will occur.
 * @since 1.1.0
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JSONCollectionType
{
	/** @return the Generic type for the collection. */
	Class<?> value() default JSONObject.class;
}
