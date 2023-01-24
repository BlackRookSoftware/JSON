/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.json;

import java.util.Date;

public final class JSONTest3
{
	public static void main(String[] args) throws Exception
	{
		JSONConverterSet set1 = new JSONConverterSet();
		JSONConverterSet set2 = new JSONConverterSet();
		set2.setConverter(Date.class, new JSONConverter<Date>() 
		{
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
		});
		
		JSONWriter.Options options = new JSONWriter.Options();
		options.setConverterSet(set1);
		JSONWriter.Options options2 = new JSONWriter.Options();
		options2.setConverterSet(set2);

		Example example = new Example();
		
		for (int i = 0; i < 5000; i++)
		{
			long t = System.nanoTime();
			String s = JSONWriter.writeJSONString(example, options);
			t = System.nanoTime() - t;
			System.out.println(s  + (t + "ns"));
		}		

		String s = null;
		for (int i = 0; i < 5000; i++)
		{
			long t = System.nanoTime();
			s = JSONWriter.writeJSONString(example, options2);
			t = System.nanoTime() - t;
			System.out.println(s  + (t + "ns"));
		}
		
		for (int i = 0; i < 5000; i++)
		{
			long t = System.nanoTime();
			Example e = JSONReader.readJSON(Example.class, s, set2);
			t = System.nanoTime() - t;
			System.out.println(e  + (t + "ns"));
		}

	}
	
	public static class Example
	{
		public Date date;
		public String x;
		
		public Example()
		{
			this.date = new Date();
			this.x = "Hello, world!";
		}
		
		@Override
		public String toString() 
		{
			return String.valueOf(date) + ", x:" + x;
		}
		
	}
	
}
