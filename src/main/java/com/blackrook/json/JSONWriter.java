/*******************************************************************************
 * Copyright (c) 2019-2020 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * A class for writing JSON data to JSON representation.
 * @author Matthew Tropiano
 */
public final class JSONWriter
{
	private static final Charset UTF_8 = Charset.forName("UTF-8");

	/**
	 * Writes a JSONObject out to the following output stream.
	 * @param jsonObject the object to write.
	 * @param out the output stream to write to.
	 * @throws IOException if a write error occurs.
	 */
	public static void writeJSON(JSONObject jsonObject, OutputStream out) throws IOException
	{
		(new WriterContext(jsonObject, out)).startWrite();
	}

	/**
	 * Writes a JSONObject out to the following output stream.
	 * @param jsonObject the object to write.
	 * @param writer the writer to write to.
	 * @throws IOException if a write error occurs.
	 */
	public static void writeJSON(JSONObject jsonObject, Writer writer) throws IOException
	{
		(new WriterContext(jsonObject, writer)).startWrite();
	}

	/**
	 * Writes a JSONObject out to the following output stream.
	 * @param jsonObject the object to write.
	 * @return the JSONObject as a JSON string. 
	 * @throws IOException if a write error occurs.
	 */
	public static String writeJSONString(JSONObject jsonObject) throws IOException
	{
		StringWriter sw = new StringWriter();
		writeJSON(jsonObject, sw);
		return sw.toString();
	}

	/**
	 * Writes a JSONObject out to the following output stream.
	 * @param object the object to write.
	 * @param out the output stream to write to.
	 * @throws IOException if a write error occurs.
	 */
	public static void writeJSON(Object object, OutputStream out) throws IOException
	{
		writeJSON(JSONObject.create(object), out);
	}

	/**
	 * Writes a JSONObject out to the following output stream.
	 * @param object the object to write.
	 * @param writer the writer to write to.
	 * @throws IOException if a write error occurs.
	 */
	public static void writeJSON(Object object, Writer writer) throws IOException
	{
		writeJSON(JSONObject.create(object), writer);
	}

	/**
	 * Writes a JSONObject out to the following output stream.
	 * @param object the object to write.
	 * @return the JSONObject as a JSON string. 
	 * @throws IOException if a write error occurs.
	 */
	public static String writeJSONString(Object object) throws IOException
	{
		StringWriter sw = new StringWriter();
		writeJSON(JSONObject.create(object), sw);
		return sw.toString();
	}

	/**
	 * Writer context.
	 */
	private static class WriterContext
	{
		private static final String HEXALPHABET = "0123456789ABCDEF";

		/** Writer. May be null. */
		private Writer writer;
		/** JSON Object. */
		private JSONObject object;
		
		public WriterContext(JSONObject object, Writer writer)
		{
			this.object = object;
			this.writer = writer;
		}

		public WriterContext(JSONObject object, OutputStream outStream)
		{
			this(object, new OutputStreamWriter(outStream, UTF_8));
		}
		
		/**
		 * Starts the write.
		 * @throws IOException if an error occurs on the write.
		 */
		public void startWrite() throws IOException
		{
			writeObject(object);
		}
		
		/**
		 * Writes an object.
		 * @param object the object to write.
		 * @throws IOException if an error occurs on the write.
		 */
		public void writeObject(JSONObject object) throws IOException
		{
			if (object.isUndefined())
				writer.append("undefined");
			else if (object.isNull())
				writer.append("null");
			else if (object.isArray())
			{
				writer.append("[");
				for (int i = 0; i < object.length(); i++)
				{
					writeObject(object.get(i));
					if (i < object.length() - 1)
						writer.append(",");
				}
				writer.append("]");
			}
			else if (object.isObject())
			{
				writer.append("{");
				int i = 0;
				int len = object.getMemberCount();
				for (String member : object.getMemberNames())
				{
					writer.append("\"");
					writeEscapedString(member);
					writer.append("\":");
					writeObject(object.get(member));
					if (i < len - 1)
						writer.append(",");
					i++;
				}
				writer.append("}");
			}
			else
			{
				Object value = object.getValue();
				if (value instanceof Boolean)
					writer.append(String.valueOf(object.getBoolean()));
				else if (value instanceof Byte)
					writer.append(String.valueOf(object.getByte()));
				else if (value instanceof Short)
					writer.append(String.valueOf(object.getShort()));
				else if (value instanceof Integer)
					writer.append(String.valueOf(object.getInt()));
				else if (value instanceof Float)
					writer.append(String.valueOf(object.getFloat()));
				else if (value instanceof Long)
					writer.append(String.valueOf(object.getLong()));
				else if (value instanceof Double)
					writer.append(String.valueOf(object.getDouble()));
				else
				{
					writer.append("\"");
					writeEscapedString(object.getString());
					writer.append("\"");
				}
			}
				
		}
		
		private void writeEscapedString(String s) throws IOException
		{
	    	for (int i = 0; i < s.length(); i++)
	    	{
	    		char c = s.charAt(i);
	    		switch (c)
	    		{
					case '\0':
						writer.append("\\0");
						break;
	    			case '\b':
	    				writer.append("\\b");
	    				break;
	    			case '\t':
	    				writer.append("\\t");
	    				break;
	    			case '\n':
	    				writer.append("\\n");
	    				break;
	    			case '\f':
	    				writer.append("\\f");
	    				break;
	    			case '\r':
	    				writer.append("\\r");
	    				break;
	    			case '\\':
	    				writer.append("\\\\");
	    				break;
	    			case '"':
	    				writer.append("\\\"");    					
	    				break;
	    			default:
	    				if (c < 0x0020 || c >= 0x7f)
	    				{
	    					writer.append('\\');
	    					writer.append('u');
	    					writer.append(HEXALPHABET.charAt((c & 0x0f000) >> 12));
	    					writer.append(HEXALPHABET.charAt((c & 0x00f00) >> 8));
	    					writer.append(HEXALPHABET.charAt((c & 0x000f0) >> 4));
	    					writer.append(HEXALPHABET.charAt(c & 0x0000f));
	    				}
	    				else
	    					writer.append(c);
	    				break;
	    		}
	    	}
		}
		
	}
	
}
