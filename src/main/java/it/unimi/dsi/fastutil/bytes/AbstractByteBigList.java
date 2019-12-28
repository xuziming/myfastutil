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
package it.unimi.dsi.fastutil.bytes;

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
public abstract class AbstractByteBigList extends AbstractByteCollection implements ByteBigList, ByteStack {
	protected AbstractByteBigList() {
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
	public void add(final long index, final byte k) {
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
	public boolean add(final byte k) {
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
	public byte removeByte(long i) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation always throws an {@link UnsupportedOperationException}.
	 */
	@Override
	public byte set(final long index, final byte k) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Adds all of the elements in the specified collection to this list (optional
	 * operation).
	 */
	@Override
	public boolean addAll(long index, final Collection<? extends Byte> c) {
		ensureIndex(index);
		final Iterator<? extends Byte> i = c.iterator();
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
	public boolean addAll(final Collection<? extends Byte> c) {
		return addAll(size64(), c);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation delegates to {@link #listIterator()}.
	 */
	@Override
	public ByteBigListIterator iterator() {
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
	public ByteBigListIterator listIterator() {
		return listIterator(0L);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation is based on the random-access methods.
	 */
	@Override
	public ByteBigListIterator listIterator(final long index) {
		ensureIndex(index);
		return new ByteBigListIterator() {
			long pos = index, last = -1;

			@Override
			public boolean hasNext() {
				return pos < AbstractByteBigList.this.size64();
			}

			@Override
			public boolean hasPrevious() {
				return pos > 0;
			}

			@Override
			public byte nextByte() {
				if (!hasNext())
					throw new NoSuchElementException();
				return AbstractByteBigList.this.getByte(last = pos++);
			}

			@Override
			public byte previousByte() {
				if (!hasPrevious())
					throw new NoSuchElementException();
				return AbstractByteBigList.this.getByte(last = --pos);
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
			public void add(byte k) {
				AbstractByteBigList.this.add(pos++, k);
				last = -1;
			}

			@Override
			public void set(byte k) {
				if (last == -1)
					throw new IllegalStateException();
				AbstractByteBigList.this.set(last, k);
			}

			@Override
			public void remove() {
				if (last == -1)
					throw new IllegalStateException();
				AbstractByteBigList.this.removeByte(last);
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
	public boolean contains(final byte k) {
		return indexOf(k) >= 0;
	}

	@Override
	public long indexOf(final byte k) {
		final ByteBigListIterator i = listIterator();
		byte e;
		while (i.hasNext()) {
			e = i.nextByte();
			if (((k) == (e)))
				return i.previousIndex();
		}
		return -1;
	}

	@Override
	public long lastIndexOf(final byte k) {
		ByteBigListIterator i = listIterator(size64());
		byte e;
		while (i.hasPrevious()) {
			e = i.previousByte();
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
				add(((byte) 0));
		else
			while (i-- != size)
				remove(i);
	}

	@Override
	public ByteBigList subList(final long from, final long to) {
		ensureIndex(from);
		ensureIndex(to);
		if (from > to)
			throw new IndexOutOfBoundsException("Start index (" + from + ") is greater than end index (" + to + ")");
		return new ByteSubList(this, from, to);
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
		ByteBigListIterator i = listIterator(from);
		long n = to - from;
		if (n < 0)
			throw new IllegalArgumentException("Start index (" + from + ") is greater than end index (" + to + ")");
		while (n-- != 0) {
			i.nextByte();
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
	public void addElements(long index, final byte a[][], long offset, long length) {
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
	public void addElements(final long index, final byte a[][]) {
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
	public void getElements(final long from, final byte a[][], long offset, long length) {
		ByteBigListIterator i = listIterator(from);
		ensureOffsetLength(a, offset, length);
		if (from + length > size64())
			throw new IndexOutOfBoundsException(
					"End index (" + (from + length) + ") is greater than list size (" + size64() + ")");
		while (length-- != 0)
			BigArrays.set(a, offset++, i.nextByte());
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
		ByteIterator i = iterator();
		int h = 1;
		long s = size64();
		while (s-- != 0) {
			byte k = i.nextByte();
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
		if (l instanceof ByteBigList) {
			final ByteBigListIterator i1 = listIterator(), i2 = ((ByteBigList) l).listIterator();
			while (s-- != 0)
				if (i1.nextByte() != i2.nextByte())
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
	public int compareTo(final BigList<? extends Byte> l) {
		if (l == this)
			return 0;
		if (l instanceof ByteBigList) {
			final ByteBigListIterator i1 = listIterator(), i2 = ((ByteBigList) l).listIterator();
			int r;
			byte e1, e2;
			while (i1.hasNext() && i2.hasNext()) {
				e1 = i1.nextByte();
				e2 = i2.nextByte();
				if ((r = (Byte.compare((e1), (e2)))) != 0)
					return r;
			}
			return i2.hasNext() ? -1 : (i1.hasNext() ? 1 : 0);
		}
		BigListIterator<? extends Byte> i1 = listIterator(), i2 = l.listIterator();
		int r;
		while (i1.hasNext() && i2.hasNext()) {
			if ((r = ((Comparable<? super Byte>) i1.next()).compareTo(i2.next())) != 0)
				return r;
		}
		return i2.hasNext() ? -1 : (i1.hasNext() ? 1 : 0);
	}

	@Override
	public void push(byte o) {
		add(o);
	}

	@Override
	public byte popByte() {
		if (isEmpty())
			throw new NoSuchElementException();
		return removeByte(size64() - 1);
	}

	@Override
	public byte topByte() {
		if (isEmpty())
			throw new NoSuchElementException();
		return getByte(size64() - 1);
	}

	@Override
	public byte peekByte(int i) {
		return getByte(size64() - 1 - i);
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
	public boolean rem(byte k) {
		long index = indexOf(k);
		if (index == -1)
			return false;
		removeByte(index);
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
	public boolean addAll(final long index, final ByteCollection c) {
		return addAll(index, (Collection<? extends Byte>) c);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation delegates to the type-specific version of
	 * {@link #addAll(long, Collection)}.
	 */
	@Override
	public boolean addAll(final long index, final ByteBigList l) {
		return addAll(index, (ByteCollection) l);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation delegates to the type-specific version of
	 * {@link #addAll(long, Collection)}.
	 */
	@Override
	public boolean addAll(final ByteCollection c) {
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
	public boolean addAll(final ByteBigList l) {
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
	public void add(final long index, final Byte ok) {
		add(index, ok.byteValue());
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
	public Byte set(final long index, final Byte ok) {
		return Byte.valueOf(set(index, ok.byteValue()));
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
	public Byte get(final long index) {
		return Byte.valueOf(getByte(index));
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
		return indexOf(((Byte) (ok)).byteValue());
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
		return lastIndexOf(((Byte) (ok)).byteValue());
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
	public Byte remove(final long index) {
		return Byte.valueOf(removeByte(index));
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
	public void push(Byte o) {
		push(o.byteValue());
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
	public Byte pop() {
		return Byte.valueOf(popByte());
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
	public Byte top() {
		return Byte.valueOf(topByte());
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
	public Byte peek(int i) {
		return Byte.valueOf(peekByte(i));
	}

	@Override
	public String toString() {
		final StringBuilder s = new StringBuilder();
		final ByteIterator i = iterator();
		long n = size64();
		byte k;
		boolean first = true;
		s.append("[");
		while (n-- != 0) {
			if (first)
				first = false;
			else
				s.append(", ");
			k = i.nextByte();
			s.append(String.valueOf(k));
		}
		s.append("]");
		return s.toString();
	}

	/** A class implementing a sublist view. */
	public static class ByteSubList extends AbstractByteBigList implements java.io.Serializable {
		private static final long serialVersionUID = -7046029254386353129L;
		/** The list this sublist restricts. */
		protected final ByteBigList l;
		/** Initial (inclusive) index of this sublist. */
		protected final long from;
		/** Final (exclusive) index of this sublist. */
		protected long to;

		public ByteSubList(final ByteBigList l, final long from, final long to) {
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
		public boolean add(final byte k) {
			l.add(to, k);
			to++;
			assert assertRange();
			return true;
		}

		@Override
		public void add(final long index, final byte k) {
			ensureIndex(index);
			l.add(from + index, k);
			to++;
			assert assertRange();
		}

		@Override
		public boolean addAll(final long index, final Collection<? extends Byte> c) {
			ensureIndex(index);
			to += c.size();
			return l.addAll(from + index, c);
		}

		@Override
		public byte getByte(long index) {
			ensureRestrictedIndex(index);
			return l.getByte(from + index);
		}

		@Override
		public byte removeByte(long index) {
			ensureRestrictedIndex(index);
			to--;
			return l.removeByte(from + index);
		}

		@Override
		public byte set(long index, byte k) {
			ensureRestrictedIndex(index);
			return l.set(from + index, k);
		}

		@Override
		public long size64() {
			return to - from;
		}

		@Override
		public void getElements(final long from, final byte[][] a, final long offset, final long length) {
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
		public void addElements(final long index, final byte a[][], long offset, long length) {
			ensureIndex(index);
			l.addElements(this.from + index, a, offset, length);
			this.to += length;
			assert assertRange();
		}

		@Override
		public ByteBigListIterator listIterator(final long index) {
			ensureIndex(index);
			return new ByteBigListIterator() {
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
				public byte nextByte() {
					if (!hasNext())
						throw new NoSuchElementException();
					return l.getByte(from + (last = pos++));
				}

				@Override
				public byte previousByte() {
					if (!hasPrevious())
						throw new NoSuchElementException();
					return l.getByte(from + (last = --pos));
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
				public void add(byte k) {
					if (last == -1)
						throw new IllegalStateException();
					ByteSubList.this.add(pos++, k);
					last = -1;
					assert assertRange();
				}

				@Override
				public void set(byte k) {
					if (last == -1)
						throw new IllegalStateException();
					ByteSubList.this.set(last, k);
				}

				@Override
				public void remove() {
					if (last == -1)
						throw new IllegalStateException();
					ByteSubList.this.removeByte(last);
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
		public ByteBigList subList(final long from, final long to) {
			ensureIndex(from);
			ensureIndex(to);
			if (from > to)
				throw new IllegalArgumentException("Start index (" + from + ") is greater than end index (" + to + ")");
			return new ByteSubList(this, from, to);
		}

		@Override
		public boolean rem(byte k) {
			long index = indexOf(k);
			if (index == -1)
				return false;
			to--;
			l.removeByte(from + index);
			assert assertRange();
			return true;
		}

		@Override
		public boolean addAll(final long index, final ByteCollection c) {
			ensureIndex(index);
			return super.addAll(index, c);
		}

		@Override
		public boolean addAll(final long index, final ByteBigList l) {
			ensureIndex(index);
			return super.addAll(index, l);
		}
	}
}
