/*******************************************************************************
 * Copyright (c) 2019 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import com.blackrook.json.util.Lexer;

/**
 * A class for reading JSON data into {@link JSONObject}s. 
 * @author Matthew Tropiano
 */
public class JSONReader
{
	/**
	 * Reads in a new JSONObject from an InputStream.
	 * This does not close the stream after reading, and reads the first structure
	 * that it finds.
	 * @param in the input stream to read from.
	 * @return the parsed JSONObject.
	 * @throws IOException if the stream can't be read, or an error occurs.
	 * @throws JSONConversionException if a parsing error occurs, or the JSON is malformed.
	 */
	public static JSONObject readJSON(InputStream in) throws IOException
	{
		return readJSON(new InputStreamReader(in));
	}

	/**
	 * Reads in a new JSONObject from a Reader.
	 * This does not close the stream after reading, and reads the first structure
	 * that it finds.
	 * @param reader the reader to read from.
	 * @return the parsed JSONObject.
	 * @throws IOException if the stream can't be read, or a read error occurs.
	 * @throws JSONConversionException if a parsing error occurs, or the JSON is malformed.
	 */
	public static JSONObject readJSON(Reader reader) throws IOException
	{
		return (new ReaderContext(reader)).doRead();
	}

	/**
	 * Reads in a new JSONObject from a string of characters.
	 * @param data the string to read.
	 * @return the parsed JSONObject.
	 * @throws IOException if the string can't be read, or a read error occurs.
	 * @throws JSONConversionException if a parsing error occurs, or the JSON is malformed.
	 */
	public static JSONObject readJSON(String data) throws IOException
	{
		return readJSON(new StringReader(data));
	}

	/**
	 * Reads in a new object from an InputStream.
	 * This does not close the stream after reading, and reads the first structure
	 * that it finds and returns it as a new object converted from the JSON.
	 * @param <T> the returned class type.
	 * @param clazz the class type to read.
	 * @param in the input stream to read from.
	 * @return the applied object, already converted.
	 * @throws IOException if the stream can't be read, or an error occurs.
	 * @throws JSONConversionException if a parsing error occurs, or the JSON is malformed.
	 */
	public static <T> T readJSON(Class<T> clazz, InputStream in) throws IOException
	{
		return readJSON(in).newObject(clazz);
	}

	/**
	 * Reads in a new object from a Reader.
	 * This does not close the stream after reading, and reads the first structure
	 * that it finds and returns it as a new object converted from the JSON.
	 * @param <T> the returned class type.
	 * @param clazz the class type to read.
	 * @param reader the reader to read from.
	 * @return the applied object, already converted.
	 * @throws IOException if the stream can't be read, or a read error occurs.
	 * @throws JSONConversionException if a parsing error occurs, or the JSON is malformed.
	 */
	public static <T> T readJSON(Class<T> clazz, Reader reader) throws IOException
	{
		return readJSON(reader).newObject(clazz);
	}

	/**
	 * Reads in a new object from a string of characters and returns it as a 
	 * new object converted from the JSON.
	 * @param <T> the returned class type.
	 * @param clazz the class type to read.
	 * @param data the string to read.
	 * @return the applied object, already converted.
	 * @throws IOException if the string can't be read, or a read error occurs.
	 * @throws JSONConversionException if a parsing error occurs, or the JSON is malformed.
	 */
	public static <T> T readJSON(Class<T> clazz, String data) throws IOException
	{
		return readJSON(data).newObject(clazz);
	}

	private static class JSONLexerKernel extends Lexer.Kernel
	{
		static final int TYPE_TRUE = 		0;
		static final int TYPE_FALSE = 		1;
		static final int TYPE_NULL = 		2;
		static final int TYPE_LBRACE = 		3;
		static final int TYPE_RBRACE = 		4;
		static final int TYPE_COLON = 		5;
		static final int TYPE_COMMA = 		6;
		static final int TYPE_LBRACK = 		7;
		static final int TYPE_RBRACK = 		8;
		static final int TYPE_MINUS = 		9;

		private JSONLexerKernel()
		{
			addStringDelimiter('"', '"');
			addStringDelimiter('\'', '\'');
			
			addDelimiter("{", TYPE_LBRACE);
			addDelimiter("}", TYPE_RBRACE);
			addDelimiter("[", TYPE_LBRACK);
			addDelimiter("]", TYPE_RBRACK);
			addDelimiter(":", TYPE_COLON);
			addDelimiter(",", TYPE_COMMA);
			addDelimiter("-", TYPE_MINUS);
			
			addKeyword("true", TYPE_TRUE);
			addKeyword("false", TYPE_FALSE);
			addKeyword("null", TYPE_NULL);
			
			setDecimalSeparator('.');
		}
	}
	
	private static final JSONLexerKernel KERNEL = new JSONLexerKernel();
	
	/**
	 * Lexer class for use in scanning text data for JSON data. 
	 */
	private static class JSONLexer extends Lexer
	{
		JSONLexer(Reader in)
		{
			super(KERNEL, in);
		}
	}
	
	/**
	 * Reader context.
	 */
	private static class ReaderContext extends Lexer.Parser
	{
		/** Member stack. */
		private List<String> errors;
		/** Member stack. */
		private Stack<String> currentMember;
		/** JSONObject Stack. */
		private Stack<JSONObject> currentObject;
		
		/** Reader context constructor. */
		ReaderContext(Reader reader)
		{
			super(new JSONLexer(reader));
			errors = new LinkedList<String>();
			currentMember = new Stack<String>();
			currentObject = new Stack<JSONObject>();
		}
		
		/**
		 * Starts parse of JSON object.
		 */
		JSONObject doRead()
		{
			nextToken();

			if (Value())
				return currentObject.pop();
			
			if (!errors.isEmpty())
			{
				StringBuilder sb = new StringBuilder();
				Iterator<String> it = errors.iterator();
				while (it.hasNext())
				{
					String s = it.next();
					sb.append(s);
					if (it.hasNext())
						sb.append('\n');
				}
				throw new JSONConversionException(sb.toString());
			}
			
			return null;
		}
		
		// Value.
		private boolean Value()
		{
			if (currentType(JSONLexerKernel.TYPE_MINUS))
			{
				nextToken();
				if (currentType(JSONLexerKernel.TYPE_NUMBER))
				{
					if (!ParseNumber("-"+currentToken().getLexeme()))
						return false;
				}
				
				nextToken();
				return true;
			}
			else if (currentType(JSONLexerKernel.TYPE_NUMBER))
			{
				if (!ParseNumber(currentToken().getLexeme()))
					return false;
				
				nextToken();
				return true;
			}
			else if (currentType(JSONLexerKernel.TYPE_STRING))
			{
				currentObject.push(JSONObject.create(currentToken().getLexeme()));
				nextToken();
				return true;
			}
			else if (currentType(JSONLexerKernel.TYPE_TRUE))
			{
				currentObject.push(JSONObject.create(Boolean.TRUE));
				nextToken();
				return true;
			}
			else if (currentType(JSONLexerKernel.TYPE_FALSE))
			{
				currentObject.push(JSONObject.create(Boolean.FALSE));
				nextToken();
				return true;
			}
			else if (currentType(JSONLexerKernel.TYPE_NULL))
			{
				currentObject.push(JSONObject.create(null));
				nextToken();
				return true;
			}
			else if (currentType(JSONLexerKernel.TYPE_LBRACK))
			{
				nextToken();
				currentObject.push(JSONObject.createEmptyArray());
				if (!ArrayValue())
					return false;
				
				if (!matchType(JSONLexerKernel.TYPE_RBRACK))
				{
					errors.add("Expected ']'");
					return false;
				}
				
				return true;
			}
			else if (currentType(JSONLexerKernel.TYPE_LBRACE))
			{
				nextToken();
				currentObject.push(JSONObject.createEmptyObject());
				if (!ObjectValue())
					return false;
				
				if (!matchType(JSONLexerKernel.TYPE_RBRACE))
				{
					errors.add("Expected '}'");
					return false;
				}
				
				return true;
			}
			
			errors.add("Expected value.");
			return false;
		}
		
		/**
		 * ArrayValue := 	Value ArrayBody
		 * 					[e]
		 */
		private boolean ArrayValue()
		{
			if (Value())
			{
				JSONObject json = currentObject.pop();
				currentObject.peek().append(json);
				return ArrayBody();
			}
			
			return true;
		}
		
		/**
		 * ArrayBody := 	"," Value ArrayBody
		 * 					[e]
		 */
		private boolean ArrayBody()
		{
			if (currentType(JSONLexerKernel.TYPE_COMMA))
			{
				nextToken();
				
				if (Value())
				{
					JSONObject json = currentObject.pop();
					currentObject.peek().append(json);
					return ArrayBody();
				}
				else
					return false;
			}
			
			return true;
		}
		
		/**
		 * ObjectValue := 	STRING ":" Value ObjectBody
		 * 					[e]
		 */
		private boolean ObjectValue()
		{
			if (currentType(JSONLexerKernel.TYPE_STRING, JSONLexerKernel.TYPE_IDENTIFIER))
			{
				currentMember.push(currentToken().getLexeme());
				nextToken();
				
				if (!matchType(JSONLexerKernel.TYPE_COLON))
				{
					errors.add("Expected ':'");
					return false;
				}
				
				if (Value())
				{
					JSONObject json = currentObject.pop();
					currentObject.peek().addMember(currentMember.pop(), json);
					return ObjectBody();
				}
				else
					return false;
			}
			
			return true;
		}
		
		/**
		 * ObjectBody := 	"," STRING ":" Value ObjectBody
		 * 					[e]
		 */
		private boolean ObjectBody()
		{
			if (currentType(JSONLexerKernel.TYPE_COMMA))
			{
				nextToken();
				
				if (!currentType(JSONLexerKernel.TYPE_STRING, JSONLexerKernel.TYPE_IDENTIFIER))
				{
					errors.add("Expected member name (string or identifier).");
					return false;
				}

				currentMember.push(currentToken().getLexeme());
				nextToken();
				
				if (!matchType(JSONLexerKernel.TYPE_COLON))
				{
					errors.add("Expected ':'");
					return false;
				}
				
				if (Value())
				{
					JSONObject json = currentObject.pop();
					currentObject.peek().addMember(currentMember.pop(), json);
					return ObjectBody();
				}
			}
			
			return true;
		}
		
		// Parses a validated number.
		private boolean ParseNumber(String s)
		{
			int idx = s.indexOf("x");
			int edx = Math.max(s.indexOf("e"), s.indexOf("E"));
			int pdx = -1;
			if (edx >= 0) 
				pdx = Math.max(edx, s.indexOf("+"));
			
			int fdx = s.indexOf(".");
			if (idx >= 0)
			{
				String lng = s.substring(idx + 1);
				try {
					currentObject.push(JSONObject.create(Long.parseLong(lng, 16)));
				} catch (NumberFormatException n) {
					errors.add("Malformed number.");
					return false;
				}
			}
			else if (edx >= 0)
			{
				String num = s.substring(0, edx);
				String exp = s.substring(pdx + 1);
				try {
					currentObject.push(JSONObject.create(Double.parseDouble(num) * Math.pow(10, Double.parseDouble(exp))));
				} catch (NumberFormatException n) {
					errors.add("Malformed number.");
					return false;
				}
			}
			else if (fdx >= 0)
			{
				try {
					currentObject.push(JSONObject.create(Double.parseDouble(s.toString())));
				} catch (NumberFormatException n) {
					errors.add("Malformed number.");
					return false;
				}
			}
			else
			{
				try {
					currentObject.push(JSONObject.create(Long.parseLong(s.toString())));
				} catch (NumberFormatException n) {
					errors.add("Malformed number.");
					return false;
				}
			}
			return true;
		}
		
	}

}
