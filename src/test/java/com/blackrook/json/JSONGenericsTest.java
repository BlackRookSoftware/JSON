/*******************************************************************************
 * Copyright (c) 2019-2020 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.AbstractMap;

import com.blackrook.json.annotation.JSONMapType;

public final class JSONGenericsTest
{
	public static void main(String[] args) throws Exception
	{
		String json = getTextualContents(ClassLoader.getSystemClassLoader().getResourceAsStream("com/blackrook/json/test2.json"));
		
		Pair out = null;
		for (int i = 0; i < 5000; i++)
		{
			long t = System.nanoTime();
			out = JSONReader.readJSON(Pair.class, json);
			t = System.nanoTime() - t;
			System.out.println(out + (t + "ns"));
		}
		for (int i = 0; i < 5000; i++)
		{
			long t = System.nanoTime();
			String s = JSONWriter.writeJSONString(out);
			t = System.nanoTime() - t;
			System.out.println(s  + (t + "ns"));
		}		
	}
	
	/**
	 * Retrieves the textual contents of a stream in the system's current encoding.
	 * @param in	the input stream to use.
	 * @return		a contiguous string (including newline characters) of the stream's contents.
	 * @throws IOException	if the read cannot be done.
	 */
	public static String getTextualContents(InputStream in) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		while ((line = br.readLine()) != null)
		{
			sb.append(line);
			sb.append('\n');
		}
		br.close();
		return sb.toString();
	}

	public static class Pair
	{
		//@JSONCollectionType(String.class)
		//public Collection<String> setjunk;
		public String[] setjunk;
		
		@JSONMapType(keyType = String.class, valueType = Integer.class)
		public AbstractMap<String, Integer> fields;

		@Override
		public String toString()
		{
			return setjunk.toString() + ", " + fields.toString();
		}
	}
}
