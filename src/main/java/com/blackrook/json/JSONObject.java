/*******************************************************************************
 * Copyright (c) 2019-2025 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.blackrook.json.annotation.JSONIgnore;
import com.blackrook.json.annotation.JSONName;
import com.blackrook.json.struct.TypeProfileFactory;
import com.blackrook.json.struct.Utils;
import com.blackrook.json.struct.TypeProfileFactory.MemberPolicy;
import com.blackrook.json.struct.TypeProfileFactory.Profile;
import com.blackrook.json.struct.TypeProfileFactory.Profile.FieldInfo;
import com.blackrook.json.struct.TypeProfileFactory.Profile.MethodInfo;

/**
 * JSON Object abstraction.
 * @author Matthew Tropiano
 */
public class JSONObject
{
	/** JSON object profile factory. */
	static final TypeProfileFactory PROFILE_FACTORY = new TypeProfileFactory(new MemberPolicy()
	{
		@Override
		public boolean isIgnored(Field field)
		{
			return field.getAnnotation(JSONIgnore.class) != null;
		}

		@Override
		public boolean isIgnored(Method method)
		{
			return method.getAnnotation(JSONIgnore.class) != null;
		}
		
		@Override
		public String getAlias(Field field)
		{
			JSONName anno = field.getAnnotation(JSONName.class);
			return anno != null ? anno.value() : null;
		}

		@Override
		public String getAlias(Method method)
		{
			JSONName anno = method.getAnnotation(JSONName.class);
			return anno != null ? anno.value() : null;
		}
	});
	
	/** Empty member list. */
	private static final String[] EMPTY_MEMBER_LIST = new String[0];
	
	/** Not an array. */
	private static final int NOT_ARRAY = -1;
	/** Undefined member. All instances of UNDEFINED are this one. */
	public static final JSONObject UNDEFINED = new JSONObject(Type.UNDEFINED, null, NOT_ARRAY);
	/** Null member. All instances of NULL are this one. */
	public static final JSONObject NULL = new JSONObject(Type.OBJECT, null, NOT_ARRAY);
	
	/** 
	 * Global converter set.
	 * This is the implementation for the global fallback, a deprecated implementation of JSON conversion
	 * in favor of supporting several different sets. 
	 */
	static final JSONConverterSet GLOBAL_CONVERTER_SET = new JSONConverterSet();
	
	/**
	 * JavaScript type of a JSONObject.
	 */
	public static enum Type
	{
		/** Undefined type for undefined. */ 
		UNDEFINED,
		/** Object type for objects, or null. Stored as HashMap&lt;String, JSONObject&gt;, or null. */
		OBJECT,
		/** Numeric type. */
		NUMBER,
		/** String type. */
		STRING,
		/** Boolean type. */
		BOOLEAN;
	}

	/** JavaScript type. */
	private Type type;
	/** Object internal value. */
	private Object value;
	/** Array length. */
	private int length;
	
	/**
	 * Gets a converter for a type for the default converter set.
	 * Since 1.3.0, It is, however, preferred to use a {@link JSONConverterSet} for specifying how JSON objects get converted.
	 * @param <E> the class type.
	 * @param clazz the class to get the converter for.
	 * @return a converter to use for JSON conversion.
	 * @see JSONConverterSet#getConverter(Class)
	 */
	public static <E> JSONConverter<E> getConverter(Class<E> clazz)
	{
		return GLOBAL_CONVERTER_SET.getConverter(clazz);
	}
	
	/**
	 * Sets a converter for a type.
	 * Since 1.3.0, It is, however, preferred to use a {@link JSONConverterSet} for specifying how JSON objects get converted.
	 * @param <E> the class type.
	 * @param clazz the class to get the converter for.
	 * @param converter the converter to use for JSON conversion.
	 * @see JSONConverterSet#setConverter(Class, JSONConverter)
	 */
	public static <E extends Object> void setConverter(Class<E> clazz, JSONConverter<E> converter)
	{
		GLOBAL_CONVERTER_SET.setConverter(clazz, converter);
	}

	/**
	 * Creates a new JSON object using the default converter set.
	 * Since 1.3.0, It is, however, preferred to use a {@link JSONConverterSet} for specifying how JSON objects get converted.
	 * @param <T> the object type used for finding a converter.
	 * @param object the object to encapsulate.
	 * @return the JSONObject representing the input object.
	 * @see JSONObject#create(Object, JSONConverterSet)
	 */
	public static <T> JSONObject create(T object)
	{
		return create(object, GLOBAL_CONVERTER_SET);
	}
	
	/**
	 * Creates a new JSON object using an associated specific converter set.
	 * Objects may have an internal converter.
	 * @param <T> the object type used for finding a converter.
	 * @param object the object to encapsulate.
	 * @param converterSet the converter set to use for creating JSONObjects from Java objects.
	 * @return the JSONObject representing the input object.
	 * @since 1.3.0
	 */
	public static <T> JSONObject create(T object, JSONConverterSet converterSet)
	{
		if (object == null)
			return NULL;
		else if (object instanceof JSONObject) // TODO: maybe this should copy?
			return (JSONObject)object;
		else if (Utils.isArray(object))
		{
			int len = Array.getLength(object);
			JSONObject out = createEmptyArray();
			for (int i = 0; i < len; i++)
				out.append(JSONObject.create(Array.get(object, i), converterSet));
			return out;
		}
		else if (object instanceof Boolean)
			return new JSONObject(Type.BOOLEAN, object, NOT_ARRAY);
		else if (object instanceof Number)
			return new JSONObject(Type.NUMBER, object, NOT_ARRAY);
		else if (object instanceof String)
			return new JSONObject(Type.STRING, object, NOT_ARRAY);

		return createFromObject(object, converterSet);
	}

	/**
	 * Creates a JSONObject that represents an empty object type.
	 * @return a JSONObject representing a blank object.
	 */
	public static JSONObject createEmptyObject()
	{
		return new JSONObject(Type.OBJECT, new HashMap<String, JSONObject>(2), NOT_ARRAY);
	}
	
	/**
	 * Creates a JSONObject that represents an empty array type.
	 * @return a JSONObject representing an empty array.
	 */
	public static JSONObject createEmptyArray()
	{
		return new JSONObject(Type.OBJECT, new ArrayList<JSONObject>(3), 0);
	}
	
	/**
	 * Creates a JSONObject from a Java Object source.
	 * @param <T> the Java object type.
	 * @param object the object itself.
	 * @param converterSet the converter set to use for conversion.
	 * @return a JSONObject.
	 * @since 1.3.0
	 */
	static <T> JSONObject createFromObject(T object, JSONConverterSet converterSet)
	{
		@SuppressWarnings("unchecked")
		Class<T> clz = (Class<T>)object.getClass();
	
		JSONConverter<T> converter = converterSet.getConverter(clz);
		if (converter != null)
			return converter.getJSONObject(object);
		
		else if (object instanceof Enum)
		{
			return JSONObject.create(((Enum<?>)object).name(), converterSet);
		}
		else if (object instanceof Map<?, ?>)
		{
			JSONObject out = JSONObject.createEmptyObject();
			for (Map.Entry<?, ?> entry : ((Map<?, ?>)object).entrySet())
			{
				String key = String.valueOf(entry.getKey());
				out.addMember(key, converterSet, entry.getValue());
			}
			return out;
		}
		else if (object instanceof Iterable<?>)
		{
			JSONObject out = JSONObject.createEmptyArray();
			Iterator<?> it = ((Iterable<?>)object).iterator();
			while (it.hasNext())
				out.append(JSONObject.create(it.next(), converterSet));
			return out;
		}
		else
		{
			JSONObject out = JSONObject.createEmptyObject();
			Profile<?> profile = JSONObject.PROFILE_FACTORY.getProfile(clz);
	
			for (Map.Entry<String, MethodInfo> getters : profile.getGetterMethodsByName().entrySet())
			{
				String memberName = getters.getKey();
				String alias = getters.getValue().getAlias();
				Method method = getters.getValue().getMethod();
				out.addMember(Utils.isNull(alias, memberName), converterSet, Utils.invokeBlind(method, object));
			}
			for (Map.Entry<String, FieldInfo> fields : profile.getPublicFieldsByName().entrySet())
			{
				String memberName = fields.getKey();
				String alias = fields.getValue().getAlias();
				Field field = fields.getValue().getField();
				out.addMember(Utils.isNull(alias, memberName), converterSet, Utils.getFieldValue(object, field));
			}
			return out;
		}
	}

	/**
	 * JSON object constructor for generating
	 * @param type the internal JSON type of this object.
	 * @param value the object to encapsulate.
	 * @param arrayLength the length of the array, if an array.
	 */
	JSONObject(Type type, Object value, int arrayLength)
	{
		this.type = type;
		this.value = value;
		this.length = arrayLength;
	}

	/**
	 * Gets this object's JavaScript type.
	 * @return the object type.
	 */
	public Type getType()
	{
		return type;
	}
	
	/**
	 * Checks if this is Object-typed.
	 * @return true if so, false if not.
	 */
	public boolean isObject()
	{
		return type == Type.OBJECT;
	}
	
	/**
	 * Checks if this is Object-typed and is an array.
	 * @return true if so, false if not.
	 */
	public boolean isArray()
	{
		return type == Type.OBJECT && length > NOT_ARRAY;
	}

	/**
	 * Gets length of this array-typed object.
	 * @return the length of this array if this is an array, or -1 if not an array.
	 */
	public int length()
	{
		return length;
	}

	/**
	 * Gets the amount of members in this object, if this is an object.
	 * If this is an array, this returns its length.
	 * @return the member count, or 0 if not an object.
	 * @see #length()
	 */
	public int getMemberCount()
	{
		return isArray() ? length() : (isObject() ? getMap().size() : 0);
	}

	/** 
	 * Returns the underlying value as a map of string to object.
	 * @return the map.   
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, JSONObject> getMap()
	{
		return (Map<String, JSONObject>)value;
	}
	
	/** 
	 * Returns the underlying value as a list of objects. 
	 * @return the list.   
	 */
	@SuppressWarnings("unchecked")
	protected List<JSONObject> getList()
	{
		return (List<JSONObject>)value;
	}
	
	/**
	 * Returns an array of member names on this object, if
	 * it is Object typed. This may return an array of index numbers,
	 * if this is an array under the covers.
	 * @return an array of member names, suitable for use with {@link #get(String)}, or an empty array,
	 * if this does not represent an object.
	 */
	public String[] getMemberNames()
	{
		String[] out = EMPTY_MEMBER_LIST;
		if (isArray())
		{
			out = new String[length];
			for (int i = 0; i < length; i++)
				out[i] = String.valueOf(i);
		}
		else if (isObject())
		{
			Set<String> keys = getMap().keySet(); 
			out = keys.toArray(new String[keys.size()]);
		}
		
		return out;
	}
	
	/**
	 * Checks if this is a null object.
	 * @return true if so, false if not.
	 */
	public boolean isNull()
	{
		return type == Type.OBJECT && value == null;
	}
	
	/**
	 * Checks if this is UNDEFINED.
	 * @return true if so, false if not.
	 */
	public boolean isUndefined()
	{
		return type == Type.UNDEFINED;
	}
	
	/**
	 * Gets this object's encapsulated value. Can be null.
	 * @return the underlying value.
	 */
	public Object getValue()
	{
		return value;
	}
	
	/**
	 * Returns true if this is an object AND contains a member of a particular name.
	 * @param memberName the name of the member to check.
	 * @return true if it exists on the object, false if not or not an object.
	 */
	public boolean hasMember(String memberName)
	{
		if (isArray())
		{
			try {
				int i = Integer.parseInt(memberName);
				return i >= 0 && i < length; 
			} catch (NumberFormatException e) {
				return false;
			}
		}
		else if (isObject())
			return getMap().containsKey(memberName);
		else
			return false;
	}
	
	/**
	 * Gets an object member of this JSONObject by name.
	 * NOTE: Arrays are objects - their member names are the index position.
	 * @param memberName the name of the member to retrieve.
	 * @return a JSONObject corresponding to this member, or {@link JSONObject#UNDEFINED} if no
	 * member by that name or this isn't a JSON Object. 
	 */
	public JSONObject get(String memberName)
	{
		if (hasMember(memberName))
			return getMap().get(memberName);
		else
			return UNDEFINED;
	}
	
	/**
	 * Gets an object member of this JSONObject by index, if this is an array.
	 * @param index the index to retrieve.
	 * @return a JSONObject corresponding to this index, or {@link JSONObject#UNDEFINED} if no
	 * such index or this isn't a JSON Object or the index is out of bounds. 
	 */
	public JSONObject get(int index)
	{
		if (value == null)
			return UNDEFINED;
		else if (isArray())
		{
			if (index < 0 || index >= length)
				return UNDEFINED;
			else
				return getList().get(index);
		}
		return get(String.valueOf(index));
	}
	
	/**
	 * Returns the value of this JSON Object as a byte.
	 * @return the byte value, or 0 if this cannot be reasonably converted to a byte.
	 */
	public byte getByte()
	{
		if (type == Type.UNDEFINED)
			return 0;
		else if (type == Type.OBJECT)
			return 0;
		else if (value instanceof Boolean)
			return ((Boolean)value) ? (byte)1 : 0;
		else if (value instanceof Number)
			return ((Number)value).byteValue();
		else if (value instanceof String)
			return Utils.parseByte((String)value, (byte)0);
		else
			return 0;
	}
	
	/**
	 * Returns the value of this JSON Object as a short.
	 * @return the short value, or 0 if this cannot be reasonably converted to a short.
	 */
	public short getShort()
	{
		if (type == Type.UNDEFINED)
			return 0;
		else if (type == Type.OBJECT)
			return 0;
		else if (value instanceof Boolean)
			return ((Boolean)value) ? (short)1 : 0;
		else if (value instanceof Number)
			return ((Number)value).shortValue();
		else if (value instanceof String)
			return Utils.parseShort((String)value, (short)0);
		else
			return 0;
	}
	
	/**
	 * Returns the value of this JSON Object as an integer.
	 * @return the integer value, or 0 if this cannot be reasonably converted to an integer.
	 */
	public int getInt()
	{
		if (type == Type.UNDEFINED)
			return 0;
		else if (type == Type.OBJECT)
			return 0;
		else if (value instanceof Boolean)
			return ((Boolean)value) ? 1 : 0;
		else if (value instanceof Number)
			return ((Number)value).intValue();
		else if (value instanceof String)
			return Utils.parseInt((String)value, 0);
		else
			return 0;
	}
	
	/**
	 * Returns the value of this JSON Object as a float.
	 * @return the float value, or 0f if this cannot be reasonably converted to a float.
	 */
	public float getFloat()
	{
		if (type == Type.UNDEFINED)
			return 0f;
		else if (type == Type.OBJECT)
			return 0f;
		else if (value instanceof Boolean)
			return ((Boolean)value) ? 1f : 0f;
		else if (value instanceof Number)
			return ((Number)value).floatValue();
		else if (value instanceof String)
			return Utils.parseFloat((String)value, 0f);
		else
			return 0f;
	}
	
	/**
	 * Returns the value of this JSON Object as a long.
	 * @return the long value, or 0L if this cannot be reasonably converted to a long.
	 */
	public long getLong()
	{
		if (type == Type.UNDEFINED)
			return 0L;
		else if (type == Type.OBJECT)
			return 0L;
		else if (value instanceof Boolean)
			return ((Boolean)value) ? 1L : 0L;
		else if (value instanceof Number)
			return ((Number)value).longValue();
		else if (value instanceof String)
			return Utils.parseLong((String)value, 0L);
		else
			return 0L;
	}
	
	/**
	 * Returns the value of this JSON Object as a double.
	 * @return the double value, or 0.0 if this cannot be reasonably converted to a long.
	 */
	public double getDouble()
	{
		if (type == Type.UNDEFINED)
			return 0.0;
		else if (type == Type.OBJECT)
			return 0.0;
		else if (value instanceof Boolean)
			return ((Boolean)value) ? 1.0 : 0.0;
		else if (value instanceof Number)
			return ((Number)value).doubleValue();
		else if (value instanceof String)
			return Utils.parseDouble((String)value, 0.0);
		else
			return 0.0;
	}
	
	/**
	 * Returns the value of this JSON Object as an boolean.
	 * Non-null Objects are always true, <i>undefined</i> is always false.
	 * @return the boolean value, or false if this cannot be reasonably converted to a boolean.
	 */
	public boolean getBoolean()
	{
		if (type == Type.UNDEFINED)
			return false;
		else if (type == Type.OBJECT)
			return !isNull();
		else if (value instanceof Boolean)
			return ((Boolean)value);
		else if (value instanceof Number)
			return ((Number)value).doubleValue() != 0.0;
		else if (value instanceof String)
			return "true".equals((String)value);
		else
			return false;
	}
	
	/**
	 * Returns the value of this JSON Object as a String.
	 * If the value of this object is undefined or null, null is returned.
	 * If this is an object, <code>Object</code> is returned.
	 * @return the string value, or false if this cannot be reasonably converted to a boolean.
	 */
	public String getString()
	{
		if (isUndefined())
			return null;
		else if (isNull())
			return null;
		else if (isObject())
			return "Object";
		else if (isArray())
			return "Object";
		else if (value instanceof Boolean)
			return String.valueOf(value);
		else if (value instanceof Number)
			return String.valueOf(value);
		else if (value instanceof String)
			return String.valueOf(value);
		else
			return null;
	}
	
	@Override
	public String toString()
	{
		return String.valueOf(getString());
	}
	
	/**
	 * Removes a member from this JSONObject, if this is an Object type.
	 * If this is not an object (or array), this causes an error.
	 * WARNING: Array types are promoted to objects whether or not this succeeds.
	 * @param name the name of the member to remove.
	 * @return true if the member was removed, false if not.
	 * @throws IllegalStateException if this JSONObject is not an Object type 
	 * ({@link #isObject()} is false) or is null ({@link #isNull()} is true) or is UNDEFINED ({@link #isUndefined()} is true).
	 */
	public boolean removeMember(String name)
	{
		verifyObjectType();
		
		if (isArray())
			promoteArrayToObject();
		
		if (hasMember(name))
			return getMap().remove(name) != null;
		else
			return false;
	}

	/**
	 * Adds a member to this JSONObject, if this is an Object type.
	 * If this is not an object, this causes an error.
	 * If this is an array type, it is promoted to an object.
	 * @param name the member name. whitespace is trimmed from this.
	 * @param object the object value of the member.
	 * @throws IllegalArgumentException if name is an empty string or just whitespace.
	 * @throws IllegalStateException if this JSONObject is not an Object type 
	 * ({@link #isObject()} is false) or is null ({@link #isNull()} is true) or is UNDEFINED ({@link #isUndefined()} is true).
	 */
	public void addMember(String name, JSONObject object)
	{
		if (Utils.isEmpty(name))
			throw new IllegalArgumentException("Member name is empty, null, or whitespace.");

		verifyObjectType();
		
		if (isArray())
			promoteArrayToObject();
		
		getMap().put(name, object);
	}
	
	/**
	 * Adds a member to this JSONObject, if this is an Object type.
	 * If this is not an object, this causes an error.
	 * If this is an array type, it is promoted to an object.
	 * Since 1.3.0, it is preferred to use a {@link JSONConverterSet} for specifying how JSON objects get converted.	 
	 * @param name the member name. whitespace is trimmed from this.
	 * @param object the object value of the member.
	 * @throws IllegalArgumentException if name is an empty string or just whitespace.
	 * @throws IllegalStateException if this JSONObject is not an Object type 
	 * ({@link #isObject()} is false) or is null ({@link #isNull()} is true) or is UNDEFINED ({@link #isUndefined()} is true).
	 */
	public void addMember(String name, Object object)
	{
		addMember(name, GLOBAL_CONVERTER_SET, object);
	}
	
	/**
	 * Adds a member to this JSONObject, if this is an Object type.
	 * If this is not an object, this causes an error.
	 * If this is an array type, it is promoted to an object.
	 * @param name the member name. whitespace is trimmed from this.
	 * @param converterSet the converter set to use for creating JSONObjects from Java objects.
	 * @param object the object value of the member.
	 * @throws IllegalArgumentException if name is an empty string or just whitespace.
	 * @throws IllegalStateException if this JSONObject is not an Object type 
	 * ({@link #isObject()} is false) or is null ({@link #isNull()} is true) or is UNDEFINED ({@link #isUndefined()} is true).
	 * @since 1.3.0
	 */
	public void addMember(String name, JSONConverterSet converterSet, Object object)
	{
		addMember(name, JSONObject.create(object, converterSet));
	}
	
	/**
	 * Appends a member to the end of this JSONObject, but only if this is an array. 
	 * @param object the object to add.
	 * @throws IllegalStateException if this JSONObject is not an Array type 
	 * ({@link #isArray()} is false) or is null ({@link #isNull()} is true) or is UNDEFINED ({@link #isUndefined()} is true).
	 * @see #isArray()
	 */
	public void append(JSONObject object)
	{
		verifyArrayType();
		
		getList().add(object);
		length = getList().size();
	}

	/**
	 * Removes a member from a specific index in this JSONObject, shifting the contents, 
	 * but only if this is an array. 
	 * @param index the index to remove.
	 * @return the JSONObject at that index or null if no object at that index.
	 * @see #isArray()
	 * @throws IllegalStateException if this JSONObject is not an Array type 
	 * ({@link #isArray()} is false) or is null ({@link #isNull()} is true) or is UNDEFINED ({@link #isUndefined()} is true).
	 */
	public JSONObject removeAt(int index)
	{
		verifyArrayType();
		
		JSONObject out = getList().remove(index);
		length = getList().size();
		return out;
	}
	
	/**
	 * Removes a member from the ending index of this JSONObject, 
	 * but only if this is an array. 
	 * @return the JSONObject at index 0 or null if this is empty.
	 * @see #isArray()
	 * @throws IllegalStateException if this JSONObject is not an array type.
	 */
	public JSONObject pop()
	{
		verifyArrayType();
		
		return removeAt(length() - 1);
	}
	
	/**
	 * Adds a member to a specific index in this JSONObject, shifting the contents,
	 * but only if this is an array.
	 * @param index the index into the array. 
	 * @param object the object to add.
	 * @see #isArray()
	 * @throws IllegalStateException if this JSONObject is not an array type.
	 */
	public void addAt(int index, JSONObject object)
	{
		verifyArrayType();
		
		getList().add(index, object);
		length = getList().size();
	}
	
	/**
	 * Adds a member to the end of this JSONObject, 
	 * but only if this is an array.
	 * @param object the object to add.
	 * @see #isArray()
	 * @throws IllegalStateException if this JSONObject is not an array type.
	 */
	public void push(JSONObject object)
	{
		append(object);
	}

	/**
	 * Creates a new instance of a class, populated with values from this object. 
	 * Since 1.3.0, it is preferred to use a {@link JSONConverterSet} for specifying how JSON objects get converted.	 
	 * <p>
	 * This JSON object is applied via the target object's public fields
	 * and setter methods, if this is an object and the target class is not a primitive
	 * or autoboxed primitive.
	 * <p>
	 * For instance, if there is a member on this object called "color", its value
	 * will be applied via the public field "color" or the setter "setColor()". Public
	 * fields take precedence over setters.
	 * <p>
	 * If the input object is an array, then the contents of the indices are replaced,
	 * up to the length of the input array or this JSON array, whichever's shorter.
	 * @param <T> the return type.
	 * @param clazz the class to instantiate and apply.
	 * @return a new instance of this object, or null if this object's value is null (see {@link #isNull()}.
	 * @throws RuntimeException if the object cannot be created.
	 * @throws JSONConversionException if an error occurs during conversion/application.
	 */
	public <T> T newObject(Class<T> clazz)
	{
		return newObject(clazz, GLOBAL_CONVERTER_SET);
	}
	
	/**
	 * Creates a new instance of a class, populated with values from this object. 
	 * <p>
	 * This JSON object is applied via the target object's public fields
	 * and setter methods, if this is an object and the target class is not a primitive
	 * or autoboxed primitive.
	 * <p>
	 * For instance, if there is a member on this object called "color", its value
	 * will be applied via the public field "color" or the setter "setColor()". Public
	 * fields take precedence over setters.
	 * <p>
	 * If the input object is an array, then the contents of the indices are replaced,
	 * up to the length of the input array or this JSON array, whichever's shorter.
	 * @param <T> the return type.
	 * @param clazz the class to instantiate and apply.
	 * @param converterSet the converter set to use for certain specific object types.
	 * @return a new instance of this object, or null if this object's value is null (see {@link #isNull()}.
	 * @throws RuntimeException if the object cannot be created.
	 * @throws JSONConversionException if an error occurs during conversion/application.
	 * @since 1.3.0
	 */
	@SuppressWarnings("unchecked")
	public <T> T newObject(Class<T> clazz, JSONConverterSet converterSet)
	{
		if (isNull())
			return null;
		
		if (Utils.isArray(clazz))
		{
			Object obj = Array.newInstance(Utils.getArrayType(clazz), length);
			return applyToObject(clazz.cast(obj), converterSet);
		}
		
		if (isObject())
			return applyToObject(Utils.create(clazz), converterSet);
		
		if (clazz == Boolean.TYPE)
			return (T)Boolean.valueOf(getBoolean());
		else if (clazz == Boolean.class)
			return clazz.cast(getBoolean());
		else if (clazz == Byte.TYPE)
			return (T)Byte.valueOf(getByte());
		else if (clazz == Byte.class)
			return clazz.cast(getByte());
		else if (clazz == Short.TYPE)
			return (T)Short.valueOf(getShort());
		else if (clazz == Short.class)
			return clazz.cast(getShort());
		else if (clazz == Integer.TYPE)
			return (T)Integer.valueOf(getInt());
		else if (clazz == Integer.class)
			return clazz.cast(getInt());
		else if (clazz == Float.TYPE)
			return (T)Float.valueOf(getFloat());
		else if (clazz == Float.class)
			return clazz.cast(getFloat());
		else if (clazz == Long.TYPE)
			return (T)Long.valueOf(getLong());
		else if (clazz == Long.class)
			return clazz.cast(getLong());
		else if (clazz == Double.TYPE)
			return (T)Double.valueOf(getDouble());
		else if (clazz == Double.class)
			return clazz.cast(getDouble());
		
		return null;
	}

	/**
	 * Applies this object to an object bean / plain ol' Java object, or Array.
	 * Since 1.3.0, it is preferred to use a {@link JSONConverterSet} for specifying how JSON objects get converted.	 
	 * <p>
	 * This JSON object is applied via the target object's public fields
	 * and setter methods, if an object.
	 * <p>
	 * For instance, if there is a member on this object called "color", its value
	 * will be applied via the public field "color" or the setter "setColor()". Public
	 * fields take precedence over setters.
	 * <p>
	 * If the input object is an array, then the contents of the indices are replaced,
	 * up to the length of the input array or this JSON array, whichever's shorter.
	 * @param <T> the return type.
	 * @param object the object to set the fields/indices of.
	 * @return the input object.
	 * @throws JSONConversionException if an error occurs during conversion/application.
	 */
	public <T> T applyToObject(T object)
	{
		return applyToObject(object, GLOBAL_CONVERTER_SET);
	}
	
	/**
	 * Applies this object to an object bean / plain ol' Java object, or Array.
	 * <p>
	 * This JSON object is applied via the target object's public fields
	 * and setter methods, if an object.
	 * <p>
	 * For instance, if there is a member on this object called "color", its value
	 * will be applied via the public field "color" or the setter "setColor()". Public
	 * fields take precedence over setters.
	 * <p>
	 * If the input object is an array, then the contents of the indices are replaced,
	 * up to the length of the input array or this JSON array, whichever's shorter.
	 * @param <T> the return type.
	 * @param object the object to set the fields/indices of.
	 * @param converterSet the converter set to use for certain specific object types.
	 * @return the input object.
	 * @throws JSONConversionException if an error occurs during conversion/application.
	 * @since 1.3.0
	 */
	@SuppressWarnings("unchecked")
	public <T> T applyToObject(T object, JSONConverterSet converterSet)
	{
		if (Utils.isArray(object))
		{
			if (!isArray())
				throw new JSONConversionException("Target is array, but not this object.");
			
			Class<?> atype = Utils.getArrayType(object);
			int len = Math.min(length, Array.getLength(object));
			for (int i = 0; i < len; i++)
				Array.set(object, i, createForType(String.format("this[%d]", i), get(i), converterSet, atype, null, null));
			
			return object;
		}
		
		Profile<T> profile = PROFILE_FACTORY.getProfile((Class<T>)object.getClass());

		for (String member : getMemberNames())
		{
			FieldInfo fieldInfo = null; 
			MethodInfo setterInfo = null;
			
			if ((fieldInfo = Utils.isNull(profile.getPublicFieldsByAlias().get(member), (profile.getPublicFieldsByName().get(member)))) != null)
			{
				Class<?> type = fieldInfo.getType();
				String alias = fieldInfo.getAlias();
				JSONObject jsobj = get(Utils.isNull(alias, member));
				if (!jsobj.isUndefined())
					Utils.setFieldValue(object, fieldInfo.getField(), createForType(member, jsobj, converterSet, type, fieldInfo.getKeyClass(), fieldInfo.getValueClass()));
			}
			else if ((setterInfo = Utils.isNull(profile.getSetterMethodsByAlias().get(member), (profile.getSetterMethodsByName().get(member)))) != null)
			{
				Class<?> type = setterInfo.getType();
				String alias = setterInfo.getAlias();
				Method method = setterInfo.getMethod();
				JSONObject jsobj = get(Utils.isNull(alias, member));
				if (!jsobj.isUndefined())
					Utils.invokeBlind(method, object, createForType(member, jsobj, converterSet, type, setterInfo.getKeyClass(), setterInfo.getValueClass()));
			}			
		}
		
		return object;
	}

	/**
	 * Promotes this array to an object.
	 */
	private void promoteArrayToObject()
	{
		HashMap<String, JSONObject> newmap = new HashMap<String, JSONObject>();
		for (int i = 0; i < length; i++)
			newmap.put(String.valueOf(i), get(i));
		value = newmap;
		length = NOT_ARRAY;
	}

	private void verifyArrayType() 
	{
		if (isNull())
			throw new IllegalStateException("Object is null.");
		
		if (isUndefined())
			throw new IllegalStateException("Object is undefined.");
		
		if (!isArray())
			throw new IllegalStateException("This is not an array Object type.");
	}
	
	private void verifyObjectType()
	{
		if (isNull())
			throw new IllegalStateException("Object is null.");
		
		if (isUndefined())
			throw new IllegalStateException("Object is undefined.");
		
		if (!isObject())
			throw new IllegalStateException("This is not an Object type.");
	}
	
	private static <T> T newClassInstance(String memberName, JSONConverterSet converterSet, Class<T> type)
	{
		try {
			return type.getDeclaredConstructor().newInstance();
		} catch (InstantiationException e) {
			throw new JSONConversionException("Member "+memberName+" cannot be converted; no nullary constructor or type is not instantiable.", e);
		} catch (IllegalAccessException e) {
			throw new JSONConversionException("Member "+memberName+" cannot be converted; constructor is not accessible.", e);
		} catch (ExceptionInInitializerError e) {
			throw new JSONConversionException("Member "+memberName+" cannot be converted; problem occurred during instantiation.", e);
		} catch (SecurityException e) {
			throw new JSONConversionException("Member "+memberName+" cannot be converted. Cannot access default constructor.", e);
		} catch (IllegalArgumentException e) {
			throw new JSONConversionException("Member "+memberName+" cannot be converted. Internal error.", e);
		} catch (InvocationTargetException e) {
			throw new JSONConversionException("Member "+memberName+" cannot be converted. Internal error.", e);
		} catch (NoSuchMethodException e) {
			throw new JSONConversionException("Member "+memberName+" cannot be converted. No default constructor.", e);
		}
	}
	
	// Creates an object for application later.
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <T, K, V> T createForType(String memberName, JSONObject jsonObject, JSONConverterSet converterSet, Class<T> type, Class<K> keyType, Class<V> valueType)
	{
		if (JSONObject.class.isAssignableFrom(type))
		{
			return (T)jsonObject;
		}

		Type jsonType = jsonObject.getType();
		
		JSONConverter<T> converter;
		if ((converter = converterSet.getConverter(type)) != null)
			return converter.getObject(jsonObject);
		
		switch (jsonType)
		{
			case BOOLEAN:
			{
				if (type == Boolean.TYPE)
					return (T)Boolean.valueOf(jsonObject.getBoolean());
				else if (type == Boolean.class)
					return type.cast(jsonObject.getBoolean());
				else if (type == Object.class)
					return type.cast(jsonObject.getBoolean());
				else
					throw new JSONConversionException("Member "+memberName+" is boolean typed; target is not boolean or Boolean.");
			}
				
			case NUMBER:
			{
				if (type == Boolean.TYPE)
					return (T)Boolean.valueOf(jsonObject.getBoolean());
				else if (type == Boolean.class)
					return type.cast(jsonObject.getBoolean());
				else if (type == Byte.TYPE)
					return (T)Byte.valueOf(jsonObject.getByte());
				else if (type == Byte.class)
					return type.cast(jsonObject.getByte());
				else if (type == Short.TYPE)
					return (T)Short.valueOf(jsonObject.getShort());
				else if (type == Short.class)
					return type.cast(jsonObject.getShort());
				else if (type == Integer.TYPE)
					return (T)Integer.valueOf(jsonObject.getInt());
				else if (type == Integer.class)
					return type.cast(jsonObject.getInt());
				else if (type == Float.TYPE)
					return (T)Float.valueOf(jsonObject.getFloat());
				else if (type == Float.class)
					return type.cast(jsonObject.getFloat());
				else if (type == Long.TYPE)
					return (T)Long.valueOf(jsonObject.getLong());
				else if (type == Long.class)
					return type.cast(jsonObject.getLong());
				else if (type == Double.TYPE)
					return (T)Double.valueOf(jsonObject.getDouble());
				else if (type == Double.class)
					return type.cast(jsonObject.getDouble());
				else if (type == Object.class)
				{
					if ((double)jsonObject.getLong() == jsonObject.getDouble())
					{
						long ln = jsonObject.getLong();
						if (ln >= (long)Integer.MIN_VALUE && ln <= (long)Integer.MAX_VALUE)
							return type.cast(Integer.valueOf(jsonObject.getInt()));
						return type.cast(Long.valueOf(jsonObject.getLong()));
					}
					else
						return type.cast(Double.valueOf(jsonObject.getDouble()));
				}
				else
					throw new JSONConversionException("Member "+memberName+" is numerically typed; target is not a numeric type.");
			}
			
			case STRING:
			{
				if (type.isEnum())
				{
					try {
						return (T)type.cast(Enum.valueOf((Class<Enum>)type, jsonObject.getString()));
					} catch (IllegalArgumentException e) {
						return null;
					}
				}
				else if (type == String.class)
					return type.cast(jsonObject.getString());
				else if (type == Object.class)
					return type.cast(jsonObject.getString());
				else
					throw new JSONConversionException("Member "+memberName+" is string typed; target is not String.");
			}
			
			case OBJECT:
			{
				// Nulls.
				if (jsonObject.isNull())
					return null;
				
				// Arrays.
				if (jsonObject.isArray())
				{
					// Target is Iterable.
					if (type == Iterable.class)
					{
						Collection<K> coll = new ArrayList<K>(jsonObject.length);
						for (int i = 0; i < jsonObject.length; i++)
							coll.add(createForType(String.format("%s[%d]", memberName, i), jsonObject.get(i), converterSet, keyType, null, null));
						return type.cast(coll);
					}
					// Target is Collection.
					else if (Collection.class.isAssignableFrom(type))
					{
						Object out;
						// Not instantiate-able.
						if (type.isInterface() || (type.getModifiers() & Modifier.ABSTRACT) != 0)
						{
							Collection<K> coll;
							if (SortedSet.class.isAssignableFrom(type))
								coll = new TreeSet<K>();
							else if (Set.class.isAssignableFrom(type))
								coll = new HashSet<K>(jsonObject.length);
							else
								coll = new ArrayList<K>(jsonObject.length);
							
							for (int i = 0; i < jsonObject.length; i++)
								coll.add(createForType(String.format("%s[%d]", memberName, i), jsonObject.get(i), converterSet, keyType, null, null));
							out = coll;
						}
						else
						{
							Collection<K> coll = (Collection<K>)newClassInstance(memberName, converterSet, type);
							for (int i = 0; i < jsonObject.length; i++)
								coll.add(createForType(String.format("%s[%d]", memberName, i), jsonObject.get(i), converterSet, keyType, null, null));
							out = coll;
						}
						return type.cast(out);
					}
					// type is array
					else if (Utils.isArray(type))
					{
						Object newarray = Array.newInstance(keyType, jsonObject.length);
						for (int i = 0; i < jsonObject.length; i++)
							Array.set(newarray, i, createForType(String.format("%s[%d]", memberName, i), jsonObject.get(i), converterSet, keyType, null, null));
						return type.cast(newarray);
					}
					else
						throw new JSONConversionException("Member "+memberName+" cannot be converted; member is array and target is not array typed or a single-type Collection type.");
				}
				
				// Target is Map.
				if (Map.class.isAssignableFrom(type))
				{
					Object out;
					// Not instantiate-able.
					if (type.isInterface() || (type.getModifiers() & Modifier.ABSTRACT) != 0)
					{
						String[] keys = jsonObject.getMemberNames();
						Map<K, V> map = new HashMap<K, V>(keys.length);
						for (String key : keys)
							map.put(
								createForType(String.format("%s->%s", memberName, key), JSONObject.create(key), converterSet, keyType, null, null), 
								createForType(String.format("%s[%s]", memberName, key), jsonObject.get(key), converterSet, valueType, null, null)
							);
						out = map;
					}
					else
					{
						String[] keys = jsonObject.getMemberNames();
						Map<K, V> map = (Map<K, V>)newClassInstance(memberName, converterSet, type);
						for (String key : keys)
							map.put(
								createForType(String.format("%s->%s", memberName, key), JSONObject.create(key), converterSet, keyType, null, null), 
								createForType(String.format("%s[%s]", memberName, key), jsonObject.get(key), converterSet, valueType, null, null)
							);
						out = map;
					}
					return type.cast(out);
				}
				// Objects.
				else
				{
					T out = newClassInstance(memberName, converterSet, type);
					jsonObject.applyToObject(out);
					return out;
				}
			}
			
			case UNDEFINED:
			{
				throw new JSONConversionException("Cannot apply "+memberName+". Undefined types cannot be applied.");
			}
			
			default:
				return null;
			
		}
	}

}
