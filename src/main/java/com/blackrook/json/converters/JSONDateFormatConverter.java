package com.blackrook.json.converters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.function.Supplier;

import com.blackrook.json.JSONConverter;
import com.blackrook.json.JSONObject;

/**
 * A special converter that converts dates from strings to Dates in JSON member data.
 * @author Matthew Tropiano
 * @since 1.3.0
 */
public class JSONDateFormatConverter implements JSONConverter<Date>
{
	// Must be this way to avoid SimpleDateFormat concurrency issues.
	
	/** Parser. */
	private ThreadLocal<SimpleDateFormat> parser; 
	/** Formatter. */
	private ThreadLocal<SimpleDateFormat> formatter; 

	/**
	 * Creates a new converter using a {@link SimpleDateFormat} string.
	 * @param format the format string.
	 */
	public JSONDateFormatConverter(final String format) 
	{
		this.parser = ThreadLocal.withInitial(() -> new SimpleDateFormat(format));
		this.formatter = ThreadLocal.withInitial(() -> new SimpleDateFormat(format));
	}
	
	/**
	 * Creates a new converter using a {@link SimpleDateFormat} string.
	 * @param format the format string.
	 * @param timeZone the date's target timezone before conversion.
	 */
	public JSONDateFormatConverter(final String format, final TimeZone timeZone) 
	{
		Supplier<SimpleDateFormat> supplier = () -> {
			SimpleDateFormat fmt = new SimpleDateFormat(format);
			fmt.setTimeZone(timeZone);
			return fmt;
		};
		this.parser = ThreadLocal.withInitial(supplier);
		this.formatter = ThreadLocal.withInitial(supplier);
	}
	
	@Override
	public JSONObject getJSONObject(Date object)
	{
		return JSONObject.create(formatter.get().format(object));
	}

	@Override
	public Date getObject(JSONObject jsonObject) 
	{
		try {
			return parser.get().parse(jsonObject.getString());
		} catch (ParseException e) {
			return null;
		}
	}

}
