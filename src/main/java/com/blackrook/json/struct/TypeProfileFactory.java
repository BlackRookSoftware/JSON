/*******************************************************************************
 * Copyright (c) 2019-2023 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.json.struct;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.blackrook.json.JSONObject;
import com.blackrook.json.annotation.JSONCollectionType;
import com.blackrook.json.annotation.JSONMapType;

/**
 * A factory that produces type profiles for POJOs and data objects.
 * @author Matthew Tropiano
 */
public class TypeProfileFactory
{
	/** The policy used by this factory. */
	private MemberPolicy policy;
	/** Generated profiles. */
	private HashMap<Class<?>, Profile<?>> generatedProfiles;
	
	/**
	 * Creates a new TypeProfileFactory.
	 * This creates type profiles using an additional policy that changes method/field handling
	 * for each type.
	 * @param policy the member policy.
	 */
	public TypeProfileFactory(MemberPolicy policy)
	{
		this.policy = policy;
		this.generatedProfiles = new HashMap<>(8);
	}

	/**
	 * Creates a new profile for a provided type.
	 * Generated profiles are stored in memory, and retrieved again by class type.
	 * <p>This method is thread-safe.
	 * @param <T> the class type.
	 * @param clazz the class.
	 * @return a new profile.
	 */
	@SuppressWarnings("unchecked")
	public <T> Profile<T> getProfile(Class<T> clazz)
	{
		Profile<T> out = null;
		if ((out = (Profile<T>)generatedProfiles.get(clazz)) == null)
		{
			synchronized (generatedProfiles)
			{
				// early out.
				if ((out = (Profile<T>)generatedProfiles.get(clazz)) == null)
				{
					out = new Profile<>(clazz, policy);
					generatedProfiles.put(clazz, out);
				}
			}
		}
		else
			out = (Profile<T>)generatedProfiles.get(clazz);
		
		return out;
	}
	
	/**
	 * Returns the field name for a getter/setter method.
	 * If the method name is not a getter or setter name, then this will return <code>methodName</code>
	 * <p>
	 * For example, the field name "setColor" will return "color" and "isHidden" returns "hidden". 
	 * (note the change in camel case).
	 * @param methodName the name of the method.
	 * @return the modified method name.
	 */
	private static String getFieldName(String methodName)
	{
		if (isGetterName(methodName))
		{
			if (methodName.startsWith("is"))
				return truncateMethodName(methodName, true);
			else if (methodName.startsWith("get"))
				return truncateMethodName(methodName, false);
		}
		else if (isSetterName(methodName))
			return truncateMethodName(methodName, false);
		
		return methodName;
	}

	/**
	 * Checks if a method name describes a "setter" method. 
	 * @param methodName the name of the method.
	 * @return true if so, false if not.
	 */
	private static boolean isSetterName(String methodName)
	{
		if (methodName.startsWith("set"))
		{
			if (methodName.length() < 4)
				return false;
			else
				return Character.isUpperCase(methodName.charAt(3));
		}
		return false;
	}

	/**
	 * Checks if a method name describes a "getter" method (also detects "is" methods). 
	 * @param methodName the name of the method.
	 * @return true if so, false if not.
	 */
	private static boolean isGetterName(String methodName)
	{
		if (methodName.startsWith("is"))
		{
			if (methodName.length() < 3)
				return false;
			else
				return Character.isUpperCase(methodName.charAt(2));
		}
		else if (methodName.startsWith("get"))
		{
			if (methodName.length() < 4)
				return false;
			else
				return Character.isUpperCase(methodName.charAt(3));
		}
		return false;
	}

	/**
	 * Checks if a method is a "setter" method.
	 * This checks its name, if it returns a void value or the source class type (chain setters), takes one argument, and if it is <b>public</b>.
	 * @param method the method to inspect.
	 * @return true if so, false if not.
	 */
	private static boolean isSetter(Method method, Class<?> sourceType)
	{
		Class<?> rettype = method.getReturnType();
		return isSetterName(method.getName()) 
			&& method.getParameterTypes().length == 1
			&& (rettype == Void.TYPE || rettype == Void.class || rettype == sourceType) 
			&& (method.getModifiers() & Modifier.PUBLIC) != 0;
	}

	/**
	 * Checks if a method is a "getter" method.
	 * This checks its name, if it returns a non-void value, takes no arguments, and if it is <b>public</b>.
	 * @param method the method to inspect.
	 * @return true if so, false if not.
	 */
	private static boolean isGetter(Method method)
	{
		return isGetterName(method.getName()) 
			&& method.getParameterTypes().length == 0
			&& !(method.getReturnType() == Void.TYPE || method.getReturnType() == Void.class) 
			&& (method.getModifiers() & Modifier.PUBLIC) != 0;
	}

	// truncator method
	private static String truncateMethodName(String methodName, boolean is)
	{
		return is 
			? Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3)
			: Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
	}

	/**
	 * An interface for figuring out whether to ignore certain fields or methods
	 * on an object that a profile is being generated for (if they are already 
	 * going to pass testing for includable fields).
	 */
	public interface MemberPolicy
	{
		/**
		 * Checks if a field is ignored by the profile generator.
		 * @param field the field to test.
		 * @return true if so, false if not.
		 */
		boolean isIgnored(Field field);
	
		/**
		 * Checks if a method is ignored by the profile generator.
		 * @param method the method to test.
		 * @return true if so, false if not.
		 */
		boolean isIgnored(Method method);
	
		/**
		 * Gets an alias name for the provided field.
		 * This is used for mapping this field to another name, usually for type/object conversions or the like.
		 * @param field the field to use.
		 * @return the name to use, or null for no alias.
		 */
		String getAlias(Field field);
	
		/**
		 * Gets an alias name for the provided method.
		 * This is used for mapping this method to another name, usually for type/object conversions or the like.
		 * @param method the method to use.
		 * @return the name to use, or null for no alias.
		 */
		String getAlias(Method method);
	}

	/**
	 * Type profile for an unknown object that has an ambiguous signature for 
	 * applying values to POJOs and beans.
	 * This only cares about setter methods with one argument and public fields.
	 * @param <T> the type class
	 */
	public static class Profile<T extends Object>
	{
		/** Map of Public fields by name. */
		private HashMap<String, FieldInfo> publicFieldsByName;
		/** Map of getters by name. */
		private HashMap<String, MethodInfo> getterMethodsByName;
		/** Map of setters by name. */
		private HashMap<String, MethodInfo> setterMethodsByName;

		/** Map of Public fields by alias. */
		private HashMap<String, FieldInfo> publicFieldsByAlias;
		/** Map of getters by alias. */
		private HashMap<String, MethodInfo> getterMethodsByAlias;
		/** Map of setters by alias. */
		private HashMap<String, MethodInfo> setterMethodsByAlias;
		
		// Creates a profile from a class. 
		private Profile(Class<? extends T> inputClass, MemberPolicy policy)
		{
			publicFieldsByName = new HashMap<String, FieldInfo>(4);
			getterMethodsByName = new HashMap<String, MethodInfo>(4);
			setterMethodsByName = new HashMap<String, MethodInfo>(4);
			publicFieldsByAlias = new HashMap<String, FieldInfo>(4);
			getterMethodsByAlias = new HashMap<String, MethodInfo>(4);
			setterMethodsByAlias = new HashMap<String, MethodInfo>(4);

			for (Field f : inputClass.getFields())
			{
				if (Modifier.isStatic(f.getModifiers()))
					continue;
					
				if (!policy.isIgnored(f))
				{
					String alias = policy.getAlias(f);
					
					Class<?> type = f.getType();
					Class<?> mapKeyType = null;
					Class<?> mapValueType = null;
					if (Map.class.isAssignableFrom(type))
					{
						JSONMapType anno = f.getAnnotation(JSONMapType.class);
						if (anno != null)
						{
							mapKeyType = anno.keyType();
							mapValueType = anno.valueType();
						}
						else
						{
							mapKeyType = String.class;
							mapValueType = JSONObject.class;
						}
					}
					else if (Collection.class.isAssignableFrom(type))
					{
						JSONCollectionType anno = f.getAnnotation(JSONCollectionType.class);
						if (anno != null)
							mapKeyType = anno.value();
						else
							mapKeyType = JSONObject.class;
					}
					else if (type == Iterable.class)
					{
						JSONCollectionType anno = f.getAnnotation(JSONCollectionType.class);
						if (anno != null)
							mapKeyType = anno.value();
						else
							mapKeyType = JSONObject.class;
					}
					else if (Utils.isArray(type))
					{
						mapKeyType = Utils.getArrayType(type);
					}

					FieldInfo fi = new FieldInfo(type, f, alias, mapKeyType, mapValueType);
					publicFieldsByName.put(f.getName(), fi);
					if (alias != null)
						publicFieldsByAlias.put(alias, fi);
				}
			}
			
			for (Method m : inputClass.getMethods())
			{
				if (!policy.isIgnored(m))
				{
					if (isGetter(m) && !m.getName().equals("getClass"))
					{
						Class<?> type = m.getReturnType();
						Class<?> mapKeyType = null;
						Class<?> mapValueType = null;
						if (Map.class.isAssignableFrom(type))
						{
							JSONMapType anno = m.getAnnotation(JSONMapType.class);
							if (anno != null)
							{
								mapKeyType = anno.keyType();
								mapValueType = anno.valueType();
							}
							else
							{
								mapKeyType = String.class;
								mapValueType = JSONObject.class;
							}
						}
						else if (Collection.class.isAssignableFrom(type))
						{
							JSONCollectionType anno = m.getAnnotation(JSONCollectionType.class);
							if (anno != null)
								mapKeyType = anno.value();
							else
								mapKeyType = JSONObject.class;
						}
						else if (type == Iterable.class)
						{
							JSONCollectionType anno = m.getAnnotation(JSONCollectionType.class);
							if (anno != null)
								mapKeyType = anno.value();
							else
								mapKeyType = JSONObject.class;
						}
						else if (Utils.isArray(type))
						{
							mapKeyType = Utils.getArrayType(type);
						}
											
						String alias = policy.getAlias(m);
						MethodInfo mi = new MethodInfo(type, m, alias, mapKeyType, mapValueType);
						getterMethodsByName.put(getFieldName(m.getName()), mi);
						if (alias != null)
							getterMethodsByAlias.put(alias, mi);
					}
					else if (isSetter(m, inputClass))
					{
						Class<?> type = m.getParameterTypes()[0];
						Class<?> mapKeyType = null;
						Class<?> mapValueType = null;
						if (Map.class.isAssignableFrom(type))
						{
							JSONMapType anno = m.getAnnotation(JSONMapType.class);
							if (anno != null)
							{
								mapKeyType = anno.keyType();
								mapValueType = anno.valueType();
							}
							else
							{
								mapKeyType = String.class;
								mapValueType = JSONObject.class;
							}
						}
						else if (Collection.class.isAssignableFrom(type))
						{
							JSONCollectionType anno = m.getAnnotation(JSONCollectionType.class);
							if (anno != null)
								mapKeyType = anno.value();
							else
								mapKeyType = JSONObject.class;
						}
						else if (type == Iterable.class)
						{
							JSONCollectionType anno = m.getAnnotation(JSONCollectionType.class);
							if (anno != null)
								mapKeyType = anno.value();
							else
								mapKeyType = JSONObject.class;
						}
						else if (Utils.isArray(type))
						{
							mapKeyType = Utils.getArrayType(type);
						}
											
						String alias = policy.getAlias(m);
						MethodInfo mi = new MethodInfo(type, m, alias, mapKeyType, mapValueType);
						setterMethodsByName.put(getFieldName(m.getName()), mi);
						if (alias != null)
							setterMethodsByAlias.put(alias, mi);
					}
				}
				
			}
		}
		
		/** 
		 * Returns a reference to the map that contains this profile's public fields.
		 * Maps "field name" to {@link FieldInfo} object.
		 * @return the map of field name to field.  
		 */
		public HashMap<String, FieldInfo> getPublicFieldsByName()
		{
			return publicFieldsByName;
		}

		/** 
		 * Returns a reference to the map that contains this profile's getter methods.
		 * Maps "field name" to {@link MethodInfo} object, which contains the {@link Class} type and the {@link Method} itself.
		 * @return the map of getter name to method.  
		 */
		public HashMap<String, MethodInfo> getGetterMethodsByName()
		{
			return getterMethodsByName;
		}

		/** 
		 * Returns a reference to the map that contains this profile's setter methods.
		 * Maps "field name" to {@link MethodInfo} object, which contains the {@link Class} type and the {@link Method} itself. 
		 * @return the map of setter name to method.  
		 */
		public HashMap<String, MethodInfo> getSetterMethodsByName()
		{
			return setterMethodsByName;
		}

		/** 
		 * Returns a reference to the map that contains this profile's public fields.
		 * Maps "field name" to {@link FieldInfo} object.
		 * @return the map of field name to field.  
		 */
		public HashMap<String, FieldInfo> getPublicFieldsByAlias()
		{
			return publicFieldsByAlias;
		}

		/** 
		 * Returns a reference to the map that contains this profile's getter methods.
		 * Maps "field name" to {@link MethodInfo} object, which contains the {@link Class} type and the {@link Method} itself.
		 * @return the map of getter name to method.  
		 */
		public HashMap<String, MethodInfo> getGetterMethodsByAlias()
		{
			return getterMethodsByAlias;
		}

		/** 
		 * Returns a reference to the map that contains this profile's setter methods.
		 * Maps "field name" to {@link MethodInfo} object, which contains the {@link Class} type and the {@link Method} itself. 
		 * @return the map of setter name to method.  
		 */
		public HashMap<String, MethodInfo> getSetterMethodsByAlias()
		{
			return setterMethodsByAlias;
		}

		/**
		 * Field information.
		 * Contains the relevant type and getter/setter method.
		 */
		public static class FieldInfo
		{
			/** Object Type. */
			private Class<?> type;
			/** Field reference. */
			private Field field;
			/** Alias, if any. */
			private String alias;
			/** Key or generic class. */
			private Class<?> primaryClass;
			/** Value class. */
			private Class<?> valueClass;

			private FieldInfo(Class<?> type, Field field, String alias, Class<?> primaryClass, Class<?> valueClass)
			{
				this.type = type;
				this.field = field;
				this.alias = alias;
				this.primaryClass = primaryClass;
				this.valueClass = valueClass;
			}

			/**
			 * @return the type that this setter takes as an argument, or this getter returns.
			 */
			public Class<?> getType()
			{
				return type;
			}

			/**
			 * @return the setter method itself.
			 */
			public Field getField()
			{
				return field;
			}
			
			/**
			 * @return the alias for the field info, if any.
			 */
			public String getAlias()
			{
				return alias;
			}
			
			/**
			 * @return true if this field or methods represents a map, false if not.
			 */
			public boolean isMap()
			{
				return primaryClass != null && valueClass != null;
			}
			
			/**
			 * @return the class type used for this generic collection.
			 */
			public Class<?> getPrimaryClass()
			{
				return primaryClass;
			}
			
			/**
			 * @return the class used for this generic map key.
			 */
			public Class<?> getKeyClass()
			{
				return primaryClass;
			}
			
			/**
			 * @return the class used for this generic map value.
			 */
			public Class<?> getValueClass()
			{
				return valueClass;
			}
			
		}
		
		/**
		 * Method signature.
		 * Contains the relevant type and getter/setter method.
		 */
		public static class MethodInfo
		{
			/** Object Type. */
			private Class<?> type;
			/** Method reference. */
			private Method method;
			/** Alias, if any. */
			private String alias;
			/** Key or generic class. */
			private Class<?> primaryClass;
			/** Value class. */
			private Class<?> valueClass;

			private MethodInfo(Class<?> type, Method method, String alias, Class<?> primaryClass, Class<?> valueClass)
			{
				this.type = type;
				this.method = method;
				this.alias = alias;
				this.primaryClass = primaryClass;
				this.valueClass = valueClass;
			}

			/**
			 * @return the type that this setter takes as an argument, or this getter returns.
			 */
			public Class<?> getType()
			{
				return type;
			}

			/**
			 * @return the setter method itself.
			 */
			public Method getMethod()
			{
				return method;
			}
			
			/**
			 * @return the alias for the field info, if any.
			 */
			public String getAlias()
			{
				return alias;
			}
			
			/**
			 * @return true if this field or methods represents a map, false if not.
			 */
			public boolean isMap()
			{
				return primaryClass != null && valueClass != null;
			}
			
			/**
			 * @return the class type used for this generic collection.
			 */
			public Class<?> getPrimaryClass()
			{
				return primaryClass;
			}
			
			/**
			 * @return the class used for this generic map key.
			 */
			public Class<?> getKeyClass()
			{
				return primaryClass;
			}
			
			/**
			 * @return the class used for this generic map value.
			 */
			public Class<?> getValueClass()
			{
				return valueClass;
			}
			
		}
		
	}

}
