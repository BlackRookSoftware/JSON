/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.json;

public final class JSONTest2
{
	public static void main(String[] args) throws Exception
	{
		String json = "{ \"x\": 5, \"y\": null}";
		JSONWriter.Options options = new JSONWriter.Options();
		options.setIndentation("    ");
		options.setOmittingNullMembers(true);

		JSONObject out = JSONReader.readJSON(json);
		
		for (int i = 0; i < 5000; i++)
		{
			long t = System.nanoTime();
			String s = JSONWriter.writeJSONString(out, options);
			t = System.nanoTime() - t;
			System.out.println(s  + (t + "ns"));
		}		
	}
}
