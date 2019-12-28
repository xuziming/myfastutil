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
package it.unimi.dsi.fastutil.shorts;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.AbstractIntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import java.util.Iterator;
import java.util.Map;

/**
 * An abstract class providing basic methods for maps implementing a
 * type-specific interface.
 *
 * <p>
 * Optional operations just throw an {@link UnsupportedOperationException}.
 * Generic versions of accessors delegate to the corresponding type-specific
 * counterparts following the interface rules (they take care of returning
 * {@code null} on a missing key).
 *
 * <p>
 * As a further help, this class provides a {@link BasicEntry BasicEntry} inner
 * class that implements a type-specific version of {@link java.util.Map.Entry};
 * it is particularly useful for those classes that do not implement their own
 * entries (e.g., most immutable maps).
 */
public abstract class AbstractShort2IntMap extends AbstractShort2IntFunction
		implements Short2IntMap, java.io.Serializable {
	private static final long serialVersionUID = -4940583368468432370L;

	protected AbstractShort2IntMap() {
	}

	@Override
	public boolean containsValue(final int v) {
		return values().contains(v);
	}

	@Override
	public boolean containsKey(final short k) {
		final ObjectIterator<Short2IntMap.Entry> i = short2IntEntrySet().iterator();
		while (i.hasNext())
			if (i.next().getShortKey() == k)
				return true;
		return false;
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * This class provides a basic but complete type-specific entry class for all
	 * those maps implementations that do not have entries on their own (e.g., most
	 * immutable maps).
	 *
	 * <p>
	 * This class does not implement {@link java.util.Map.Entry#setValue(Object)
	 * setValue()}, as the modification would not be reflected in the base map.
	 */
	public static class BasicEntry implements Short2IntMap.Entry {
		protected short key;
		protected int value;

		public BasicEntry() {
		}

		public BasicEntry(final Short key, final Integer value) {
			this.key = (key).shortValue();
			this.value = (value).intValue();
		}

		public BasicEntry(final short key, final int value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public short getShortKey() {
			return key;
		}

		@Override
		public int getIntValue() {
			return value;
		}

		@Override
		public int setValue(final int value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean equals(final Object o) {
			if (!(o instanceof Map.Entry))
				return false;
			if (o instanceof Short2IntMap.Entry) {
				final Short2IntMap.Entry e = (Short2IntMap.Entry) o;
				return ((key) == (e.getShortKey())) && ((value) == (e.getIntValue()));
			}
			final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
			final Object key = e.getKey();
			if (key == null || !(key instanceof Short))
				return false;
			final Object value = e.getValue();
			if (value == null || !(value instanceof Integer))
				return false;
			return ((this.key) == (((Short) (key)).shortValue())) && ((this.value) == (((Integer) (value)).intValue()));
		}

		@Override
		public int hashCode() {
			return (key) ^ (value);
		}

		@Override
		public String toString() {
			return key + "->" + value;
		}
	}

	/**
	 * This class provides a basic implementation for an Entry set which forwards
	 * some queries to the map.
	 */
	public abstract static class BasicEntrySet extends AbstractObjectSet<Entry> {
		protected final Short2IntMap map;

		public BasicEntrySet(final Short2IntMap map) {
			this.map = map;
		}

		@Override
		public boolean contains(final Object o) {
			if (!(o instanceof Map.Entry))
				return false;
			if (o instanceof Short2IntMap.Entry) {
				final Short2IntMap.Entry e = (Short2IntMap.Entry) o;
				final short k = e.getShortKey();
				return map.containsKey(k) && ((map.get(k)) == (e.getIntValue()));
			}
			final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
			final Object key = e.getKey();
			if (key == null || !(key instanceof Short))
				return false;
			final short k = ((Short) (key)).shortValue();
			final Object value = e.getValue();
			if (value == null || !(value instanceof Integer))
				return false;
			return map.containsKey(k) && ((map.get(k)) == (((Integer) (value)).intValue()));
		}

		@Override
		public boolean remove(final Object o) {
			if (!(o instanceof Map.Entry))
				return false;
			if (o instanceof Short2IntMap.Entry) {
				final Short2IntMap.Entry e = (Short2IntMap.Entry) o;
				return map.remove(e.getShortKey(), e.getIntValue());
			}
			Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
			final Object key = e.getKey();
			if (key == null || !(key instanceof Short))
				return false;
			final short k = ((Short) (key)).shortValue();
			final Object value = e.getValue();
			if (value == null || !(value instanceof Integer))
				return false;
			final int v = ((Integer) (value)).intValue();
			return map.remove(k, v);
		}

		@Override
		public int size() {
			return map.size();
		}
	}

	/**
	 * Returns a type-specific-set view of the keys of this map.
	 *
	 * <p>
	 * The view is backed by the set returned by {@link Map#entrySet()}. Note that
	 * <em>no attempt is made at caching the result of this method</em>, as this
	 * would require adding some attributes that lightweight implementations would
	 * not need. Subclasses may easily override this policy by calling this method
	 * and caching the result, but implementors are encouraged to write more
	 * efficient ad-hoc implementations.
	 *
	 * @return a set view of the keys of this map; it may be safely cast to a
	 *         type-specific interface.
	 */
	@Override
	public ShortSet keySet() {
		return new AbstractShortSet() {
			@Override
			public boolean contains(final short k) {
				return containsKey(k);
			}

			@Override
			public int size() {
				return AbstractShort2IntMap.this.size();
			}

			@Override
			public void clear() {
				AbstractShort2IntMap.this.clear();
			}

			@Override
			public ShortIterator iterator() {
				return new ShortIterator() {
					private final ObjectIterator<Short2IntMap.Entry> i = Short2IntMaps
							.fastIterator(AbstractShort2IntMap.this);

					@Override
					public short nextShort() {
						return i.next().getShortKey();
					};

					@Override
					public boolean hasNext() {
						return i.hasNext();
					}

					@Override
					public void remove() {
						i.remove();
					}
				};
			}
		};
	}

	/**
	 * Returns a type-specific-set view of the values of this map.
	 *
	 * <p>
	 * The view is backed by the set returned by {@link Map#entrySet()}. Note that
	 * <em>no attempt is made at caching the result of this method</em>, as this
	 * would require adding some attributes that lightweight implementations would
	 * not need. Subclasses may easily override this policy by calling this method
	 * and caching the result, but implementors are encouraged to write more
	 * efficient ad-hoc implementations.
	 *
	 * @return a set view of the values of this map; it may be safely cast to a
	 *         type-specific interface.
	 */
	@Override
	public IntCollection values() {
		return new AbstractIntCollection() {
			@Override
			public boolean contains(final int k) {
				return containsValue(k);
			}

			@Override
			public int size() {
				return AbstractShort2IntMap.this.size();
			}

			@Override
			public void clear() {
				AbstractShort2IntMap.this.clear();
			}

			@Override
			public IntIterator iterator() {
				return new IntIterator() {
					private final ObjectIterator<Short2IntMap.Entry> i = Short2IntMaps
							.fastIterator(AbstractShort2IntMap.this);

					@Override
					public int nextInt() {
						return i.next().getIntValue();
					}

					@Override
					public boolean hasNext() {
						return i.hasNext();
					}
				};
			}
		};
	}

	/** {@inheritDoc} */
	@SuppressWarnings({ "deprecation" })
	@Override
	public void putAll(final Map<? extends Short, ? extends Integer> m) {
		if (m instanceof Short2IntMap) {
			ObjectIterator<Short2IntMap.Entry> i = Short2IntMaps.fastIterator((Short2IntMap) m);
			while (i.hasNext()) {
				final Short2IntMap.Entry e = i.next();
				put(e.getShortKey(), e.getIntValue());
			}
		} else {
			int n = m.size();
			final Iterator<? extends Map.Entry<? extends Short, ? extends Integer>> i = m.entrySet().iterator();
			Map.Entry<? extends Short, ? extends Integer> e;
			while (n-- != 0) {
				e = i.next();
				put(e.getKey(), e.getValue());
			}
		}
	}

	/**
	 * Returns a hash code for this map.
	 *
	 * The hash code of a map is computed by summing the hash codes of its entries.
	 *
	 * @return a hash code for this map.
	 */
	@Override
	public int hashCode() {
		int h = 0, n = size();
		final ObjectIterator<Short2IntMap.Entry> i = Short2IntMaps.fastIterator(this);
		while (n-- != 0)
			h += i.next().hashCode();
		return h;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Map))
			return false;
		final Map<?, ?> m = (Map<?, ?>) o;
		if (m.size() != size())
			return false;
		return short2IntEntrySet().containsAll(m.entrySet());
	}

	@Override
	public String toString() {
		final StringBuilder s = new StringBuilder();
		final ObjectIterator<Short2IntMap.Entry> i = Short2IntMaps.fastIterator(this);
		int n = size();
		Short2IntMap.Entry e;
		boolean first = true;
		s.append("{");
		while (n-- != 0) {
			if (first)
				first = false;
			else
				s.append(", ");
			e = i.next();
			s.append(String.valueOf(e.getShortKey()));
			s.append("=>");
			s.append(String.valueOf(e.getIntValue()));
		}
		s.append("}");
		return s.toString();
	}
}
