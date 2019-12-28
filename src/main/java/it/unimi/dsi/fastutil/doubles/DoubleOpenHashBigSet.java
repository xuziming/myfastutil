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

import static it.unimi.dsi.fastutil.BigArrays.copy;
import static it.unimi.dsi.fastutil.BigArrays.fill;
import static it.unimi.dsi.fastutil.BigArrays.set;
import it.unimi.dsi.fastutil.BigArrays;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.Size64;
import it.unimi.dsi.fastutil.HashCommon;
import static it.unimi.dsi.fastutil.HashCommon.bigArraySize;
import static it.unimi.dsi.fastutil.HashCommon.maxFill;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A type-specific hash big set with with a fast, small-footprint
 * implementation.
 *
 * <p>
 * Instances of this class use a hash table to represent a big set: the number
 * of elements in the set is limited only by the amount of core memory. The
 * table (backed by a {@linkplain it.unimi.dsi.fastutil.BigArrays big array}) is
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
 * <p>
 * The methods of this class are about 30% slower than those of the
 * corresponding non-big set.
 *
 * @see Hash
 * @see HashCommon
 */
public class DoubleOpenHashBigSet extends AbstractDoubleSet implements java.io.Serializable, Cloneable, Hash, Size64 {
	private static final long serialVersionUID = 0L;
	private static final boolean ASSERTS = false;
	/** The big array of keys. */
	protected transient double[][] key;
	/** The mask for wrapping a position counter. */
	protected transient long mask;
	/** The mask for wrapping a segment counter. */
	protected transient int segmentMask;
	/** The mask for wrapping a base counter. */
	protected transient int baseMask;
	/** Whether this set contains the null key. */
	protected transient boolean containsNull;
	/** The current table size (always a power of 2). */
	protected transient long n;
	/**
	 * Threshold after which we rehash. It must be the table size times {@link #f}.
	 */
	protected transient long maxFill;
	/**
	 * We never resize below this threshold, which is the construction-time {#n}.
	 */
	protected final transient long minN;
	/** The acceptable load factor. */
	protected final float f;
	/** Number of entries in the set. */
	protected long size;

	/** Initialises the mask values. */
	private void initMasks() {
		mask = n - 1;
		/*
		 * Note that either we have more than one segment, and in this case all segments
		 * are BigArrays.SEGMENT_SIZE long, or we have exactly one segment whose length
		 * is a power of two.
		 */
		segmentMask = key[0].length - 1;
		baseMask = key.length - 1;
	}

	/**
	 * Creates a new hash big set.
	 *
	 * <p>
	 * The actual table size will be the least power of two greater than
	 * {@code expected}/{@code f}.
	 *
	 * @param expected the expected number of elements in the set.
	 * @param f        the load factor.
	 */

	public DoubleOpenHashBigSet(final long expected, final float f) {
		if (f <= 0 || f > 1)
			throw new IllegalArgumentException("Load factor must be greater than 0 and smaller than or equal to 1");
		if (n < 0)
			throw new IllegalArgumentException("The expected number of elements must be nonnegative");
		this.f = f;
		minN = n = bigArraySize(expected, f);
		maxFill = maxFill(n, f);
		key = DoubleBigArrays.newBigArray(n);
		initMasks();
	}

	/**
	 * Creates a new hash big set with {@link Hash#DEFAULT_LOAD_FACTOR} as load
	 * factor.
	 *
	 * @param expected the expected number of elements in the hash big set.
	 */
	public DoubleOpenHashBigSet(final long expected) {
		this(expected, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Creates a new hash big set with initial expected
	 * {@link Hash#DEFAULT_INITIAL_SIZE} elements and
	 * {@link Hash#DEFAULT_LOAD_FACTOR} as load factor.
	 */
	public DoubleOpenHashBigSet() {
		this(DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Creates a new hash big set copying a given collection.
	 *
	 * @param c a {@link Collection} to be copied into the new hash big set.
	 * @param f the load factor.
	 */
	public DoubleOpenHashBigSet(final Collection<? extends Double> c, final float f) {
		this(c.size(), f);
		addAll(c);
	}

	/**
	 * Creates a new hash big set with {@link Hash#DEFAULT_LOAD_FACTOR} as load
	 * factor copying a given collection.
	 *
	 * @param c a {@link Collection} to be copied into the new hash big set.
	 */
	public DoubleOpenHashBigSet(final Collection<? extends Double> c) {
		this(c, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Creates a new hash big set copying a given type-specific collection.
	 *
	 * @param c a type-specific collection to be copied into the new hash big set.
	 * @param f the load factor.
	 */
	public DoubleOpenHashBigSet(final DoubleCollection c, final float f) {
		this(c.size(), f);
		addAll(c);
	}

	/**
	 * Creates a new hash big set with {@link Hash#DEFAULT_LOAD_FACTOR} as load
	 * factor copying a given type-specific collection.
	 *
	 * @param c a type-specific collection to be copied into the new hash big set.
	 */
	public DoubleOpenHashBigSet(final DoubleCollection c) {
		this(c, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Creates a new hash big set using elements provided by a type-specific
	 * iterator.
	 *
	 * @param i a type-specific iterator whose elements will fill the new hash big
	 *          set.
	 * @param f the load factor.
	 */
	public DoubleOpenHashBigSet(final DoubleIterator i, final float f) {
		this(DEFAULT_INITIAL_SIZE, f);
		while (i.hasNext())
			add(i.nextDouble());
	}

	/**
	 * Creates a new hash big set with {@link Hash#DEFAULT_LOAD_FACTOR} as load
	 * factor using elements provided by a type-specific iterator.
	 *
	 * @param i a type-specific iterator whose elements will fill the new hash big
	 *          set.
	 */
	public DoubleOpenHashBigSet(final DoubleIterator i) {
		this(i, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Creates a new hash big set using elements provided by an iterator.
	 *
	 * @param i an iterator whose elements will fill the new hash big set.
	 * @param f the load factor.
	 */
	public DoubleOpenHashBigSet(final Iterator<?> i, final float f) {
		this(DoubleIterators.asDoubleIterator(i), f);
	}

	/**
	 * Creates a new hash big set with {@link Hash#DEFAULT_LOAD_FACTOR} as load
	 * factor using elements provided by an iterator.
	 *
	 * @param i an iterator whose elements will fill the new hash big set.
	 */
	public DoubleOpenHashBigSet(final Iterator<?> i) {
		this(DoubleIterators.asDoubleIterator(i));
	}

	/**
	 * Creates a new hash big set and fills it with the elements of a given array.
	 *
	 * @param a      an array whose elements will be used to fill the new hash big
	 *               set.
	 * @param offset the first element to use.
	 * @param length the number of elements to use.
	 * @param f      the load factor.
	 */
	public DoubleOpenHashBigSet(final double[] a, final int offset, final int length, final float f) {
		this(length < 0 ? 0 : length, f);
		DoubleArrays.ensureOffsetLength(a, offset, length);
		for (int i = 0; i < length; i++)
			add(a[offset + i]);
	}

	/**
	 * Creates a new hash big set with {@link Hash#DEFAULT_LOAD_FACTOR} as load
	 * factor and fills it with the elements of a given array.
	 *
	 * @param a      an array whose elements will be used to fill the new hash big
	 *               set.
	 * @param offset the first element to use.
	 * @param length the number of elements to use.
	 */
	public DoubleOpenHashBigSet(final double[] a, final int offset, final int length) {
		this(a, offset, length, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Creates a new hash big set copying the elements of an array.
	 *
	 * @param a an array to be copied into the new hash big set.
	 * @param f the load factor.
	 */
	public DoubleOpenHashBigSet(final double[] a, final float f) {
		this(a, 0, a.length, f);
	}

	/**
	 * Creates a new hash big set with {@link Hash#DEFAULT_LOAD_FACTOR} as load
	 * factor copying the elements of an array.
	 *
	 * @param a an array to be copied into the new hash big set.
	 */
	public DoubleOpenHashBigSet(final double[] a) {
		this(a, DEFAULT_LOAD_FACTOR);
	}

	private long realSize() {
		return containsNull ? size - 1 : size;
	}

	private void ensureCapacity(final long capacity) {
		final long needed = bigArraySize(capacity, f);
		if (needed > n)
			rehash(needed);
	}

	@Override
	public boolean addAll(Collection<? extends Double> c) {
		final long size = c instanceof Size64 ? ((Size64) c).size64() : c.size();
		// The resulting collection will be at least c.size() big
		if (f <= .5)
			ensureCapacity(size); // The resulting collection will be sized for c.size() elements
		else
			ensureCapacity(size64() + size); // The resulting collection will be sized for size() + c.size() elements
		return super.addAll(c);
	}

	@Override
	public boolean addAll(DoubleCollection c) {
		final long size = c instanceof Size64 ? ((Size64) c).size64() : c.size();
		if (f <= .5)
			ensureCapacity(size); // The resulting collection will be size for c.size() elements
		else
			ensureCapacity(size64() + size); // The resulting collection will be sized for size() + c.size() elements
		return super.addAll(c);
	}

	@Override
	public boolean add(final double k) {
		int displ, base;
		if ((Double.doubleToLongBits(k) == 0)) {
			if (containsNull)
				return false;
			containsNull = true;
		} else {
			double curr;
			final double[][] key = this.key;
			final long h = it.unimi.dsi.fastutil.HashCommon.mix(Double.doubleToRawLongBits(k));
			// The starting point.
			if (!(Double
					.doubleToLongBits(curr = key[base = (int) ((h & mask) >>> BigArrays.SEGMENT_SHIFT)][displ = (int) (h
							& segmentMask)]) == 0)) {
				if ((Double.doubleToLongBits(curr) == Double.doubleToLongBits(k)))
					return false;
				while (!(Double
						.doubleToLongBits(curr = key[base = (base + ((displ = (displ + 1) & segmentMask) == 0 ? 1 : 0))
								& baseMask][displ]) == 0))
					if ((Double.doubleToLongBits(curr) == Double.doubleToLongBits(k)))
						return false;
			}
			key[base][displ] = k;
		}
		if (size++ >= maxFill)
			rehash(2 * n);
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
	protected final void shiftKeys(long pos) {
		// Shift entries with the same hash.
		long last, slot;
		final double[][] key = this.key;
		for (;;) {
			pos = ((last = pos) + 1) & mask;
			for (;;) {
				if ((Double.doubleToLongBits(BigArrays.get(key, pos)) == 0)) {
					set(key, last, (0));
					return;
				}
				slot = it.unimi.dsi.fastutil.HashCommon.mix(Double.doubleToRawLongBits(BigArrays.get(key, pos))) & mask;
				if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos)
					break;
				pos = (pos + 1) & mask;
			}
			set(key, last, BigArrays.get(key, pos));
		}
	}

	private boolean removeEntry(final int base, final int displ) {
		size--;
		shiftKeys(base * (long) BigArrays.SEGMENT_SIZE + displ);
		if (n > minN && size < maxFill / 4 && n > DEFAULT_INITIAL_SIZE)
			rehash(n / 2);
		return true;
	}

	private boolean removeNullEntry() {
		containsNull = false;
		size--;
		if (n > minN && size < maxFill / 4 && n > DEFAULT_INITIAL_SIZE)
			rehash(n / 2);
		return true;
	}

	@Override
	public boolean remove(final double k) {
		if ((Double.doubleToLongBits(k) == 0)) {
			if (containsNull)
				return removeNullEntry();
			return false;
		}
		double curr;
		final double[][] key = this.key;
		final long h = it.unimi.dsi.fastutil.HashCommon.mix(Double.doubleToRawLongBits(k));
		int displ, base;
		// The starting point.
		if ((Double.doubleToLongBits(curr = key[base = (int) ((h & mask) >>> BigArrays.SEGMENT_SHIFT)][displ = (int) (h
				& segmentMask)]) == 0))
			return false;
		if ((Double.doubleToLongBits(curr) == Double.doubleToLongBits(k)))
			return removeEntry(base, displ);
		while (true) {
			if ((Double.doubleToLongBits(curr = key[base = (base + ((displ = (displ + 1) & segmentMask) == 0 ? 1 : 0))
					& baseMask][displ]) == 0))
				return false;
			if ((Double.doubleToLongBits(curr) == Double.doubleToLongBits(k)))
				return removeEntry(base, displ);
		}
	}

	@Override
	public boolean contains(final double k) {
		if ((Double.doubleToLongBits(k) == 0))
			return containsNull;
		double curr;
		final double[][] key = this.key;
		final long h = it.unimi.dsi.fastutil.HashCommon.mix(Double.doubleToRawLongBits(k));
		int displ, base;
		// The starting point.
		if ((Double.doubleToLongBits(curr = key[base = (int) ((h & mask) >>> BigArrays.SEGMENT_SHIFT)][displ = (int) (h
				& segmentMask)]) == 0))
			return false;
		if ((Double.doubleToLongBits(curr) == Double.doubleToLongBits(k)))
			return true;
		while (true) {
			if ((Double.doubleToLongBits(curr = key[base = (base + ((displ = (displ + 1) & segmentMask) == 0 ? 1 : 0))
					& baseMask][displ]) == 0))
				return false;
			if ((Double.doubleToLongBits(curr) == Double.doubleToLongBits(k)))
				return true;
		}
	}

	/*
	 * Removes all elements from this set.
	 *
	 */
	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * To increase object reuse, this method does not change the table size. If you
	 * want to reduce the table size, you must use {@link #trim(long)}.
	 */
	@Override
	public void clear() {
		if (size == 0)
			return;
		size = 0;
		containsNull = false;
		fill(key, (0));
	}

	/** An iterator over a hash big set. */
	private class SetIterator implements DoubleIterator {
		/**
		 * The base of the last entry returned, if positive or zero; initially, the
		 * number of components of the key array. If negative, the last element returned
		 * was that of index {@code - base - 1} from the {@link #wrapped} list.
		 */
		int base = key.length;
		/** The displacement of the last entry returned; initially, zero. */
		int displ;
		/**
		 * The index of the last entry that has been returned (or {@link Long#MIN_VALUE}
		 * if {@link #base} is negative). It is -1 if either we did not return an entry
		 * yet, or the last returned entry has been removed.
		 */
		long last = -1;
		/** A downward counter measuring how many entries must still be returned. */
		long c = size;
		/** A boolean telling us whether we should return the null key. */
		boolean mustReturnNull = DoubleOpenHashBigSet.this.containsNull;
		/**
		 * A lazily allocated list containing elements that have wrapped around the
		 * table because of removals.
		 */
		DoubleArrayList wrapped;

		@Override
		public boolean hasNext() {
			return c != 0;
		}

		@Override
		public double nextDouble() {
			if (!hasNext())
				throw new NoSuchElementException();
			c--;
			if (mustReturnNull) {
				mustReturnNull = false;
				last = n;
				return (0);
			}
			final double[][] key = DoubleOpenHashBigSet.this.key;
			for (;;) {
				if (displ == 0 && base <= 0) {
					// We are just enumerating elements from the wrapped list.
					last = Long.MIN_VALUE;
					return wrapped.getDouble(-(--base) - 1);
				}
				if (displ-- == 0)
					displ = key[--base].length - 1;
				final double k = key[base][displ];
				if (!(Double.doubleToLongBits(k) == 0)) {
					last = base * (long) BigArrays.SEGMENT_SIZE + displ;
					return k;
				}
			}
		}

		/**
		 * Shifts left entries with the specified hash code, starting at the specified
		 * position, and empties the resulting free entry.
		 *
		 * @param pos a starting position.
		 */
		private final void shiftKeys(long pos) {
			// Shift entries with the same hash.
			long last, slot;
			double curr;
			final double[][] key = DoubleOpenHashBigSet.this.key;
			for (;;) {
				pos = ((last = pos) + 1) & mask;
				for (;;) {
					if ((Double.doubleToLongBits(curr = BigArrays.get(key, pos)) == 0)) {
						set(key, last, (0));
						return;
					}
					slot = it.unimi.dsi.fastutil.HashCommon.mix(Double.doubleToRawLongBits(curr)) & mask;
					if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos)
						break;
					pos = (pos + 1) & mask;
				}
				if (pos < last) { // Wrapped entry.
					if (wrapped == null)
						wrapped = new DoubleArrayList();
					wrapped.add(BigArrays.get(key, pos));
				}
				set(key, last, curr);
			}
		}

		@Override
		public void remove() {
			if (last == -1)
				throw new IllegalStateException();
			if (last == n)
				DoubleOpenHashBigSet.this.containsNull = false;
			else if (base >= 0)
				shiftKeys(last);
			else {
				// We're removing wrapped entries.
				DoubleOpenHashBigSet.this.remove(wrapped.getDouble(-base - 1));
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
	public DoubleIterator iterator() {
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
	 * @see #trim(long)
	 */
	public boolean trim() {
		return trim(size);
	}

	/**
	 * Rehashes this set if the table is too large.
	 *
	 * <p>
	 * Let <var>N</var> be the smallest table size that can hold
	 * <code>max(n,{@link #size64()})</code> entries, still satisfying the load
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
	public boolean trim(final long n) {
		final long l = bigArraySize(n, f);
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
	 * Resizes the set.
	 *
	 * <p>
	 * This method implements the basic rehashing strategy, and may be overriden by
	 * subclasses implementing different rehashing strategies (e.g., disk-based
	 * rehashing). However, you should not override this method unless you
	 * understand the internal workings of this class.
	 *
	 * @param newN the new size
	 */

	protected void rehash(final long newN) {
		final double key[][] = this.key;
		final double newKey[][] = DoubleBigArrays.newBigArray(newN);
		final long mask = newN - 1; // Note that this is used by the hashing macro
		final int newSegmentMask = newKey[0].length - 1;
		final int newBaseMask = newKey.length - 1;
		int base = 0, displ = 0, b, d;
		long h;
		double k;
		for (long i = realSize(); i-- != 0;) {
			while ((Double.doubleToLongBits(key[base][displ]) == 0))
				base = (base + ((displ = (displ + 1) & segmentMask) == 0 ? 1 : 0));
			k = key[base][displ];
			h = it.unimi.dsi.fastutil.HashCommon.mix(Double.doubleToRawLongBits(k));
			// The starting point.
			if (!(Double.doubleToLongBits(
					newKey[b = (int) ((h & mask) >>> BigArrays.SEGMENT_SHIFT)][d = (int) (h & newSegmentMask)]) == 0))
				while (!(Double.doubleToLongBits(
						newKey[b = (b + ((d = (d + 1) & newSegmentMask) == 0 ? 1 : 0)) & newBaseMask][d]) == 0))
					;
			newKey[b][d] = k;
			base = (base + ((displ = (displ + 1) & segmentMask) == 0 ? 1 : 0));
		}
		this.n = newN;
		this.key = newKey;
		initMasks();
		maxFill = maxFill(n, f);
	}

	@Deprecated
	@Override
	public int size() {
		return (int) Math.min(Integer.MAX_VALUE, size);
	}

	@Override
	public long size64() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Returns a deep copy of this big set.
	 *
	 * <p>
	 * This method performs a deep copy of this big hash set; the data stored in the
	 * set, however, is not cloned. Note that this makes a difference only for
	 * object keys.
	 *
	 * @return a deep copy of this big set.
	 */
	@Override

	public DoubleOpenHashBigSet clone() {
		DoubleOpenHashBigSet c;
		try {
			c = (DoubleOpenHashBigSet) super.clone();
		} catch (CloneNotSupportedException cantHappen) {
			throw new InternalError();
		}
		c.key = copy(key);
		c.containsNull = containsNull;
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
		final double key[][] = this.key;
		int h = 0, base = 0, displ = 0;
		for (long j = realSize(); j-- != 0;) {
			while ((Double.doubleToLongBits(key[base][displ]) == 0))
				base = (base + ((displ = (displ + 1) & segmentMask) == 0 ? 1 : 0));
			h += it.unimi.dsi.fastutil.HashCommon.double2int(key[base][displ]);
			base = (base + ((displ = (displ + 1) & segmentMask) == 0 ? 1 : 0));
		}
		return h;
	}

	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
		final DoubleIterator i = iterator();
		s.defaultWriteObject();
		for (long j = size; j-- != 0;)
			s.writeDouble(i.nextDouble());
	}

	private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
		s.defaultReadObject();
		n = bigArraySize(size, f);
		maxFill = maxFill(n, f);
		final double[][] key = this.key = DoubleBigArrays.newBigArray(n);
		initMasks();
		long h;
		double k;
		int base, displ;
		for (long i = size; i-- != 0;) {
			k = s.readDouble();
			if ((Double.doubleToLongBits(k) == 0))
				containsNull = true;
			else {
				h = it.unimi.dsi.fastutil.HashCommon.mix(Double.doubleToRawLongBits(k));
				if (!(Double
						.doubleToLongBits(key[base = (int) ((h & mask) >>> BigArrays.SEGMENT_SHIFT)][displ = (int) (h
								& segmentMask)]) == 0))
					while (!(Double
							.doubleToLongBits(key[base = (base + ((displ = (displ + 1) & segmentMask) == 0 ? 1 : 0))
									& baseMask][displ]) == 0))
						;
				key[base][displ] = k;
			}
		}
		if (ASSERTS)
			checkTable();
	}

	private void checkTable() {
	}
}
