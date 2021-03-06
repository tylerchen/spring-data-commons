/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.mapping.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.ReflectionUtils;

/**
 * Unit tests for {@link AbstractPersistentProperty}.
 * 
 * @author Oliver Gierke
 */
public class AbstractPersistentPropertyUnitTests {

	TypeInformation<TestClassComplex> typeInfo;
	PersistentEntity<TestClassComplex, SamplePersistentProperty> entity;
	SimpleTypeHolder typeHolder;

	@Before
	public void setUp() {
		typeInfo = ClassTypeInformation.from(TestClassComplex.class);
		entity = new BasicPersistentEntity<TestClassComplex, SamplePersistentProperty>(typeInfo);
		typeHolder = new SimpleTypeHolder();
	}

	/**
	 * @see DATACMNS-68
	 */
	@Test
	public void discoversComponentTypeCorrectly() throws Exception {

		Field field = ReflectionUtils.findField(TestClassComplex.class, "testClassSet");

		SamplePersistentProperty property = new SamplePersistentProperty(field, null, entity, typeHolder);
		property.getComponentType();
	}

	@Test
	public void returnsNestedEntityTypeCorrectly() {

		Field field = ReflectionUtils.findField(TestClassComplex.class, "testClassSet");

		SamplePersistentProperty property = new SamplePersistentProperty(field, null, entity, typeHolder);
		assertThat(property.getPersistentEntityType().iterator().hasNext(), is(false));
	}

	/**
	 * @see DATACMNS-132
	 */
	@Test
	public void isEntityWorksForUntypedMaps() throws Exception {

		Field field = ReflectionUtils.findField(TestClassComplex.class, "map");
		SamplePersistentProperty property = new SamplePersistentProperty(field, null, entity, typeHolder);
		assertThat(property.isEntity(), is(false));
	}

	/**
	 * @see DATACMNS-132
	 */
	@Test
	public void isEntityWorksForUntypedCollection() throws Exception {

		Field field = ReflectionUtils.findField(TestClassComplex.class, "collection");
		SamplePersistentProperty property = new SamplePersistentProperty(field, null, entity, typeHolder);
		assertThat(property.isEntity(), is(false));
	}

	/**
	 * @see DATACMNS-121
	 */
	@Test
	public void considersPropertiesEqualIfFieldEquals() {

		Field first = ReflectionUtils.findField(FirstConcrete.class, "genericField");
		Field second = ReflectionUtils.findField(SecondConcrete.class, "genericField");

		SamplePersistentProperty firstProperty = new SamplePersistentProperty(first, null, entity, typeHolder);
		SamplePersistentProperty secondProperty = new SamplePersistentProperty(second, null, entity, typeHolder);

		assertThat(firstProperty, is(secondProperty));
		assertThat(firstProperty.hashCode(), is(secondProperty.hashCode()));
	}

	/**
	 * @see DATACMNS-180
	 */
	@Test
	public void doesNotConsiderJavaTransientFieldsTransient() {

		Field transientField = ReflectionUtils.findField(TestClassComplex.class, "transientField");

		PersistentProperty<?> property = new SamplePersistentProperty(transientField, null, entity, typeHolder);
		assertThat(property.isTransient(), is(false));
	}

	/**
	 * @see DATACMNS-206
	 */
	@Test
	public void findsSimpleGettersAndASetters() {

		Field field = ReflectionUtils.findField(AccessorTestClass.class, "id");
		PersistentProperty<SamplePersistentProperty> property = new SamplePersistentProperty(field, getPropertyDescriptor(
				AccessorTestClass.class, "id"), entity, typeHolder);

		assertThat(property.getGetter(), is(notNullValue()));
		assertThat(property.getSetter(), is(notNullValue()));
	}

	/**
	 * @see DATACMNS-206
	 */
	@Test
	public void doesNotUseInvalidGettersAndASetters() {

		Field field = ReflectionUtils.findField(AccessorTestClass.class, "anotherId");
		PersistentProperty<SamplePersistentProperty> property = new SamplePersistentProperty(field, getPropertyDescriptor(
				AccessorTestClass.class, "anotherId"), entity, typeHolder);

		assertThat(property.getGetter(), is(nullValue()));
		assertThat(property.getSetter(), is(nullValue()));
	}

	/**
	 * @see DATACMNS-206
	 */
	@Test
	public void usesCustomGetter() {

		Field field = ReflectionUtils.findField(AccessorTestClass.class, "yetAnotherId");
		PersistentProperty<SamplePersistentProperty> property = new SamplePersistentProperty(field, getPropertyDescriptor(
				AccessorTestClass.class, "yetAnotherId"), entity, typeHolder);

		assertThat(property.getGetter(), is(notNullValue()));
		assertThat(property.getSetter(), is(nullValue()));
	}

	/**
	 * @see DATACMNS-206
	 */
	@Test
	public void usesCustomSetter() {

		Field field = ReflectionUtils.findField(AccessorTestClass.class, "yetYetAnotherId");
		PersistentProperty<SamplePersistentProperty> property = new SamplePersistentProperty(field, getPropertyDescriptor(
				AccessorTestClass.class, "yetYetAnotherId"), entity, typeHolder);

		assertThat(property.getGetter(), is(nullValue()));
		assertThat(property.getSetter(), is(notNullValue()));
	}

	/**
	 * @see DATACMNS-206
	 */
	@Test
	public void returnsNullGetterAndSetterIfNoPropertyDescriptorGiven() {

		Field field = ReflectionUtils.findField(AccessorTestClass.class, "id");
		PersistentProperty<SamplePersistentProperty> property = new SamplePersistentProperty(field, null, entity,
				typeHolder);

		assertThat(property.getGetter(), is(nullValue()));
		assertThat(property.getSetter(), is(nullValue()));
	}

	private static PropertyDescriptor getPropertyDescriptor(Class<?> type, String propertyName) {

		try {

			BeanInfo info = Introspector.getBeanInfo(type);

			for (PropertyDescriptor descriptor : info.getPropertyDescriptors()) {
				if (descriptor.getName().equals(propertyName)) {
					return descriptor;
				}
			}

			return null;

		} catch (IntrospectionException e) {
			return null;
		}
	}

	class Generic<T> {
		T genericField;

	}

	class FirstConcrete extends Generic<String> {

	}

	class SecondConcrete extends Generic<Integer> {

	}

	@SuppressWarnings("serial")
	class TestClassSet extends TreeSet<Object> {
	}

	@SuppressWarnings("rawtypes")
	class TestClassComplex {

		String id;
		TestClassSet testClassSet;
		Map map;
		Collection collection;
		transient Object transientField;
	}

	class AccessorTestClass {

		// Valid getters and setters
		Long id;
		// Invalid getters and setters
		Long anotherId;

		// Customized getter
		Number yetAnotherId;

		// Customized setter
		Number yetYetAnotherId;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getAnotherId() {
			return anotherId.toString();
		}

		public void setAnotherId(String anotherId) {
			this.anotherId = Long.parseLong(anotherId);
		}

		public Long getYetAnotherId() {
			return null;
		}

		public void setYetYetAnotherId(Object yetYetAnotherId) {
			this.yetYetAnotherId = null;
		}
	}

	class SamplePersistentProperty extends AbstractPersistentProperty<SamplePersistentProperty> {

		public SamplePersistentProperty(Field field, PropertyDescriptor propertyDescriptor,
				PersistentEntity<?, SamplePersistentProperty> owner, SimpleTypeHolder simpleTypeHolder) {
			super(field, propertyDescriptor, owner, simpleTypeHolder);
		}

		public boolean isIdProperty() {
			return false;
		}

		@Override
		protected Association<SamplePersistentProperty> createAssociation() {
			return null;
		}
	}
}
