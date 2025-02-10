package de.m_marvin.industria.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import kotlin.collections.ArrayDeque;
import net.minecraftforge.fml.unsafe.UnsafeHacks;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import sun.misc.Unsafe;

public class UnsafeUtility {

	private static final Unsafe theUnsafe;
	
	static {
		try {
			Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafeField.setAccessible(true);
			theUnsafe = (Unsafe) theUnsafeField.get(null);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			throw new RuntimeException("UNSAFE ACCESS ERROR :(", e);
		}
	}
	
	private static final Map<Class<?>, Set<Field>> fieldCache = new ConcurrentHashMap<Class<?>, Set<Field>>();
	
	public static Set<Field> getAllClassFields(Class<?> clazz) {
		Set<Field> fields = fieldCache.get(clazz);
		if (fields == null) {
			fields = Stream.of(clazz.getDeclaredFields())
				.filter(f -> !Modifier.isStatic(f.getModifiers())).collect(Collectors.toSet());
			for (Field field : fields)
				field.setAccessible(true);
			for (Class<?> i : clazz.getInterfaces())
				fields.addAll(getAllClassFields(i));
			Class<?> s = clazz.getSuperclass();
			if (s != null)
				fields.addAll(getAllClassFields(s));
			fieldCache.put(clazz, fields);
		}
		return fields;
	}
	
	public static void copyClassFields(Class<?> clazz, Object source, Object target) {
		Objects.requireNonNull(source);
		Objects.requireNonNull(target);
		if (!clazz.isInstance(source))
			throw new IllegalArgumentException("supplied source object " + source.toString() + " is not of type " + clazz.getName());
		if (!clazz.isInstance(target))
			throw new IllegalArgumentException("supplied target object " + target.toString() + " is not of type " + clazz.getName());
		Set<Field> fields = getAllClassFields(clazz);
		System.out.println("- start of dump -");
		for (Field field : fields) {
			
			// FIXME UNSAFE ACCESS ERROR
			
			System.out.println(field.getType() + " \t " + field);
			
		}
		System.out.println("- end of dump -");
	}
	
}
