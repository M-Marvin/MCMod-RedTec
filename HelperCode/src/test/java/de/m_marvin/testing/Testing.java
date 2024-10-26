package de.m_marvin.testing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Testing {
	
	public static class A {
		public void function() {
			System.out.println("A");
		}
	}
	
	public static class B extends A {
		@Override
		public void function() {
			System.out.println("B");
		}
	}
	
	
	public static class C extends B {
		@Override
		public void function() {
			super.function();
		}
	}
	
	
	public static void main(String[] args) throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
		
		C test = new C();
		
		Method ssm = A.class.getDeclaredMethod("function");
		ssm.setAccessible(true);
		
		System.out.println("Super call:");
		test.function();

		System.out.println("Method Super-Super call:");
		ssm.invoke((B) test);
		
	}
	
}
