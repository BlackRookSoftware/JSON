package com.blackrook.json;

import java.io.InputStream;
import java.util.Arrays;

import com.blackrook.json.annotation.JSONName;

public final class JSONTest
{
	public static void main(String[] args) throws Exception
	{
		InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream("com/blackrook/json/test.json");
		Pair[] out = JSONReader.readJSON(Pair[].class, in);
		System.out.println(Arrays.toString(out));
		System.out.println(JSONWriter.writeJSONString(out));
	}
	
	public static class Pair
	{
		public int x;
		public int y;
		@JSONName("name")
		public int a;
		private int z;
		
		public int getZ()
		{
			return z;
		}
		
		public void setZ(int z)
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
