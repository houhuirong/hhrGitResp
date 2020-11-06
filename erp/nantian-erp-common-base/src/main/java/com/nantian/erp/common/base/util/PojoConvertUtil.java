package com.nantian.erp.common.base.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PO转换类
 * @author 71838065.nantian.co
 *
 */
public class PojoConvertUtil {
	private static Logger logger = LoggerFactory.getLogger(PojoConvertUtil.class);
	/**
	 * 变量缓存
	 */
	private static final Map<String, Map<String, Field>> cacheFields = new ConcurrentHashMap<>();
	private static final Set<Class> basicClass = new HashSet<>();
	static {
		basicClass.add(Integer.class);
		basicClass.add(Character.class);
		basicClass.add(Byte.class);
		basicClass.add(Float.class);
		basicClass.add(Double.class);
		basicClass.add(Boolean.class);
		basicClass.add(Long.class);
		basicClass.add(Short.class);
		basicClass.add(String.class);
		basicClass.add(BigDecimal.class);
	}

	/**
	 * 将具有相同属性的类型进行转换
	 * 
	 * @param orig
	 * @param <T>
	 * @return
	 */
	public static <T> T convertPojo(Object orig, Class<T> targetClass) {
		try {
			T target = targetClass.newInstance();
			/** 获取源对象的所有变量 */
			Field[] fields = orig.getClass().getDeclaredFields();
			for (Field field : fields) {
				if (isStatic(field))
					continue;
				/** 获取目标方法 */
				Field targetField = getTargetField(targetClass, field.getName());
				if (targetField == null)
					continue;
				Object value = getFiledValue(field, orig);
				if (value == null)
					continue;
				Class type1 = field.getType();
				Class type2 = targetField.getType();
				// 两个类型是否相同
				boolean sameType = type1.equals(type2);
				if (isBasicType(type1)) {
					if (sameType)
						setFieldValue(targetField, target, value);
				} else if (value instanceof Map && Map.class.isAssignableFrom(type2)) {// 对map
					setMap((Map) value, field, targetField, target);
				} else if (value instanceof Set && Set.class.isAssignableFrom(type2)) {// 对set
					setCollection((Collection) value, field, targetField, target);
				} else if (value instanceof List && List.class.isAssignableFrom(type2)) {// 对list
					setCollection((Collection) value, field, targetField, target);
				} else if (value instanceof Enum && Enum.class.isAssignableFrom(type2)) {// 对enum
					setEnum((Enum) value, field, targetField, target);
				} else if (value instanceof java.util.Date && java.util.Date.class.isAssignableFrom(type2)) {// 对日期类型，不处理如joda包之类的扩展时间，不处理calendar
					setDate((Date) value, targetField, type2, target, sameType);
				}
			}
			return target;
		} catch (Throwable t) {
			logger.error("转换失败:" + t.getMessage());
			throw new RuntimeException(t.getMessage());
		}
	}

	/**
	 * 获取字段值
	 * 
	 * @param field
	 * @param obj
	 * @return
	 */
	private static Object getFiledValue(Field field, Object obj) throws IllegalAccessException {
		// 获取原有的访问权限
		boolean access = field.isAccessible();
		try {
			// 设置可访问的权限
			field.setAccessible(true);
			return field.get(obj);
		} finally {
			// 恢复访问权限
			field.setAccessible(access);
		}
	}

	/**
	 * 设置方法值
	 * 
	 * @param field
	 * @param obj
	 * @param value
	 * @throws IllegalAccessException
	 */
	private static void setFieldValue(Field field, Object obj, Object value) throws IllegalAccessException {
		// 获取原有的访问权限
		boolean access = field.isAccessible();
		try {
			// 设置可访问的权限
			field.setAccessible(true);
			field.set(obj, value);
		} finally {
			// 恢复访问权限
			field.setAccessible(access);
		}
	}

	/**
	 * 转换list
	 * 
	 * @param orig
	 * @param targetClass
	 * @param <T>
	 * @return
	 */
	public static <T> List<T> convertPojos(List orig, Class<T> targetClass) {
		List<T> list = new ArrayList<>(orig.size());
		for (Object object : orig) {
			list.add(convertPojo(object, targetClass));
		}
		return list;
	}

	/**
	 * 设置Map
	 * 
	 * @param value
	 * @param origField
	 * @param targetField
	 * @param targetObject
	 * @param <T>
	 */
	private static <T> void setMap(Map value, Field origField, Field targetField, T targetObject)
			throws IllegalAccessException, InstantiationException {
		Type origType = origField.getGenericType();
		Type targetType = targetField.getGenericType();
		if (origType instanceof ParameterizedType && targetType instanceof ParameterizedType) {// 泛型类型
			ParameterizedType origParameterizedType = (ParameterizedType) origType;
			Type[] origTypes = origParameterizedType.getActualTypeArguments();
			ParameterizedType targetParameterizedType = (ParameterizedType) targetType;
			Type[] targetTypes = targetParameterizedType.getActualTypeArguments();
			if (origTypes != null && origTypes.length == 2 && targetTypes != null && targetTypes.length == 2) {// 正常泛型,查看第二个泛型是否不为基本类型
				Class clazz = (Class) origTypes[1];
				if (!isBasicType(clazz) && !clazz.equals(targetTypes[1])) {// 如果不是基本类型并且泛型不一致，则需要继续转换
					Set<Map.Entry> entries = value.entrySet();
					Map targetMap = value.getClass().newInstance();
					for (Map.Entry entry : entries) {
						targetMap.put(entry.getKey(), convertPojo(entry.getValue(), (Class) targetTypes[1]));
					}
					setFieldValue(targetField, targetObject, targetMap);
					return;
				}
			}
		}
		setFieldValue(targetField, targetObject, value);
	}

	/**
	 * 设置集合
	 * 
	 * @param value
	 * @param origField
	 * @param targetField
	 * @param targetObject
	 * @param <T>
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private static <T> void setCollection(Collection value, Field origField, Field targetField, T targetObject)
			throws IllegalAccessException, InstantiationException {
		Type origType = origField.getGenericType();
		Type targetType = targetField.getGenericType();
		if (origType instanceof ParameterizedType && targetType instanceof ParameterizedType) {// 泛型类型
			ParameterizedType origParameterizedType = (ParameterizedType) origType;
			Type[] origTypes = origParameterizedType.getActualTypeArguments();
			ParameterizedType targetParameterizedType = (ParameterizedType) targetType;
			Type[] targetTypes = targetParameterizedType.getActualTypeArguments();
			if (origTypes != null && origTypes.length == 1 && targetTypes != null && targetTypes.length == 1) {// 正常泛型,查看第二个泛型是否不为基本类型
				Class clazz = (Class) origTypes[0];
				if (!isBasicType(clazz) && !clazz.equals(targetTypes[0])) {// 如果不是基本类型并且泛型不一致，则需要继续转换
					Collection collection = value.getClass().newInstance();
					for (Object obj : value) {
						collection.add(convertPojo(obj, (Class) targetTypes[0]));
					}
					setFieldValue(targetField, targetObject, collection);
					return;
				}
			}
		}
		setFieldValue(targetField, targetObject, value);
	}

	/**
	 * 设置枚举类型
	 * 
	 * @param value
	 * @param origField
	 * @param targetField
	 * @param targetObject
	 * @param <T>
	 */
	private static <T> void setEnum(Enum value, Field origField, Field targetField, T targetObject) throws Exception {
		if (origField.equals(targetField)) {
			setFieldValue(targetField, targetObject, value);
		} else {
			// 枚举类型都具有一个static修饰的valueOf方法
			Method method = targetField.getType().getMethod("valueOf", String.class);
			setFieldValue(targetField, targetObject, method.invoke(null, value.toString()));
		}
	}

	/**
	 * 设置日期类型
	 * 
	 * @param value
	 * @param targetField
	 * @param targetFieldType
	 * @param targetObject
	 * @param <T>
	 */
	private static <T> void setDate(Date value, Field targetField, Class targetFieldType, T targetObject,
			boolean sameType) throws IllegalAccessException {
		Date date = null;
		if (sameType) {
			date = value;
		} else if (targetFieldType.equals(java.sql.Date.class)) {
			date = new java.sql.Date(value.getTime());
		} else if (targetFieldType.equals(java.util.Date.class)) {
			date = new Date(value.getTime());
		} else if (targetFieldType.equals(java.sql.Timestamp.class)) {
			date = new java.sql.Timestamp(value.getTime());
		}
		setFieldValue(targetField, targetObject, date);
	}

	/**
	 * 获取适配方法
	 * 
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
	public static Field getTargetField(Class clazz, String fieldName) {
		String classKey = clazz.getName();
		Map<String, Field> fieldMap = cacheFields.get(classKey);
		if (fieldMap == null) {
			fieldMap = new HashMap<>();
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				if (isStatic(field))
					continue;
				fieldMap.put(field.getName(), field);
			}
			cacheFields.put(classKey, fieldMap);
		}
		return fieldMap.get(fieldName);
	}

	/**
	 * 确实是否为基础类型
	 * 
	 * @param clazz
	 * @return
	 */
	public static boolean isBasicType(Class clazz) {
		return clazz.isPrimitive() || basicClass.contains(clazz);
	}

	/**
	 * 判断变量是否有静态修饰符static
	 * 
	 * @param field
	 * @return
	 */
	public static boolean isStatic(Field field) {
		return (8 & field.getModifiers()) == 8;
	}

	public static Map Objcet2Map(Object obj) throws Exception {
		Map map = new HashMap<String, Object>();
		Class clazz = obj.getClass();
		// 获取所有属性
		Field[] fields = clazz.getDeclaredFields();
		// 迭代属性数组
		for (Field field : fields) {
			// 获取属性名
			String name = field.getName();
			// 拼接get方法名
			String methodName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
			// 获取方法
			Method method = clazz.getDeclaredMethod(methodName);
			// 执行方法
			Object value = method.invoke(obj);
			map.put(name, value);
		}
		return map;
	}

	/**
	 * 把一个map集合转化为目标对象
	 * 
	 * @param clazz
	 *            目标对象的字节码对象
	 * @param map
	 *            map集合
	 * @param exceptFields
	 *            不需要转换的属性集合
	 * @return 字节码对象生产的目标对象
	 */
	public static <T> T map2Object(Class<T> clazz, Map map, List<String> exceptFields) throws Exception {
		// 构造迭代器
		Iterator iterator = map.entrySet().iterator();
		// 根据字节码生产对象
		T obj = clazz.newInstance();
		// 迭代map集合
		while (iterator.hasNext()) {
			// Entry <K,V>
			Entry e = (Entry) iterator.next();
			// 获取Key 和 Value
			String key = (String) e.getKey();
			key = key.toLowerCase();
			if (key.indexOf("_") != -1) {
				String[] names = key.split("_");
				StringBuffer sb = new StringBuffer(); 
				for (int i = 0; i < names.length; i++) {
					if (i==0) {
						sb.append(names[0].toLowerCase());
					} else {
						sb.append(names[i].substring(0,1).toUpperCase()+names[i].substring(1).toLowerCase());
					}
				}
				key = sb.toString(); 
			}
			Object value = e.getValue();
			
			if (exceptFields != null && exceptFields.contains(key)) {
				continue;
			}
			
			// 根据属性名称获取属性
			Field field = clazz.getDeclaredField(key);
			String typeName = field.getType().getName();
			// 拼接set方法名 因为 field.set(obj, value)方法不允许修改private 的属性
			String methodName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1, key.length());
			// 根据方法名称和参数类型获取方法
			Method method = clazz.getDeclaredMethod(methodName, field.getType());
			// 执行方法
			if (value instanceof Character) {
				if (value != null) {
					method.invoke(obj, String.valueOf(value));
				}
			} else if (value instanceof BigDecimal){
				if (value != null) {
					if ("java.lang.Integer".equals(typeName)  || "int".equals(typeName)) {
						method.invoke(obj, Integer.parseInt(value.toString()));
					} else if ("java.lang.Long".equals(typeName) || "long".equals(typeName)) {
						method.invoke(obj, Long.parseLong(value.toString()));
					} else if ("java.math.Bigdecimal".equals(typeName)) {
						method.invoke(obj, new BigDecimal(value.toString()));
					} else {
						method.invoke(obj, value.toString());
					}
				}
			} else if (value instanceof Clob) {
				if (value != null) {
					if ("java.lang.String".equals(typeName)) {
						method.invoke(obj, clob2String((Clob)value));
					}
				}
			} else {
				if (value != null) {
					method.invoke(obj, value);
				}
			}
		}

		return obj;
	}
	
	/**
	 * 把一个map集合转化为目标对象
	 * 
	 * @param clazz
	 *            目标对象的字节码对象
	 * @param map
	 *            map集合
	 * @return 字节码对象生产的目标对象
	 */
	public static <T> T map2Object(Class<T> clazz, Map map) throws Exception {
		return map2Object(clazz, map, null);
	}
	
	/**
	 * 读取clob字段转换成字符串
	 * @param clob
	 * @return
	 */
	private static String clob2String(Clob clob) {
		BufferedReader bReader = null;
		Reader is = null;
		StringBuffer sBuffer = new StringBuffer();
		try {
			is = clob.getCharacterStream();
			bReader = new BufferedReader(is);
			String temp = null;
			while ((temp = bReader.readLine()) != null) {
				sBuffer.append(temp);
			}
		} catch (SQLException e) {
			logger.error("读取clob字段出错", e);
		} catch (IOException e) {
			logger.error("读取clob字段出错", e);
		}finally {
			if (bReader != null) {
				try {
					bReader.close();
				} catch (IOException e) {
					logger.error("读取clob字段出错", e);
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					logger.error("读取clob字段出错", e);
				}
			}
		}
		return sBuffer.toString();
	}
}
