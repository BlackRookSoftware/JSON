/*******************************************************************************
 * Copyright (c) 2019-2023 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.json;

public final class JSONTest5
{
	public static void main(String[] args) throws Exception
	{
		JSONObject array = JSONReader.readJSON("[1,2,3,4,5]");
		array.push(JSONObject.create(6));
		System.out.println(JSONWriter.writeJSONString(array));
		array.pop();
		System.out.println(JSONWriter.writeJSONString(array));
	}
	
}
