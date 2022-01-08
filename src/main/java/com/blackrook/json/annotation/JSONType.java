/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
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

import com.blackrook.json.JSONConverter;
import com.blackrook.json.JSONDefaultConverter;
import com.blackrook.json.JSONObject;

/**
 * An annotation for telling {@link JSONObject} that this object type
 * needs special rules for conversion (both to and from this type).
 * @author Matthew Tropiano
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JSONType
{
	/** 
	 * This class's converter class.
	 * @return the JSONConverter class to use. 
	 */
	Class<? extends JSONConverter<Object>> converter() default JSONDefaultConverter.class;
}
