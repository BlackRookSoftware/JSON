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

import com.blackrook.json.JSONObject;

/**
 * An annotation for telling {@link JSONObject} that this field or method
 * should not be serialized into a JSON construct of any kind.
 * @author Matthew Tropiano
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JSONIgnore
{
	// Empty
}
