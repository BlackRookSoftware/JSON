/*******************************************************************************
 * Copyright (c) 2019 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.json;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

import com.blackrook.json.struct.Utils;
import com.blackrook.json.struct.TypeProfileFactory.Profile;
import com.blackrook.json.struct.TypeProfileFactory.Profile.FieldInfo;
import com.blackrook.json.struct.TypeProfileFactory.Profile.MethodInfo;

/**
 * Default converter for all types of objects that are not
 * a part of the annotated conversions.
 * @author Matthew Tropiano
 */
public class JSONDefaultConverter implements JSONConverter<Object>
{
	@Override
	public JSONObject getJSONObject(Object object)
	{
		if (object instanceof Enum)
		{
			return JSONObject.create(((Enum<?>)object).name());
		}
		else if (object instanceof Map<?, ?>)
		{
			JSONObject out = JSONObject.createEmptyObject();
			for (Map.Entry<?, ?> entry : ((Map<?, ?>)object).entrySet())
			{
				String key = String.valueOf(entry.getKey());
				out.addMember(key, entry.getValue());
			}
			return out;
		}
		else if (object instanceof Iterable<?>)
		{
			JSONObject out = JSONObject.createEmptyArray();
			Iterator<?> it = ((Iterable<?>)object).iterator();
			while (it.hasNext())
				out.append(JSONObject.create(it.next()));
			return out;
		}
		else
		{
			Class<?> clz = object.getClass();
			JSONObject out = JSONObject.createEmptyObject();
			Profile<?> profile = JSONObject.PROFILE_FACTORY.getProfile(clz);

			for (Map.Entry<String, MethodInfo> getters : profile.getGetterMethodsByName().entrySet())
			{
				String memberName = getters.getKey();
				String alias = getters.getValue().getAlias();
				Method method = getters.getValue().getMethod();
				out.addMember(Utils.isNull(alias, memberName), Utils.invokeBlind(method, object));
			}
			for (Map.Entry<String, FieldInfo> fields : profile.getPublicFieldsByName().entrySet())
			{
				String memberName = fields.getKey();
				String alias = fields.getValue().getAlias();
				Field field = fields.getValue().getField();
				out.addMember(Utils.isNull(alias, memberName), Utils.getFieldValue(object, field));
			}
			return out;
		}
	}

	/** Returns null. */
	@Override
	public Object getObject(JSONObject jsonObject)
	{
		return null;
	}

}
