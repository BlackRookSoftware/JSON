/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.json;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default converter for all types of objects that are not
 * a part of the annotated conversions. The default converter is meant to cover
 * {@link Enum} types, {@link Map} types, and other types that implement {@link Iterable}, such as
 * {@link List}s, {@link Set}s, and other implementations of {@link Collection}.
 * <p>
 * This is a one-way converter - this goes from Java object to JSON, and not back. It is not
 * recommended to use this converter directly, as this is a catch-all for common conversions
 * without explicit structure.
 * @author Matthew Tropiano
 */
public class JSONDefaultConverter implements JSONConverter<Object>
{
	@Override
	public JSONObject getJSONObject(Object object)
	{
		return JSONObject.createFromObject(object, JSONObject.GLOBAL_CONVERTER_SET);
	}

	/** Returns null. */
	@Override
	public Object getObject(JSONObject jsonObject)
	{
		return null;
	}

}
