package com.blackrook.json.converters;

import java.util.TimeZone;

/**
 * A JSON date/time converter that uses the format <code>yyyy-MM-dd'T'HH:mm:ss.SSS'Z'</code> in UTC
 * time as the format.
 * @author Matthew Tropiano
 * @since [NOW]
 */
public class JSONISODateTimeConverter extends JSONDateFormatConverter 
{
	/**
	 * Creates a new converter.
	 */
	public JSONISODateTimeConverter()
	{
		super("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone("UTC"));
	}

}
