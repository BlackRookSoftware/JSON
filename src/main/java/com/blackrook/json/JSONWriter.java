/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
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
public class JSONWriter
{
	private static final Charset UTF_8 = Charset.forName("UTF-8");
	private static final Options DEFAULT_OPTIONS = new Options();
	
	/**
	 * Writes a JSONObject out to the following output stream.
	 * @param jsonObject the object to write.
	 * @param options the options to use for JSON output. 
	 * @param out the output stream to write to.
	 * @throws IOException if a write error occurs.
	 * @since 1.2.0
	 */
	public static void writeJSON(JSONObject jsonObject, Options options, OutputStream out) throws IOException
	{
		(new WriterContext(jsonObject, options, out)).startWrite();
	}

	/**
	 * Writes a JSONObject out to the following output stream.
	 * @param jsonObject the object to write.
	 * @param options the options to use for JSON output. 
	 * @param writer the writer to write to.
	 * @throws IOException if a write error occurs.
	 * @since 1.2.0
	 */
	public static void writeJSON(JSONObject jsonObject, Options options, Writer writer) throws IOException
	{
		(new WriterContext(jsonObject, options, writer)).startWrite();
	}

	/**
	 * Writes a JSONObject out to the following output stream.
	 * @param jsonObject the object to write.
	 * @param options the options to use for JSON output. 
	 * @return the JSONObject as a JSON string. 
	 * @throws IOException if a write error occurs.
	 * @since 1.2.0
	 */
	public static String writeJSONString(JSONObject jsonObject, Options options) throws IOException
	{
		StringWriter sw = new StringWriter();
		writeJSON(jsonObject, options, sw);
		return sw.toString();
	}

	/**
	 * Writes a JSONObject out to the following output stream.
	 * @param jsonObject the object to write.
	 * @param out the output stream to write to.
	 * @throws IOException if a write error occurs.
	 */
	public static void writeJSON(JSONObject jsonObject, OutputStream out) throws IOException
	{
		writeJSON(jsonObject, DEFAULT_OPTIONS, out);
	}

	/**
	 * Writes a JSONObject out to the following output stream.
	 * @param jsonObject the object to write.
	 * @param writer the writer to write to.
	 * @throws IOException if a write error occurs.
	 */
	public static void writeJSON(JSONObject jsonObject, Writer writer) throws IOException
	{
		writeJSON(jsonObject, DEFAULT_OPTIONS, writer);
	}

	/**
	 * Writes a JSONObject out to the following output stream.
	 * @param jsonObject the object to write.
	 * @return the JSONObject as a JSON string. 
	 * @throws IOException if a write error occurs.
	 */
	public static String writeJSONString(JSONObject jsonObject) throws IOException
	{
		return writeJSONString(jsonObject, DEFAULT_OPTIONS);
	}

	/**
	 * Writes a JSONObject out to the following output stream.
	 * @param object the object to write.
	 * @param options the options to use for JSON output. 
	 * @param out the output stream to write to.
	 * @throws IOException if a write error occurs.
	 * @since 1.2.0
	 */
	public static void writeJSON(Object object, Options options, OutputStream out) throws IOException
	{
		writeJSON(JSONObject.create(object, options.converterSet), options, out);
	}

	/**
	 * Writes a JSONObject out to the following output stream.
	 * @param object the object to write.
	 * @param options the options to use for JSON output. 
	 * @param writer the writer to write to.
	 * @throws IOException if a write error occurs.
	 * @since 1.2.0
	 */
	public static void writeJSON(Object object, Options options, Writer writer) throws IOException
	{
		writeJSON(JSONObject.create(object, options.converterSet), options, writer);
	}

	/**
	 * Writes a JSONObject out to the following output stream.
	 * @param object the object to write.
	 * @param options the options to use for JSON output. 
	 * @return the JSONObject as a JSON string. 
	 * @throws IOException if a write error occurs.
	 * @since 1.2.0
	 */
	public static String writeJSONString(Object object, Options options) throws IOException
	{
		StringWriter sw = new StringWriter();
		writeJSON(JSONObject.create(object, options.converterSet), options, sw);
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
		writeJSON(JSONObject.create(object, DEFAULT_OPTIONS.converterSet), out);
	}

	/**
	 * Writes a JSONObject out to the following output stream.
	 * @param object the object to write.
	 * @param writer the writer to write to.
	 * @throws IOException if a write error occurs.
	 */
	public static void writeJSON(Object object, Writer writer) throws IOException
	{
		writeJSON(JSONObject.create(object, DEFAULT_OPTIONS.converterSet), writer);
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
		writeJSON(JSONObject.create(object, DEFAULT_OPTIONS.converterSet), sw);
		return sw.toString();
	}

	/** This writer's options. */
	private Options options;
	
	/**
	 * Creates a new JSONWriter with a set of options to be used for every write.
	 * @param options the writer options to use.
	 * @since 1.2.0
	 */
	public JSONWriter(Options options)
	{
		this.options = options;
	}
	
	/**
	 * Writes a JSONObject out to the following output stream.
	 * @param jsonObject the object to write.
	 * @param out the output stream to write to.
	 * @throws IOException if a write error occurs.
	 * @since 1.2.0
	 */
	public void write(JSONObject jsonObject, OutputStream out) throws IOException
	{
		writeJSON(jsonObject, options, out);
	}

	/**
	 * Writes a JSONObject out to the following output stream.
	 * @param jsonObject the object to write.
	 * @param writer the writer to write to.
	 * @throws IOException if a write error occurs.
	 * @since 1.2.0
	 */
	public void write(JSONObject jsonObject, Writer writer) throws IOException
	{
		writeJSON(jsonObject, options, writer);
	}

	/**
	 * Writes a JSONObject out to the following output stream.
	 * @param jsonObject the object to write.
	 * @return the JSONObject as a JSON string. 
	 * @throws IOException if a write error occurs.
	 * @since 1.2.0
	 */
	public String writeString(JSONObject jsonObject) throws IOException
	{
		return writeJSONString(jsonObject, options);
	}

	/**
	 * Writes a JSONObject out to the following output stream.
	 * @param object the object to write.
	 * @param out the output stream to write to.
	 * @throws IOException if a write error occurs.
	 * @since 1.2.0
	 */
	public void write(Object object, OutputStream out) throws IOException
	{
		write(JSONObject.create(object, DEFAULT_OPTIONS.converterSet), out);
	}

	/**
	 * Writes a JSONObject out to the following output stream.
	 * @param object the object to write.
	 * @param writer the writer to write to.
	 * @throws IOException if a write error occurs.
	 * @since 1.2.0
	 */
	public void write(Object object, Writer writer) throws IOException
	{
		write(JSONObject.create(object, DEFAULT_OPTIONS.converterSet), writer);
	}

	/**
	 * Writes a JSONObject out to the following output stream.
	 * @param object the object to write.
	 * @return the JSONObject as a JSON string. 
	 * @throws IOException if a write error occurs.
	 * @since 1.2.0
	 */
	public String writeString(Object object) throws IOException
	{
		StringWriter sw = new StringWriter();
		write(JSONObject.create(object, DEFAULT_OPTIONS.converterSet), sw);
		return sw.toString();
	}

	/**
	 * The JSON export options to pass to the writer methods.
	 * @since 1.2.0
	 */
	public static class Options
	{
		/** Pretty-print indentation. */
		private String indentation;
		/** If true, null fields will be left undefined in output. */
		private boolean nullOmitting;
		/** The converter set used for object conversion. */
		private JSONConverterSet converterSet;
		
		public Options()
		{
			this.indentation = null;
			this.nullOmitting = false;
			this.converterSet = JSONObject.GLOBAL_CONVERTER_SET;
		}
		
		/**
		 * @return the indentation string to use for pretty-printing indentation.
		 */
		public String getIndentation() 
		{
			return indentation;
		}
		
		/**
		 * Sets the indentation string to use for pretty-printing indentation.
		 * @param indentation the indentation string to use.
		 */
		public void setIndentation(String indentation) 
		{
			this.indentation = indentation;
		}
		
		/**
		 * @return true if this omits object type members with a null values, false if not.
		 */
		public boolean isOmittingNullMembers() 
		{
			return nullOmitting;
		}
		
		/**
		 * Sets if the writer omits null values from being exported from objects.
		 * @param nullOmitting true if so, false if not.
		 */
		public void setOmittingNullMembers(boolean nullOmitting) 
		{
			this.nullOmitting = nullOmitting;
		}

		/**
		 * Replaces the underlying converter set.
		 * @param converterSet the converter set to use.
		 * @since [NOW]
		 */
		public void setConverterSet(JSONConverterSet converterSet) 
		{
			this.converterSet = converterSet;
		}
		
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
		/** Options. */
		private Options options;
		
		private WriterContext(JSONObject object, Options options, Writer writer)
		{
			this.object = object;
			this.writer = writer;
			this.options = options;
		}

		private WriterContext(JSONObject object, Options options, OutputStream outStream)
		{
			this(object, options, new OutputStreamWriter(outStream, UTF_8));
		}
		
		/**
		 * Starts the write.
		 * @throws IOException if an error occurs on the write.
		 */
		private void startWrite() throws IOException
		{
			writeObject(object, 0);
		}
		
		/**
		 * Writes an object.
		 * @param object the object to write.
		 * @param indentDepth the current indentation depth.
		 * @throws IOException if an error occurs on the write.
		 */
		private void writeObject(JSONObject object, int indentDepth) throws IOException
		{
			if (object.isUndefined())
				writer.append("undefined");
			else if (object.isNull())
				writer.append("null");
			else if (object.isArray())
				writeArrayValue(object, indentDepth + 1);
			else if (!object.isObject())
				writePrimitiveValue(object.getValue());
			else
				writeObjectValue(object, indentDepth + 1);
		}

		private void writeArrayValue(JSONObject object, int indentDepth) throws IOException
		{
			String memberIndent = indentString(options.indentation, indentDepth);
			String endIndent = indentString(options.indentation, indentDepth - 1);
			
			writer.append("[");
			if (memberIndent != null)
				writer.append('\n');
			
			final int len = object.length();
			for (int i = 0; i < len; i++)
			{
				if (memberIndent != null)
					writer.append(memberIndent);
				
				writeObject(object.get(i), indentDepth);
				if (i < len - 1)
				{
					writer.append(",");
					if (memberIndent != null)
						writer.append('\n');
				}
				else if (memberIndent != null)
				{
					writer.append('\n');
				}
			}

			if (endIndent != null)
				writer.append(endIndent);
			
			writer.append("]");
		}
		
		private void writeObjectValue(JSONObject object, int indentDepth) throws IOException
		{
			String memberIndent = indentString(options.indentation, indentDepth);
			String endIndent = indentString(options.indentation, indentDepth - 1);

			writer.append("{");

			boolean wroteOne = false;
			for (String member : object.getMemberNames())
			{
				JSONObject outObj = object.get(member);
				if (options.isOmittingNullMembers() && (outObj.isNull() || outObj.isUndefined()))
					continue;

				if (wroteOne)
					writer.append(",");

				if (memberIndent != null)
				{
					writer.append('\n');
					writer.append(memberIndent);
				}

				writer.append("\"");
				writeEscapedString(member);
				writer.append("\":");
				
				if (memberIndent != null)
					writer.append(' ');
				
				writeObject(outObj, indentDepth);
				wroteOne = true;
			}

			if (wroteOne)
			{
				if (memberIndent != null)
					writer.append("\n");

				if (endIndent != null)
					writer.append(endIndent);
			}
			
			writer.append("}");
		}
		
		private void writePrimitiveValue(Object value) throws IOException
		{
			
			if (value instanceof Boolean)
				writer.append(String.valueOf(value));
			else if (value instanceof Byte)
				writer.append(String.valueOf(value));
			else if (value instanceof Short)
				writer.append(String.valueOf(value));
			else if (value instanceof Integer)
				writer.append(String.valueOf(value));
			else if (value instanceof Float)
				writer.append(String.valueOf(value));
			else if (value instanceof Long)
				writer.append(String.valueOf(value));
			else if (value instanceof Double)
				writer.append(String.valueOf(value));
			else
			{
				writer.append("\"");
				writeEscapedString(String.valueOf(value));
				writer.append("\"");
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

		private static String indentString(String indentation, int depth) throws IOException
		{
			if (indentation == null)
				return null;
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < depth; i++)
				sb.append(indentation);
			return sb.toString();
		}

	}
	
}
