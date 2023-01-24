package com.blackrook.json;

import java.util.HashMap;

import com.blackrook.json.annotation.JSONType;

/**
 * A set of converters for converting Java object types to JSON types, and back.
 * Used in creating {@link JSONObject}s and reading/writing JSON.
 * @author Matthew Tropiano
 * @since [NOW]
 */
public class JSONConverterSet
{
	/** Map of converters. */
	private HashMap<Class<?>, JSONConverter<?>> converters;
	
	/**
	 * Creates a new converter set.
	 */
	public JSONConverterSet()
	{
		this.converters = new HashMap<>();
	}

	/**
	 * Gets a converter for a type.
	 * @param <E> the class type.
	 * @param clazz the class to get the converter for.
	 * @return a converter to use for JSON conversion.
	 */
	@SuppressWarnings("unchecked")
	public <E> JSONConverter<E> getConverter(Class<E> clazz)
	{
		JSONConverter<E> out = null;
		if ((out = (JSONConverter<E>)converters.get(clazz)) == null)
		{
			JSONType jsonType = clazz.getAnnotation(JSONType.class);
			if (jsonType == null)
				return null;
			
			synchronized (converters)
			{
				if ((out = (JSONConverter<E>)converters.get(clazz)) == null)
				{
					try {
						out = (JSONConverter<E>)jsonType.converter().getDeclaredConstructor().newInstance();
						setConverter(clazz, out);
					} catch (Throwable e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		else
			out = (JSONConverter<E>)converters.get(clazz);
		
		return out;
	}

	/**
	 * Sets a converter for a type.
	 * @param <E> the class type.
	 * @param clazz the class to get the converter for.
	 * @param converter the converter to use for JSON conversion.
	 */
	public <E extends Object> void setConverter(Class<E> clazz, JSONConverter<E> converter)
	{
		converters.put(clazz, converter);
	}
	
}
