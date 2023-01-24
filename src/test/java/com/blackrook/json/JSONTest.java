/*******************************************************************************
 * Copyright (c) 2019-2023 Black Rook Software
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
import java.util.Arrays;

import com.blackrook.json.annotation.JSONName;

public final class JSONTest
{
	public static void main(String[] args) throws Exception
	{
		String json = getTextualContents(ClassLoader.getSystemClassLoader().getResourceAsStream("com/blackrook/json/test.json"));
		JSONWriter.Options options = new JSONWriter.Options();
		options.setIndentation("    ");
		options.setOmittingNullMembers(true);
		
		Pair[] out = null;
		for (int i = 0; i < 5000; i++)
		{
			long t = System.nanoTime();
			out = JSONReader.readJSON(Pair[].class, json);
			t = System.nanoTime() - t;
			System.out.println(Arrays.toString(out) + (t + "ns"));
		}
		for (int i = 0; i < 5000; i++)
		{
			long t = System.nanoTime();
			String s = JSONWriter.writeJSONString(out, options);
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
		public int x;
		public int y;
		@JSONName("name")
		public int a;
		private JSONObject z;
		
		public JSONObject getZ()
		{
			return z;
		}
		
		public void setZ(JSONObject z)
		{
			this.z = z;
		}
		
		@Override
		public String toString()
		{
			return "(" + x + ", " + y + ", " + z + ", " + a + ")";
		}
	}
}
