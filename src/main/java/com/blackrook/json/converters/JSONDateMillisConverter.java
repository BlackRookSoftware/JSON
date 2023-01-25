package com.blackrook.json.converters;

import java.util.Date;

import com.blackrook.json.JSONConverter;
import com.blackrook.json.JSONObject;

/**
 * A special converter that converts dates from milliseconds since Epoch to Dates in JSON member data.
 * @author Matthew Tropiano
 * @since [NOW]
 */
public class JSONDateMillisConverter implements JSONConverter<Date>
{
	/**
	 * Creates a new converter.
	 */
	public JSONDateMillisConverter() {}
	
	@Override
	public JSONObject getJSONObject(Date object)
	{
		return JSONObject.create(object.getTime());
	}

	@Override
	public Date getObject(JSONObject jsonObject) 
	{
		return new Date(jsonObject.getLong());
	}

}
