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
package it.unimi.dsi.fastutil.longs;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.HashCommon;
import static it.unimi.dsi.fastutil.HashCommon.arraySize;
import static it.unimi.dsi.fastutil.HashCommon.maxFill;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A type-specific hash set with a fast, small-footprint implementation whose
 * {@linkplain it.unimi.dsi.fastutil.Hash.Strategy hashing strategy} is
 * specified at creation time.
 *
 * <p>
 * Instances of this class use a hash table to represent a set. The table is
 * filled up to a specified <em>load factor</em>, and then doubled in size to
 * accommodate new entries. If the table is emptied below <em>one fourth</em> of
 * the load factor, it is halved in size; however, the table is never reduced to
 * a size smaller than that at creation time: this approach makes it possible to
 * create sets with a large capacity in which insertions and deletions do not
 * cause immediately rehashing. Moreover, halving is not performed when deleting
 * entries from an iterator, as it would interfere with the iteration process.
 *
 * <p>
 * Note that {@link #clear()} does not modify the hash table size. Rather, a
 * family of {@linkplain #trim() trimming methods} lets you control the size of
 * the table; this is particularly useful if you reuse instances of this class.
 *
 * @see Hash
 * @see HashCommon
 */
public class LongOpenCustomHashSet extends AbstractLongSet implements java.io.Serializable, Cloneable, Hash {
	private static final long serialVersionUID = 0L;
	private static final boolean ASSERTS = false;
	/** The array of keys. */
	protected transient long[] key;
	/** The mask for wrapping a position counter. */
	protected transient int mask;
	/** Whether this set contains the null key. */
	protected transient boolean containsNull;
	/** The hash strategy of this custom set. */
	protected it.unimi.dsi.fastutil.longs.LongHash.Strategy strategy;
	/**
	 * The current table size. Note that an additional element is allocated for
	 * storing the null key.
	 */
	protected transient int n;
	/**
	 * Threshold after which we rehash. It must be the table size times {@link #f}.
	 */
	protected transient int maxFill;
	/**
	 * We never resize below this threshold, which is the construction-time {#n}.
	 */
	protected final transient int minN;
	/** Number of entries in the set (including the null key, if present). */
	protected int size;
	/** The acceptable load factor. */
	protected final float f;

	/**
	 * Creates a new hash set.
	 *
	 * <p>
	 * The actual table size will be the least power of two greater than
	 * {@code expected}/{@code f}.
	 *
	 * @param expected the expected number of elements in the hash set.
	 * @param f        the load factor.
	 * @param strategy the strategy.
	 */

	public LongOpenCustomHashSet(final int expected, final float f,
			final it.unimi.dsi.fastutil.longs.LongHash.Strategy strategy) {
		this.strategy = strategy;
		if (f <= 0 || f > 1)
			throw new IllegalArgumentException("Load factor must be greater than 0 and smaller than or equal to 1");
		if (expected < 0)
			throw new IllegalArgumentException("The expected number of elements must be nonnegative");
		this.f = f;
		minN = n = arraySize(expected, f);
		mask = n - 1;
		maxFill = maxFill(n, f);
		key = new long[n + 1];
	}

	/**
	 * Creates a new hash set with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor.
	 *
	 * @param expected the expected number of elements in the hash set.
	 * @param strategy the strategy.
	 */
	public LongOpenCustomHashSet(final int expected, final it.unimi.dsi.fastutil.longs.LongHash.Strategy strategy) {
		this(expected, DEFAULT_LOAD_FACTOR, strategy);
	}

	/**
	 * Creates a new hash set with initial expected
	 * {@link Hash#DEFAULT_INITIAL_SIZE} elements and
	 * {@link Hash#DEFAULT_LOAD_FACTOR} as load factor.
	 * 
	 * @param strategy the strategy.
	 */
	public LongOpenCustomHashSet(final it.unimi.dsi.fastutil.longs.LongHash.Strategy strategy) {
		this(DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR, strategy);
	}

	/**
	 * Creates a new hash set copying a given collection.
	 *
	 * @param c        a {@link Collection} to be copied into the new hash set.
	 * @param f        the load factor.
	 * @param strategy the strategy.
	 */
	public LongOpenCustomHashSet(final Collection<? extends Long> c, final float f,
			final it.unimi.dsi.fastutil.longs.LongHash.Strategy strategy) {
		this(c.size(), f, strategy);
		addAll(c);
	}

	/**
	 * Creates a new hash set with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor
	 * copying a given collection.
	 *
	 * @param c        a {@link Collection} to be copied into the new hash set.
	 * @param strategy the strategy.
	 */
	public LongOpenCustomHashSet(final Collection<? extends Long> c,
			final it.unimi.dsi.fastutil.longs.LongHash.Strategy strategy) {
		this(c, DEFAULT_LOAD_FACTOR, strategy);
	}

	/**
	 * Creates a new hash set copying a given type-specific collection.
	 *
	 * @param c        a type-specific collection to be copied into the new hash
	 *                 set.
	 * @param f        the load factor.
	 * @param strategy the strategy.
	 */
	public LongOpenCustomHashSet(final LongCollection c, final float f,
			it.unimi.dsi.fastutil.longs.LongHash.Strategy strategy) {
		this(c.size(), f, strategy);
		addAll(c);
	}

	/**
	 * Creates a new hash set with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor
	 * copying a given type-specific collection.
	 *
	 * @param c        a type-specific collection to be copied into the new hash
	 *                 set.
	 * @param strategy the strategy.
	 */
	public LongOpenCustomHashSet(final LongCollection c, final it.unimi.dsi.fastutil.longs.LongHash.Strategy strategy) {
		this(c, DEFAULT_LOAD_FACTOR, strategy);
	}

	/**
	 * Creates a new hash set using elements provided by a type-specific iterator.
	 *
	 * @param i        a type-specific iterator whose elements will fill the set.
	 * @param f        the load factor.
	 * @param strategy the strategy.
	 */
	public LongOpenCustomHashSet(final LongIterator i, final float f,
			final it.unimi.dsi.fastutil.longs.LongHash.Strategy strategy) {
		this(DEFAULT_INITIAL_SIZE, f, strategy);
		while (i.hasNext())
			add(i.nextLong());
	}

	/**
	 * Creates a new hash set with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor
	 * using elements provided by a type-specific iterator.
	 *
	 * @param i        a type-specific iterator whose elements will fill the set.
	 * @param strategy the strategy.
	 */
	public LongOpenCustomHashSet(final LongIterator i, final it.unimi.dsi.fastutil.longs.LongHash.Strategy strategy) {
		this(i, DEFAULT_LOAD_FACTOR, strategy);
	}

	/**
	 * Creates a new hash set using elements provided by an iterator.
	 *
	 * @param i        an iterator whose elements will fill the set.
	 * @param f        the load factor.
	 * @param strategy the strategy.
	 */
	public LongOpenCustomHashSet(final Iterator<?> i, final float f,
			final it.unimi.dsi.fastutil.longs.LongHash.Strategy strategy) {
		this(LongIterators.asLongIterator(i), f, strategy);
	}

	/**
	 * Creates a new hash set with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor
	 * using elements provided by an iterator.
	 *
	 * @param i        an iterator whose elements will fill the set.
	 * @param strategy the strategy.
	 */
	public LongOpenCustomHashSet(final Iterator<?> i, final it.unimi.dsi.fastutil.longs.LongHash.Strategy strategy) {
		this(LongIterators.asLongIterator(i), strategy);
	}

	/**
	 * Creates a new hash set and fills it with the elements of a given array.
	 *
	 * @param a        an array whose elements will be used to fill the set.
	 * @param offset   the first element to use.
	 * @param length   the number of elements to use.
	 * @param f        the load factor.
	 * @param strategy the strategy.
	 */
	public LongOpenCustomHashSet(final long[] a, final int offset, final int length, final float f,
			final it.unimi.dsi.fastutil.longs.LongHash.Strategy strategy) {
		this(length < 0 ? 0 : length, f, strategy);
		LongArrays.ensureOffsetLength(a, offset, length);
		for (int i = 0; i < length; i++)
			add(a[offset + i]);
	}

	/**
	 * Creates a new hash set with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor
	 * and fills it with the elements of a given array.
	 *
	 * @param a        an array whose elements will be used to fill the set.
	 * @param offset   the first element to use.
	 * @param length   the number of elements to use.
	 * @param strategy the strategy.
	 */
	public LongOpenCustomHashSet(final long[] a, final int offset, final int length,
			final it.unimi.dsi.fastutil.longs.LongHash.Strategy strategy) {
		this(a, offset, length, DEFAULT_LOAD_FACTOR, strategy);
	}

	/**
	 * Creates a new hash set copying the elements of an array.
	 *
	 * @param a        an array to be copied into the new hash set.
	 * @param f        the load factor.
	 * @param strategy the strategy.
	 */
	public LongOpenCustomHashSet(final long[] a, final float f,
			final it.unimi.dsi.fastutil.longs.LongHash.Strategy strategy) {
		this(a, 0, a.length, f, strategy);
	}

	/**
	 * Creates a new hash set with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor
	 * copying the elements of an array.
	 *
	 * @param a        an array to be copied into the new hash set.
	 * @param strategy the strategy.
	 */
	public LongOpenCustomHashSet(final long[] a, final it.unimi.dsi.fastutil.longs.LongHash.Strategy strategy) {
		this(a, DEFAULT_LOAD_FACTOR, strategy);
	}

	/**
	 * Returns the hashing strategy.
	 *
	 * @return the hashing strategy of this custom hash set.
	 */
	public it.unimi.dsi.fastutil.longs.LongHash.Strategy strategy() {
		return strategy;
	}

	private int realSize() {
		return containsNull ? size - 1 : size;
	}

	private void ensureCapacity(final int capacity) {
		final int needed = arraySize(capacity, f);
		if (needed > n)
			rehash(needed);
	}

	private void tryCapacity(final long capacity) {
		final int needed = (int) Math.min(1 << 30,
				Math.max(2, HashCommon.nextPowerOfTwo((long) Math.ceil(capacity / f))));
		if (needed > n)
			rehash(needed);
	}

	@Override
	public boolean addAll(LongCollection c) {
		if (f <= .5)
			ensureCapacity(c.size()); // The resulting collection will be sized for c.size() elements
		else
			tryCapacity(size() + c.size()); // The resulting collection will be tentatively sized for size() + c.size()
											// elements
		return super.addAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends Long> c) {
		// The resulting collection will be at least c.size() big
		if (f <= .5)
			ensureCapacity(c.size()); // The resulting collection will be sized for c.size() elements
		else
			tryCapacity(size() + c.size()); // The resulting collection will be tentatively sized for size() + c.size()
											// elements
		return super.addAll(c);
	}

	@Override
	public boolean add(final long k) {
		int pos;
		if ((strategy.equals((k), (0)))) {
			if (containsNull)
				return false;
			containsNull = true;
			key[n] = k;
		} else {
			long curr;
			final long[] key = this.key;
			// The starting point.
			if (!((curr = key[pos = (it.unimi.dsi.fastutil.HashCommon.mix(strategy.hashCode(k))) & mask]) == (0))) {
				if ((strategy.equals((curr), (k))))
					return false;
				while (!((curr = key[pos = (pos + 1) & mask]) == (0)))
					if ((strategy.equals((curr), (k))))
						return false;
			}
			key[pos] = k;
		}
		if (size++ >= maxFill)
			rehash(arraySize(size + 1, f));
		if (ASSERTS)
			checkTable();
		return true;
	}

	/**
	 * Shifts left entries with the specified hash code, starting at the specified
	 * position, and empties the resulting free entry.
	 *
	 * @param pos a starting position.
	 */
	protected final void shiftKeys(int pos) {
		// Shift entries with the same hash.
		int last, slot;
		long curr;
		final long[] key = this.key;
		for (;;) {
			pos = ((last = pos) + 1) & mask;
			for (;;) {
				if (((curr = key[pos]) == (0))) {
					key[last] = (0);
					return;
				}
				slot = (it.unimi.dsi.fastutil.HashCommon.mix(strategy.hashCode(curr))) & mask;
				if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos)
					break;
				pos = (pos + 1) & mask;
			}
			key[last] = curr;
		}
	}

	private boolean removeEntry(final int pos) {
		size--;
		shiftKeys(pos);
		if (n > minN && size < maxFill / 4 && n > DEFAULT_INITIAL_SIZE)
			rehash(n / 2);
		return true;
	}

	private boolean removeNullEntry() {
		containsNull = false;
		key[n] = (0);
		size--;
		if (n > minN && size < maxFill / 4 && n > DEFAULT_INITIAL_SIZE)
			rehash(n / 2);
		return true;
	}

	@Override
	public boolean remove(final long k) {
		if ((strategy.equals((k), (0)))) {
			if (containsNull)
				return removeNullEntry();
			return false;
		}
		long curr;
		final long[] key = this.key;
		int pos;
		// The starting point.
		if (((curr = key[pos = (it.unimi.dsi.fastutil.HashCommon.mix(strategy.hashCode(k))) & mask]) == (0)))
			return false;
		if ((strategy.equals((k), (curr))))
			return removeEntry(pos);
		while (true) {
			if (((curr = key[pos = (pos + 1) & mask]) == (0)))
				return false;
			if ((strategy.equals((k), (curr))))
				return removeEntry(pos);
		}
	}

	@Override
	public boolean contains(final long k) {
		if ((strategy.equals((k), (0))))
			return containsNull;
		long curr;
		final long[] key = this.key;
		int pos;
		// The starting point.
		if (((curr = key[pos = (it.unimi.dsi.fastutil.HashCommon.mix(strategy.hashCode(k))) & mask]) == (0)))
			return false;
		if ((strategy.equals((k), (curr))))
			return true;
		while (true) {
			if (((curr = key[pos = (pos + 1) & mask]) == (0)))
				return false;
			if ((strategy.equals((k), (curr))))
				return true;
		}
	}

	/*
	 * Removes all elements from this set.
	 *
	 * <p>To increase object reuse, this method does not change the table size. If
	 * you want to reduce the table size, you must use {@link #trim()}.
	 *
	 */
	@Override
	public void clear() {
		if (size == 0)
			return;
		size = 0;
		containsNull = false;
		Arrays.fill(key, (0));
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	/** An iterator over a hash set. */
	private class SetIterator implements LongIterator {
		/**
		 * The index of the last entry returned, if positive or zero; initially,
		 * {@link #n}. If negative, the last element returned was that of index
		 * {@code - pos - 1} from the {@link #wrapped} list.
		 */
		int pos = n;
		/**
		 * The index of the last entry that has been returned (more precisely, the value
		 * of {@link #pos} if {@link #pos} is positive, or {@link Integer#MIN_VALUE} if
		 * {@link #pos} is negative). It is -1 if either we did not return an entry yet,
		 * or the last returned entry has been removed.
		 */
		int last = -1;
		/** A downward counter measuring how many entries must still be returned. */
		int c = size;
		/** A boolean telling us whether we should return the null key. */
		boolean mustReturnNull = LongOpenCustomHashSet.this.containsNull;
		/**
		 * A lazily allocated list containing elements that have wrapped around the
		 * table because of removals.
		 */
		LongArrayList wrapped;

		@Override
		public boolean hasNext() {
			return c != 0;
		}

		@Override
		public long nextLong() {
			if (!hasNext())
				throw new NoSuchElementException();
			c--;
			if (mustReturnNull) {
				mustReturnNull = false;
				last = n;
				return key[n];
			}
			final long key[] = LongOpenCustomHashSet.this.key;
			for (;;) {
				if (--pos < 0) {
					// We are just enumerating elements from the wrapped list.
					last = Integer.MIN_VALUE;
					return wrapped.getLong(-pos - 1);
				}
				if (!((key[pos]) == (0)))
					return key[last = pos];
			}
		}

		/**
		 * Shifts left entries with the specified hash code, starting at the specified
		 * position, and empties the resulting free entry.
		 *
		 * @param pos a starting position.
		 */
		private final void shiftKeys(int pos) {
			// Shift entries with the same hash.
			int last, slot;
			long curr;
			final long[] key = LongOpenCustomHashSet.this.key;
			for (;;) {
				pos = ((last = pos) + 1) & mask;
				for (;;) {
					if (((curr = key[pos]) == (0))) {
						key[last] = (0);
						return;
					}
					slot = (it.unimi.dsi.fastutil.HashCommon.mix(strategy.hashCode(curr))) & mask;
					if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos)
						break;
					pos = (pos + 1) & mask;
				}
				if (pos < last) { // Wrapped entry.
					if (wrapped == null)
						wrapped = new LongArrayList(2);
					wrapped.add(key[pos]);
				}
				key[last] = curr;
			}
		}

		@Override
		public void remove() {
			if (last == -1)
				throw new IllegalStateException();
			if (last == n) {
				LongOpenCustomHashSet.this.containsNull = false;
				LongOpenCustomHashSet.this.key[n] = (0);
			} else if (pos >= 0)
				shiftKeys(last);
			else {
				// We're removing wrapped entries.
				LongOpenCustomHashSet.this.remove(wrapped.getLong(-pos - 1));
				last = -1; // Note that we must not decrement size
				return;
			}
			size--;
			last = -1; // You can no longer remove this entry.
			if (ASSERTS)
				checkTable();
		}
	}

	@Override
	public LongIterator iterator() {
		return new SetIterator();
	}

	/**
	 * Rehashes this set, making the table as small as possible.
	 *
	 * <p>
	 * This method rehashes the table to the smallest size satisfying the load
	 * factor. It can be used when the set will not be changed anymore, so to
	 * optimize access speed and size.
	 *
	 * <p>
	 * If the table size is already the minimum possible, this method does nothing.
	 *
	 * @return true if there was enough memory to trim the set.
	 * @see #trim(int)
	 */
	public boolean trim() {
		return trim(size);
	}

	/**
	 * Rehashes this set if the table is too large.
	 *
	 * <p>
	 * Let <var>N</var> be the smallest table size that can hold
	 * <code>max(n,{@link #size()})</code> entries, still satisfying the load
	 * factor. If the current table size is smaller than or equal to <var>N</var>,
	 * this method does nothing. Otherwise, it rehashes this set in a table of size
	 * <var>N</var>.
	 *
	 * <p>
	 * This method is useful when reusing sets. {@linkplain #clear() Clearing a set}
	 * leaves the table size untouched. If you are reusing a set many times, you can
	 * call this method with a typical size to avoid keeping around a very large
	 * table just because of a few large transient sets.
	 *
	 * @param n the threshold for the trimming.
	 * @return true if there was enough memory to trim the set.
	 * @see #trim()
	 */
	public boolean trim(final int n) {
		final int l = HashCommon.nextPowerOfTwo((int) Math.ceil(n / f));
		if (l >= this.n || size > maxFill(l, f))
			return true;
		try {
			rehash(l);
		} catch (OutOfMemoryError cantDoIt) {
			return false;
		}
		return true;
	}

	/**
	 * Rehashes the set.
	 *
	 * <p>
	 * This method implements the basic rehashing strategy, and may be overriden by
	 * subclasses implementing different rehashing strategies (e.g., disk-based
	 * rehashing). However, you should not override this method unless you
	 * understand the internal workings of this class.
	 *
	 * @param newN the new size
	 */

	protected void rehash(final int newN) {
		final long key[] = this.key;
		final int mask = newN - 1; // Note that this is used by the hashing macro
		final long newKey[] = new long[newN + 1];
		int i = n, pos;
		for (int j = realSize(); j-- != 0;) {
			while (((key[--i]) == (0)))
				;
			if (!((newKey[pos = (it.unimi.dsi.fastutil.HashCommon.mix(strategy.hashCode(key[i]))) & mask]) == (0)))
				while (!((newKey[pos = (pos + 1) & mask]) == (0)))
					;
			newKey[pos] = key[i];
		}
		n = newN;
		this.mask = mask;
		maxFill = maxFill(n, f);
		this.key = newKey;
	}

	/**
	 * Returns a deep copy of this set.
	 *
	 * <p>
	 * This method performs a deep copy of this hash set; the data stored in the
	 * set, however, is not cloned. Note that this makes a difference only for
	 * object keys.
	 *
	 * @return a deep copy of this set.
	 */
	@Override

	public LongOpenCustomHashSet clone() {
		LongOpenCustomHashSet c;
		try {
			c = (LongOpenCustomHashSet) super.clone();
		} catch (CloneNotSupportedException cantHappen) {
			throw new InternalError();
		}
		c.key = key.clone();
		c.containsNull = containsNull;
		c.strategy = strategy;
		return c;
	}

	/**
	 * Returns a hash code for this set.
	 *
	 * This method overrides the generic method provided by the superclass. Since
	 * {@code equals()} is not overriden, it is important that the value returned by
	 * this method is the same value as the one returned by the overriden method.
	 *
	 * @return a hash code for this set.
	 */
	@Override
	public int hashCode() {
		int h = 0;
		for (int j = realSize(), i = 0; j-- != 0;) {
			while (((key[i]) == (0)))
				i++;
			h += (strategy.hashCode(key[i]));
			i++;
		}
		// Zero / null have hash zero.
		return h;
	}

	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
		final LongIterator i = iterator();
		s.defaultWriteObject();
		for (int j = size; j-- != 0;)
			s.writeLong(i.nextLong());
	}

	private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
		s.defaultReadObject();
		n = arraySize(size, f);
		maxFill = maxFill(n, f);
		mask = n - 1;
		final long key[] = this.key = new long[n + 1];
		long k;
		for (int i = size, pos; i-- != 0;) {
			k = s.readLong();
			if ((strategy.equals((k), (0)))) {
				pos = n;
				containsNull = true;
			} else {
				if (!((key[pos = (it.unimi.dsi.fastutil.HashCommon.mix(strategy.hashCode(k))) & mask]) == (0)))
					while (!((key[pos = (pos + 1) & mask]) == (0)))
						;
			}
			key[pos] = k;
		}
		if (ASSERTS)
			checkTable();
	}

	private void checkTable() {
	}
}
