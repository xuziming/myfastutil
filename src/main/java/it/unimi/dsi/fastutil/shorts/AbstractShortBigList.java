/*
	* Copyright (C) 2010-2019 Sebastiano Vigna
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

import static it.unimi.dsi.fastutil.BigArrays.ensureOffsetLength;
import static it.unimi.dsi.fastutil.BigArrays.length;
import it.unimi.dsi.fastutil.BigArrays;
import java.util.Iterator;
import java.util.Collection;
import java.util.NoSuchElementException;
import it.unimi.dsi.fastutil.BigList;
import it.unimi.dsi.fastutil.BigListIterator;

/**
 * An abstract class providing basic methods for big lists implementing a
 * type-specific big list interface.
 */
public abstract class AbstractShortBigList extends AbstractShortCollection implements ShortBigList, ShortStack {
	protected AbstractShortBigList() {
	}

	/**
	 * Ensures that the given index is nonnegative and not greater than this
	 * big-list size.
	 *
	 * @param index an index.
	 * @throws IndexOutOfBoundsException if the given index is negative or greater
	 *                                   than this big-list size.
	 */
	protected void ensureIndex(final long index) {
		if (index < 0)
			throw new IndexOutOfBoundsException("Index (" + index + ") is negative");
		if (index > size64())
			throw new IndexOutOfBoundsException("Index (" + index + ") is greater than list size (" + (size64()) + ")");
	}

	/**
	 * Ensures that the given index is nonnegative and smaller than this big-list
	 * size.
	 *
	 * @param index an index.
	 * @throws IndexOutOfBoundsException if the given index is negative or not
	 *                                   smaller than this big-list size.
	 */
	protected void ensureRestrictedIndex(final long index) {
		if (index < 0)
			throw new IndexOutOfBoundsException("Index (" + index + ") is negative");
		if (index >= size64())
			throw new IndexOutOfBoundsException(
					"Index (" + index + ") is greater than or equal to list size (" + (size64()) + ")");
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation always throws an {@link UnsupportedOperationException}.
	 */
	@Override
	public void add(final long index, final short k) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation delegates to the type-specific version of
	 * {@link BigList#add(long, Object)}.
	 */
	@Override
	public boolean add(final short k) {
		add(size64(), k);
		return true;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation always throws an {@link UnsupportedOperationException}.
	 */
	@Override
	public short removeShort(long i) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation always throws an {@link UnsupportedOperationException}.
	 */
	@Override
	public short set(final long index, final short k) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Adds all of the elements in the specified collection to this list (optional
	 * operation).
	 */
	@Override
	public boolean addAll(long index, final Collection<? extends Short> c) {
		ensureIndex(index);
		final Iterator<? extends Short> i = c.iterator();
		final boolean retVal = i.hasNext();
		while (i.hasNext())
			add(index++, i.next());
		return retVal;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation delegates to the type-specific version of
	 * {@link BigList#addAll(long, Collection)}.
	 */
	@Override
	public boolean addAll(final Collection<? extends Short> c) {
		return addAll(size64(), c);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation delegates to {@link #listIterator()}.
	 */
	@Override
	public ShortBigListIterator iterator() {
		return listIterator();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation delegates to {@link BigList#listIterator(long)
	 * listIterator(0)}.
	 */
	@Override
	public ShortBigListIterator listIterator() {
		return listIterator(0L);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation is based on the random-access methods.
	 */
	@Override
	public ShortBigListIterator listIterator(final long index) {
		ensureIndex(index);
		return new ShortBigListIterator() {
			long pos = index, last = -1;

			@Override
			public boolean hasNext() {
				return pos < AbstractShortBigList.this.size64();
			}

			@Override
			public boolean hasPrevious() {
				return pos > 0;
			}

			@Override
			public short nextShort() {
				if (!hasNext())
					throw new NoSuchElementException();
				return AbstractShortBigList.this.getShort(last = pos++);
			}

			@Override
			public short previousShort() {
				if (!hasPrevious())
					throw new NoSuchElementException();
				return AbstractShortBigList.this.getShort(last = --pos);
			}

			@Override
			public long nextIndex() {
				return pos;
			}

			@Override
			public long previousIndex() {
				return pos - 1;
			}

			@Override
			public void add(short k) {
				AbstractShortBigList.this.add(pos++, k);
				last = -1;
			}

			@Override
			public void set(short k) {
				if (last == -1)
					throw new IllegalStateException();
				AbstractShortBigList.this.set(last, k);
			}

			@Override
			public void remove() {
				if (last == -1)
					throw new IllegalStateException();
				AbstractShortBigList.this.removeShort(last);
				/*
				 * If the last operation was a next(), we are removing an element *before* us,
				 * and we must decrease pos correspondingly.
				 */
				if (last < pos)
					pos--;
				last = -1;
			}
		};
	}

	/**
	 * Returns true if this list contains the specified element.
	 * <p>
	 * This implementation delegates to {@code indexOf()}.
	 * 
	 * @see BigList#contains(Object)
	 */
	@Override
	public boolean contains(final short k) {
		return indexOf(k) >= 0;
	}

	@Override
	public long indexOf(final short k) {
		final ShortBigListIterator i = listIterator();
		short e;
		while (i.hasNext()) {
			e = i.nextShort();
			if (((k) == (e)))
				return i.previousIndex();
		}
		return -1;
	}

	@Override
	public long lastIndexOf(final short k) {
		ShortBigListIterator i = listIterator(size64());
		short e;
		while (i.hasPrevious()) {
			e = i.previousShort();
			if (((k) == (e)))
				return i.nextIndex();
		}
		return -1;
	}

	@Override
	public void size(final long size) {
		long i = size64();
		if (size > i)
			while (i++ < size)
				add(((short) 0));
		else
			while (i-- != size)
				remove(i);
	}

	@Override
	public ShortBigList subList(final long from, final long to) {
		ensureIndex(from);
		ensureIndex(to);
		if (from > to)
			throw new IndexOutOfBoundsException("Start index (" + from + ") is greater than end index (" + to + ")");
		return new ShortSubList(this, from, to);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This is a trivial iterator-based implementation. It is expected that
	 * implementations will override this method with a more optimized version.
	 */
	@Override
	public void removeElements(final long from, final long to) {
		ensureIndex(to);
		ShortBigListIterator i = listIterator(from);
		long n = to - from;
		if (n < 0)
			throw new IllegalArgumentException("Start index (" + from + ") is greater than end index (" + to + ")");
		while (n-- != 0) {
			i.nextShort();
			i.remove();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This is a trivial iterator-based implementation. It is expected that
	 * implementations will override this method with a more optimized version.
	 */
	@Override
	public void addElements(long index, final short a[][], long offset, long length) {
		ensureIndex(index);
		ensureOffsetLength(a, offset, length);
		while (length-- != 0)
			add(index++, BigArrays.get(a, offset++));
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation delegates to the analogous method for big-array
	 * fragments.
	 */
	@Override
	public void addElements(final long index, final short a[][]) {
		addElements(index, a, 0, length(a));
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This is a trivial iterator-based implementation. It is expected that
	 * implementations will override this method with a more optimized version.
	 */
	@Override
	public void getElements(final long from, final short a[][], long offset, long length) {
		ShortBigListIterator i = listIterator(from);
		ensureOffsetLength(a, offset, length);
		if (from + length > size64())
			throw new IndexOutOfBoundsException(
					"End index (" + (from + length) + ") is greater than list size (" + size64() + ")");
		while (length-- != 0)
			BigArrays.set(a, offset++, i.nextShort());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation delegates to {@link #removeElements(long, long)}.
	 */
	@Override
	public void clear() {
		removeElements(0, size64());
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation delegates to {@link #size64()}.
	 * 
	 * @deprecated Please use {@link #size64()} instead.
	 */
	@Override
	@Deprecated
	public int size() {
		return (int) Math.min(Integer.MAX_VALUE, size64());
	}

	private boolean valEquals(final Object a, final Object b) {
		return a == null ? b == null : a.equals(b);
	}

	/**
	 * Returns the hash code for this big list, which is identical to
	 * {@link java.util.List#hashCode()}.
	 *
	 * @return the hash code for this big list.
	 */
	@Override
	public int hashCode() {
		ShortIterator i = iterator();
		int h = 1;
		long s = size64();
		while (s-- != 0) {
			short k = i.nextShort();
			h = 31 * h + (k);
		}
		return h;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this)
			return true;
		if (!(o instanceof BigList))
			return false;
		final BigList<?> l = (BigList<?>) o;
		long s = size64();
		if (s != l.size64())
			return false;
		if (l instanceof ShortBigList) {
			final ShortBigListIterator i1 = listIterator(), i2 = ((ShortBigList) l).listIterator();
			while (s-- != 0)
				if (i1.nextShort() != i2.nextShort())
					return false;
			return true;
		}
		final BigListIterator<?> i1 = listIterator(), i2 = l.listIterator();
		while (s-- != 0)
			if (!valEquals(i1.next(), i2.next()))
				return false;
		return true;
	}

	/**
	 * Compares this big list to another object. If the argument is a
	 * {@link BigList}, this method performs a lexicographical comparison;
	 * otherwise, it throws a {@code ClassCastException}.
	 *
	 * @param l a big list.
	 * @return if the argument is a {@link BigList}, a negative integer, zero, or a
	 *         positive integer as this list is lexicographically less than, equal
	 *         to, or greater than the argument.
	 * @throws ClassCastException if the argument is not a big list.
	 */

	@Override
	public int compareTo(final BigList<? extends Short> l) {
		if (l == this)
			return 0;
		if (l instanceof ShortBigList) {
			final ShortBigListIterator i1 = listIterator(), i2 = ((ShortBigList) l).listIterator();
			int r;
			short e1, e2;
			while (i1.hasNext() && i2.hasNext()) {
				e1 = i1.nextShort();
				e2 = i2.nextShort();
				if ((r = (Short.compare((e1), (e2)))) != 0)
					return r;
			}
			return i2.hasNext() ? -1 : (i1.hasNext() ? 1 : 0);
		}
		BigListIterator<? extends Short> i1 = listIterator(), i2 = l.listIterator();
		int r;
		while (i1.hasNext() && i2.hasNext()) {
			if ((r = ((Comparable<? super Short>) i1.next()).compareTo(i2.next())) != 0)
				return r;
		}
		return i2.hasNext() ? -1 : (i1.hasNext() ? 1 : 0);
	}

	@Override
	public void push(short o) {
		add(o);
	}

	@Override
	public short popShort() {
		if (isEmpty())
			throw new NoSuchElementException();
		return removeShort(size64() - 1);
	}

	@Override
	public short topShort() {
		if (isEmpty())
			throw new NoSuchElementException();
		return getShort(size64() - 1);
	}

	@Override
	public short peekShort(int i) {
		return getShort(size64() - 1 - i);
	}

	/**
	 * Removes a single instance of the specified element from this collection, if
	 * it is present (optional operation).
	 * <p>
	 * This implementation delegates to {@code indexOf()}.
	 * 
	 * @see BigList#remove(Object)
	 */
	@Override
	public boolean rem(short k) {
		long index = indexOf(k);
		if (index == -1)
			return false;
		removeShort(index);
		return true;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation delegates to the type-specific version of
	 * {@link #addAll(long, Collection)}.
	 */
	@Override
	public boolean addAll(final long index, final ShortCollection c) {
		return addAll(index, (Collection<? extends Short>) c);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation delegates to the type-specific version of
	 * {@link #addAll(long, Collection)}.
	 */
	@Override
	public boolean addAll(final long index, final ShortBigList l) {
		return addAll(index, (ShortCollection) l);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation delegates to the type-specific version of
	 * {@link #addAll(long, Collection)}.
	 */
	@Override
	public boolean addAll(final ShortCollection c) {
		return addAll(size64(), c);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation delegates to the type-specific list version of
	 * {@link #addAll(long, Collection)}.
	 */
	@Override
	public boolean addAll(final ShortBigList l) {
		return addAll(size64(), l);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation delegates to the corresponding type-specific method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public void add(final long index, final Short ok) {
		add(index, ok.shortValue());
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation delegates to the corresponding type-specific method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public Short set(final long index, final Short ok) {
		return Short.valueOf(set(index, ok.shortValue()));
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation delegates to the corresponding type-specific method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public Short get(final long index) {
		return Short.valueOf(getShort(index));
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation delegates to the corresponding type-specific method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public long indexOf(final Object ok) {
		return indexOf(((Short) (ok)).shortValue());
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation delegates to the corresponding type-specific method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public long lastIndexOf(final Object ok) {
		return lastIndexOf(((Short) (ok)).shortValue());
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation delegates to the corresponding type-specific method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public Short remove(final long index) {
		return Short.valueOf(removeShort(index));
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation delegates to the corresponding type-specific method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public void push(Short o) {
		push(o.shortValue());
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation delegates to the corresponding type-specific method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public Short pop() {
		return Short.valueOf(popShort());
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation delegates to the corresponding type-specific method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public Short top() {
		return Short.valueOf(topShort());
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation delegates to the corresponding type-specific method.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public Short peek(int i) {
		return Short.valueOf(peekShort(i));
	}

	@Override
	public String toString() {
		final StringBuilder s = new StringBuilder();
		final ShortIterator i = iterator();
		long n = size64();
		short k;
		boolean first = true;
		s.append("[");
		while (n-- != 0) {
			if (first)
				first = false;
			else
				s.append(", ");
			k = i.nextShort();
			s.append(String.valueOf(k));
		}
		s.append("]");
		return s.toString();
	}

	/** A class implementing a sublist view. */
	public static class ShortSubList extends AbstractShortBigList implements java.io.Serializable {
		private static final long serialVersionUID = -7046029254386353129L;
		/** The list this sublist restricts. */
		protected final ShortBigList l;
		/** Initial (inclusive) index of this sublist. */
		protected final long from;
		/** Final (exclusive) index of this sublist. */
		protected long to;

		public ShortSubList(final ShortBigList l, final long from, final long to) {
			this.l = l;
			this.from = from;
			this.to = to;
		}

		private boolean assertRange() {
			assert from <= l.size64();
			assert to <= l.size64();
			assert to >= from;
			return true;
		}

		@Override
		public boolean add(final short k) {
			l.add(to, k);
			to++;
			assert assertRange();
			return true;
		}

		@Override
		public void add(final long index, final short k) {
			ensureIndex(index);
			l.add(from + index, k);
			to++;
			assert assertRange();
		}

		@Override
		public boolean addAll(final long index, final Collection<? extends Short> c) {
			ensureIndex(index);
			to += c.size();
			return l.addAll(from + index, c);
		}

		@Override
		public short getShort(long index) {
			ensureRestrictedIndex(index);
			return l.getShort(from + index);
		}

		@Override
		public short removeShort(long index) {
			ensureRestrictedIndex(index);
			to--;
			return l.removeShort(from + index);
		}

		@Override
		public short set(long index, short k) {
			ensureRestrictedIndex(index);
			return l.set(from + index, k);
		}

		@Override
		public long size64() {
			return to - from;
		}

		@Override
		public void getElements(final long from, final short[][] a, final long offset, final long length) {
			ensureIndex(from);
			if (from + length > size64())
				throw new IndexOutOfBoundsException(
						"End index (" + from + length + ") is greater than list size (" + size64() + ")");
			l.getElements(this.from + from, a, offset, length);
		}

		@Override
		public void removeElements(final long from, final long to) {
			ensureIndex(from);
			ensureIndex(to);
			l.removeElements(this.from + from, this.from + to);
			this.to -= (to - from);
			assert assertRange();
		}

		@Override
		public void addElements(final long index, final short a[][], long offset, long length) {
			ensureIndex(index);
			l.addElements(this.from + index, a, offset, length);
			this.to += length;
			assert assertRange();
		}

		@Override
		public ShortBigListIterator listIterator(final long index) {
			ensureIndex(index);
			return new ShortBigListIterator() {
				long pos = index, last = -1;

				@Override
				public boolean hasNext() {
					return pos < size64();
				}

				@Override
				public boolean hasPrevious() {
					return pos > 0;
				}

				@Override
				public short nextShort() {
					if (!hasNext())
						throw new NoSuchElementException();
					return l.getShort(from + (last = pos++));
				}

				@Override
				public short previousShort() {
					if (!hasPrevious())
						throw new NoSuchElementException();
					return l.getShort(from + (last = --pos));
				}

				@Override
				public long nextIndex() {
					return pos;
				}

				@Override
				public long previousIndex() {
					return pos - 1;
				}

				@Override
				public void add(short k) {
					if (last == -1)
						throw new IllegalStateException();
					ShortSubList.this.add(pos++, k);
					last = -1;
					assert assertRange();
				}

				@Override
				public void set(short k) {
					if (last == -1)
						throw new IllegalStateException();
					ShortSubList.this.set(last, k);
				}

				@Override
				public void remove() {
					if (last == -1)
						throw new IllegalStateException();
					ShortSubList.this.removeShort(last);
					/*
					 * If the last operation was a next(), we are removing an element *before* us,
					 * and we must decrease pos correspondingly.
					 */
					if (last < pos)
						pos--;
					last = -1;
					assert assertRange();
				}
			};
		}

		@Override
		public ShortBigList subList(final long from, final long to) {
			ensureIndex(from);
			ensureIndex(to);
			if (from > to)
				throw new IllegalArgumentException("Start index (" + from + ") is greater than end index (" + to + ")");
			return new ShortSubList(this, from, to);
		}

		@Override
		public boolean rem(short k) {
			long index = indexOf(k);
			if (index == -1)
				return false;
			to--;
			l.removeShort(from + index);
			assert assertRange();
			return true;
		}

		@Override
		public boolean addAll(final long index, final ShortCollection c) {
			ensureIndex(index);
			return super.addAll(index, c);
		}

		@Override
		public boolean addAll(final long index, final ShortBigList l) {
			ensureIndex(index);
			return super.addAll(index, l);
		}
	}
}
