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
package it.unimi.dsi.fastutil.doubles;

import it.unimi.dsi.fastutil.Function;

/**
 * A class providing static methods and objects that do useful things with
 * type-specific functions.
 *
 * @see it.unimi.dsi.fastutil.Function
 * @see java.util.Collections
 */
public final class Double2ObjectFunctions {
	private Double2ObjectFunctions() {
	}

	/**
	 * An immutable class representing an empty type-specific function.
	 *
	 * <p>
	 * This class may be useful to implement your own in case you subclass a
	 * type-specific function.
	 */
	public static class EmptyFunction<V> extends AbstractDouble2ObjectFunction<V>
			implements java.io.Serializable, Cloneable {
		private static final long serialVersionUID = -7046029254386353129L;

		protected EmptyFunction() {
		}

		@Override
		public V get(final double k) {
			return (null);
		}

		@Override
		public boolean containsKey(final double k) {
			return false;
		}

		@Override
		public V defaultReturnValue() {
			return (null);
		}

		@Override
		public void defaultReturnValue(final V defRetValue) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public void clear() {
		}

		@Override
		public Object clone() {
			return EMPTY_FUNCTION;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public boolean equals(final Object o) {
			if (!(o instanceof Function))
				return false;
			return ((Function<?, ?>) o).size() == 0;
		}

		@Override
		public String toString() {
			return "{}";
		}

		private Object readResolve() {
			return EMPTY_FUNCTION;
		}
	}

	/**
	 * An empty type-specific function (immutable). It is serializable and
	 * cloneable.
	 */
	@SuppressWarnings("rawtypes")
	public static final EmptyFunction EMPTY_FUNCTION = new EmptyFunction();

	/**
	 * An immutable class representing a type-specific singleton function. Note that
	 * the default return value is still settable.
	 *
	 * <p>
	 * Note that albeit the function is immutable, its default return value may be
	 * changed.
	 *
	 * <p>
	 * This class may be useful to implement your own in case you subclass a
	 * type-specific function.
	 */
	public static class Singleton<V> extends AbstractDouble2ObjectFunction<V>
			implements java.io.Serializable, Cloneable {
		private static final long serialVersionUID = -7046029254386353129L;
		protected final double key;
		protected final V value;

		protected Singleton(final double key, final V value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public boolean containsKey(final double k) {
			return (Double.doubleToLongBits(key) == Double.doubleToLongBits(k));
		}

		@Override
		public V get(final double k) {
			return (Double.doubleToLongBits(key) == Double.doubleToLongBits(k)) ? value : defRetValue;
		}

		@Override
		public int size() {
			return 1;
		}

		@Override
		public Object clone() {
			return this;
		}
	}

	/**
	 * Returns a type-specific immutable function containing only the specified
	 * pair. The returned function is serializable and cloneable.
	 *
	 * <p>
	 * Note that albeit the returned function is immutable, its default return value
	 * may be changed.
	 *
	 * @param key   the only key of the returned function.
	 * @param value the only value of the returned function.
	 * @return a type-specific immutable function containing just the pair
	 *         {@code &lt;key,value&gt;}.
	 */
	public static <V> Double2ObjectFunction<V> singleton(final double key, V value) {
		return new Singleton<>(key, value);
	}

	/**
	 * Returns a type-specific immutable function containing only the specified
	 * pair. The returned function is serializable and cloneable.
	 *
	 * <p>
	 * Note that albeit the returned function is immutable, its default return value
	 * may be changed.
	 *
	 * @param key   the only key of the returned function.
	 * @param value the only value of the returned function.
	 * @return a type-specific immutable function containing just the pair
	 *         {@code &lt;key,value&gt;}.
	 */
	public static <V> Double2ObjectFunction<V> singleton(final Double key, final V value) {
		return new Singleton<>((key).doubleValue(), (value));
	}

	/** A synchronized wrapper class for functions. */
	public static class SynchronizedFunction<V> implements Double2ObjectFunction<V>, java.io.Serializable {
		private static final long serialVersionUID = -7046029254386353129L;
		protected final Double2ObjectFunction<V> function;
		protected final Object sync;

		protected SynchronizedFunction(final Double2ObjectFunction<V> f, final Object sync) {
			if (f == null)
				throw new NullPointerException();
			this.function = f;
			this.sync = sync;
		}

		protected SynchronizedFunction(final Double2ObjectFunction<V> f) {
			if (f == null)
				throw new NullPointerException();
			this.function = f;
			this.sync = this;
		}

		@Override
		public V apply(double operand) {
			synchronized (sync) {
				return function.apply(operand);
			}
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public V apply(final Double key) {
			synchronized (sync) {
				return function.apply(key);
			}
		}

		@Override
		public int size() {
			synchronized (sync) {
				return function.size();
			}
		}

		@Override
		public V defaultReturnValue() {
			synchronized (sync) {
				return function.defaultReturnValue();
			}
		}

		@Override
		public void defaultReturnValue(final V defRetValue) {
			synchronized (sync) {
				function.defaultReturnValue(defRetValue);
			}
		}

		@Override
		public boolean containsKey(final double k) {
			synchronized (sync) {
				return function.containsKey(k);
			}
		}

		@Deprecated
		@Override
		public boolean containsKey(final Object k) {
			synchronized (sync) {
				return function.containsKey(k);
			}
		}

		@Override
		public V put(final double k, final V v) {
			synchronized (sync) {
				return function.put(k, v);
			}
		}

		@Override
		public V get(final double k) {
			synchronized (sync) {
				return function.get(k);
			}
		}

		@Override
		public V remove(final double k) {
			synchronized (sync) {
				return function.remove(k);
			}
		}

		@Override
		public void clear() {
			synchronized (sync) {
				function.clear();
			}
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public V put(final Double k, final V v) {
			synchronized (sync) {
				return function.put(k, v);
			}
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public V get(final Object k) {
			synchronized (sync) {
				return function.get(k);
			}
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public V remove(final Object k) {
			synchronized (sync) {
				return function.remove(k);
			}
		}

		@Override
		public int hashCode() {
			synchronized (sync) {
				return function.hashCode();
			}
		}

		@Override
		public boolean equals(final Object o) {
			if (o == this)
				return true;
			synchronized (sync) {
				return function.equals(o);
			}
		}

		@Override
		public String toString() {
			synchronized (sync) {
				return function.toString();
			}
		}

		private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
			synchronized (sync) {
				s.defaultWriteObject();
			}
		}
	}

	/**
	 * Returns a synchronized type-specific function backed by the given
	 * type-specific function.
	 *
	 * @param f the function to be wrapped in a synchronized function.
	 * @return a synchronized view of the specified function.
	 * @see java.util.Collections#synchronizedMap(java.util.Map)
	 */
	public static <V> Double2ObjectFunction<V> synchronize(final Double2ObjectFunction<V> f) {
		return new SynchronizedFunction<>(f);
	}

	/**
	 * Returns a synchronized type-specific function backed by the given
	 * type-specific function, using an assigned object to synchronize.
	 *
	 * @param f    the function to be wrapped in a synchronized function.
	 * @param sync an object that will be used to synchronize the access to the
	 *             function.
	 * @return a synchronized view of the specified function.
	 * @see java.util.Collections#synchronizedMap(java.util.Map)
	 */
	public static <V> Double2ObjectFunction<V> synchronize(final Double2ObjectFunction<V> f, final Object sync) {
		return new SynchronizedFunction<>(f, sync);
	}

	/** An unmodifiable wrapper class for functions. */
	public static class UnmodifiableFunction<V> extends AbstractDouble2ObjectFunction<V>
			implements java.io.Serializable {
		private static final long serialVersionUID = -7046029254386353129L;
		protected final Double2ObjectFunction<V> function;

		protected UnmodifiableFunction(final Double2ObjectFunction<V> f) {
			if (f == null)
				throw new NullPointerException();
			this.function = f;
		}

		@Override
		public int size() {
			return function.size();
		}

		@Override
		public V defaultReturnValue() {
			return function.defaultReturnValue();
		}

		@Override
		public void defaultReturnValue(final V defRetValue) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean containsKey(final double k) {
			return function.containsKey(k);
		}

		@Override
		public V put(final double k, final V v) {
			throw new UnsupportedOperationException();
		}

		@Override
		public V get(final double k) {
			return function.get(k);
		}

		@Override
		public V remove(final double k) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public V put(final Double k, final V v) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public V get(final Object k) {
			return function.get(k);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		public V remove(final Object k) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int hashCode() {
			return function.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			return o == this || function.equals(o);
		}

		@Override
		public String toString() {
			return function.toString();
		}
	}

	/**
	 * Returns an unmodifiable type-specific function backed by the given
	 * type-specific function.
	 *
	 * @param f the function to be wrapped in an unmodifiable function.
	 * @return an unmodifiable view of the specified function.
	 * @see java.util.Collections#unmodifiableMap(java.util.Map)
	 */
	public static <V> Double2ObjectFunction<V> unmodifiable(final Double2ObjectFunction<V> f) {
		return new UnmodifiableFunction<>(f);
	}

	/**
	 * An adapter for mapping generic total functions to partial primitive
	 * functions.
	 */
	public static class PrimitiveFunction<V> implements Double2ObjectFunction<V> {
		protected final java.util.function.Function<? super Double, ? extends V> function;

		protected PrimitiveFunction(java.util.function.Function<? super Double, ? extends V> function) {
			this.function = function;
		}

		@Override
		public boolean containsKey(double key) {
			return function.apply(Double.valueOf(key)) != null;
		}

		@Deprecated
		@Override
		public boolean containsKey(Object key) {
			if (key == null)
				return false;
			return function.apply((Double) (key)) != null;
		}

		@Override
		public V get(double key) {
			V v = function.apply(Double.valueOf(key));
			if (v == null)
				return null;
			return (v);
		}

		@Deprecated
		@Override
		public V get(Object key) {
			if (key == null)
				return null;
			return function.apply((Double) key);
		}

		@Deprecated
		@Override
		public V put(final Double key, final V value) {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Returns a (partial) type-specific function based on the given total generic
	 * function.
	 * <p>
	 * The returned function contains all keys which are not mapped to {@code null}.
	 * If the function already is a primitive function, it is returned without
	 * changes.
	 * <p>
	 * <strong>Warning</strong>: If the given function is a &ldquo;widened&rdquo;
	 * primitive function (e.g. an {@code Int2IntFunction} given to
	 * {@code Short2ShortFunctions}), it still is wrapped into a proxy, decreasing
	 * performance.
	 *
	 * @param f the function to be converted to a type-specific function.
	 * @return a primitive view of the specified function.
	 * @throws NullPointerException if {@code f} is null.
	 * @see PrimitiveFunction
	 * @since 8.1.0
	 */
	@SuppressWarnings("unchecked")
	public static <V> Double2ObjectFunction<V> primitive(
			final java.util.function.Function<? super Double, ? extends V> f) {
		java.util.Objects.requireNonNull(f);
		if (f instanceof Double2ObjectFunction)
			return (Double2ObjectFunction<V>) f;
		if (f instanceof java.util.function.DoubleFunction)
			return ((java.util.function.DoubleFunction<V>) f)::apply;
		return new PrimitiveFunction<>(f);
	}
}
