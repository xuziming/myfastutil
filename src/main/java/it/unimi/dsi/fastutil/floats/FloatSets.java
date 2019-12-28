/*
	* Copyright (C) 2002-2019 Sebastiano Vigna
	*
	* Licensed under the Apache License, Version 2.0 (the "License");
	* you may not use this file except in compliance with the License.
	* You may obtain a copy of the License at
	*
	*     http://www.apache.org/licenses/LICENSE-2.0
	*
	* Unless required by applicable law or agreed to in writing, software
	* distributed under the License is distributed on an "AS IS" BASIS,
	* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	* See the License for the specific language governing permissions and
	* limitations under the License.
	*/
package it.unimi.dsi.fastutil.floats;

import java.util.Collection;
import java.util.Set;

/**
 * A class providing static methods and objects that do useful things with
 * type-specific sets.
 *
 * @see java.util.Collections
 */
public final class FloatSets {
	private FloatSets() {
	}

	/**
	 * An immutable class representing the empty set and implementing a
	 * type-specific set interface.
	 *
	 * <p>
	 * This class may be useful to implement your own in case you subclass a
	 * type-specific set.
	 */
	public static class EmptySet extends FloatCollections.EmptyCollection
			implements FloatSet, java.io.Serializable, Cloneable {
		private static final long serialVersionUID = -7046029254386353129L;

		protected EmptySet() {
		}

		@Override
		public boolean remove(float ok) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object clone() {
			return EMPTY_SET;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public boolean equals(final Object o) {
			return o instanceof Set && ((Set) o).isEmpty();
		}

		@Deprecated
		@Override
		public boolean rem(final float k) {
			return super.rem(k);
		}

		private Object readResolve() {
			return EMPTY_SET;
		}
	}

	/**
	 * An empty set (immutable). It is serializable and cloneable.
	 */

	public static final EmptySet EMPTY_SET = new EmptySet();

	/**
	 * An immutable class representing a type-specific singleton set.
	 *
	 * <p>
	 * This class may be useful to implement your own in case you subclass a
	 * type-specific set.
	 */
	public static class Singleton extends AbstractFloatSet implements java.io.Serializable, Cloneable {
		private static final long serialVersionUID = -7046029254386353129L;
		protected final float element;

		protected Singleton(final float element) {
			this.element = element;
		}

		@Override
		public boolean contains(final float k) {
			return (Float.floatToIntBits(k) == Float.floatToIntBits(element));
		}

		@Override
		public boolean remove(final float k) {
			throw new UnsupportedOperationException();
		}

		@Override
		public FloatListIterator iterator() {
			return FloatIterators.singleton(element);
		}

		@Override
		public int size() {
			return 1;
		}

		@Override
		public boolean addAll(final Collection<? extends Float> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(final Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(final Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(final FloatCollection c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(final FloatCollection c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(final FloatCollection c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object clone() {
			return this;
		}
	}

	/**
	 * Returns a type-specific immutable set containing only the specified element.
	 * The returned set is serializable and cloneable.
	 *
	 * @param element the only element of the returned set.
	 * @return a type-specific immutable set containing just {@code element}.
	 */
	public static FloatSet singleton(final float element) {
		return new Singleton(element);
	}

	/**
	 * Returns a type-specific immutable set containing only the specified element.
	 * The returned set is serializable and cloneable.
	 *
	 * @param element the only element of the returned set.
	 * @return a type-specific immutable set containing just {@code element}.
	 */
	public static FloatSet singleton(final Float element) {
		return new Singleton((element).floatValue());
	}

	/** A synchronized wrapper class for sets. */
	public static class SynchronizedSet extends FloatCollections.SynchronizedCollection
			implements FloatSet, java.io.Serializable {
		private static final long serialVersionUID = -7046029254386353129L;

		protected SynchronizedSet(final FloatSet s, final Object sync) {
			super(s, sync);
		}

		protected SynchronizedSet(final FloatSet s) {
			super(s);
		}

		@Override
		public boolean remove(final float k) {
			synchronized (sync) {
				return collection.rem(k);
			}
		}

		@Deprecated
		@Override
		public boolean rem(final float k) {
			return super.rem(k);
		}
	}

	/**
	 * Returns a synchronized type-specific set backed by the given type-specific
	 * set.
	 *
	 * @param s the set to be wrapped in a synchronized set.
	 * @return a synchronized view of the specified set.
	 * @see java.util.Collections#synchronizedSet(Set)
	 */
	public static FloatSet synchronize(final FloatSet s) {
		return new SynchronizedSet(s);
	}

	/**
	 * Returns a synchronized type-specific set backed by the given type-specific
	 * set, using an assigned object to synchronize.
	 *
	 * @param s    the set to be wrapped in a synchronized set.
	 * @param sync an object that will be used to synchronize the access to the set.
	 * @return a synchronized view of the specified set.
	 * @see java.util.Collections#synchronizedSet(Set)
	 */
	public static FloatSet synchronize(final FloatSet s, final Object sync) {
		return new SynchronizedSet(s, sync);
	}

	/** An unmodifiable wrapper class for sets. */
	public static class UnmodifiableSet extends FloatCollections.UnmodifiableCollection
			implements FloatSet, java.io.Serializable {
		private static final long serialVersionUID = -7046029254386353129L;

		protected UnmodifiableSet(final FloatSet s) {
			super(s);
		}

		@Override
		public boolean remove(final float k) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean equals(final Object o) {
			if (o == this)
				return true;
			return collection.equals(o);
		}

		@Override
		public int hashCode() {
			return collection.hashCode();
		}

		@Deprecated
		@Override
		public boolean rem(final float k) {
			return super.rem(k);
		}
	}

	/**
	 * Returns an unmodifiable type-specific set backed by the given type-specific
	 * set.
	 *
	 * @param s the set to be wrapped in an unmodifiable set.
	 * @return an unmodifiable view of the specified set.
	 * @see java.util.Collections#unmodifiableSet(Set)
	 */
	public static FloatSet unmodifiable(final FloatSet s) {
		return new UnmodifiableSet(s);
	}
}
