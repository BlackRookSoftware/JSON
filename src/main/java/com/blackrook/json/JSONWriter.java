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
		/** Outputstream. May be null. */
		private OutputStream outStream;
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
			this.object = object;
			this.outStream = outStream;
		}
		
		// Writes to an output stream or writer.
		private void writeString(String object) throws IOException
		{
			if (outStream != null)
				outStream.write(object.getBytes(UTF_8));
			if (writer != null)
				writer.append(object);
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
				writeString("undefined");
			else if (object.isNull())
				writeString("null");
			else if (object.isArray())
			{
				writeString("[");
				for (int i = 0; i < object.length(); i++)
				{
					writeObject(object.get(i));
					if (i < object.length() - 1)
						writeString(",");
				}
				writeString("]");
			}
			else if (object.isObject())
			{
				writeString("{");
				int i = 0;
				int len = object.getMemberCount();
				for (String member : object.getMemberNames())
				{
					writeString("\"");
					writeString(escape(member));
					writeString("\":");
					writeObject(object.get(member));
					if (i < len - 1)
						writeString(",");
					i++;
				}
				writeString("}");
			}
			else
			{
				Object value = object.getValue();
				if (value instanceof Boolean)
					writeString(String.valueOf(object.getBoolean()));
				else if (value instanceof Byte)
					writeString(String.valueOf(object.getByte()));
				else if (value instanceof Short)
					writeString(String.valueOf(object.getShort()));
				else if (value instanceof Integer)
					writeString(String.valueOf(object.getInt()));
				else if (value instanceof Float)
					writeString(String.valueOf(object.getFloat()));
				else if (value instanceof Long)
					writeString(String.valueOf(object.getLong()));
				else if (value instanceof Double)
					writeString(String.valueOf(object.getDouble()));
				else
				{
					writeString("\"");
					writeString(escape(object.getString()));
					writeString("\"");
				}
			}
				
		}
		
		private String escape(String s)
		{
	    	StringBuilder out = new StringBuilder();
	    	for (int i = 0; i < s.length(); i++)
	    	{
	    		char c = s.charAt(i);
	    		switch (c)
	    		{
					case '\0':
						out.append("\\0");
						break;
	    			case '\b':
	    				out.append("\\b");
	    				break;
	    			case '\t':
	    				out.append("\\t");
	    				break;
	    			case '\n':
	    				out.append("\\n");
	    				break;
	    			case '\f':
	    				out.append("\\f");
	    				break;
	    			case '\r':
	    				out.append("\\r");
	    				break;
	    			case '\\':
	    				out.append("\\\\");
	    				break;
	    			case '"':
	    				out.append("\\\"");    					
	    				break;
	    			default:
	    				if (c < 0x0020 || c >= 0x7f)
	    					out.append("\\u"+String.format("%04x", (int)c));
	    				else
	    					out.append(c);
	    				break;
	    		}
	    	}
	    	
	    	return out.toString();
		}
		
	}
	
}
