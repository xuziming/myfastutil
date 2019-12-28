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
package it.unimi.dsi.fastutil.doubles;

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
public abstract class AbstractDoubleBigList extends AbstractDoubleCollection implements DoubleBigList, DoubleStack {
	protected AbstractDoubleBigList() {
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
	public void add(final long index, final double k) {
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
	public boolean add(final double k) {
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
	public double removeDouble(long i) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation always throws an {@link UnsupportedOperationException}.
	 */
	@Override
	public double set(final long index, final double k) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Adds all of the elements in the specified collection to this list (optional
	 * operation).
	 */
	@Override
	public boolean addAll(long index, final Collection<? extends Double> c) {
		ensureIndex(index);
		final Iterator<? extends Double> i = c.iterator();
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
	public boolean addAll(final Collection<? extends Double> c) {
		return addAll(size64(), c);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation delegates to {@link #listIterator()}.
	 */
	@Override
	public DoubleBigListIterator iterator() {
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
	public DoubleBigListIterator listIterator() {
		return listIterator(0L);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation is based on the random-access methods.
	 */
	@Override
	public DoubleBigListIterator listIterator(final long index) {
		ensureIndex(index);
		return new DoubleBigListIterator() {
			long pos = index, last = -1;

			@Override
			public boolean hasNext() {
				return pos < AbstractDoubleBigList.this.size64();
			}

			@Override
			public boolean hasPrevious() {
				return pos > 0;
			}

			@Override
			public double nextDouble() {
				if (!hasNext())
					throw new NoSuchElementException();
				return AbstractDoubleBigList.this.getDouble(last = pos++);
			}

			@Override
			public double previousDouble() {
				if (!hasPrevious())
					throw new NoSuchElementException();
				return AbstractDoubleBigList.this.getDouble(last = --pos);
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
			public void add(double k) {
				AbstractDoubleBigList.this.add(pos++, k);
				last = -1;
			}

			@Override
			public void set(double k) {
				if (last == -1)
					throw new IllegalStateException();
				AbstractDoubleBigList.this.set(last, k);
			}

			@Override
			public void remove() {
				if (last == -1)
					throw new IllegalStateException();
				AbstractDoubleBigList.this.removeDouble(last);
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
	public boolean contains(final double k) {
		return indexOf(k) >= 0;
	}

	@Override
	public long indexOf(final double k) {
		final DoubleBigListIterator i = listIterator();
		double e;
		while (i.hasNext()) {
			e = i.nextDouble();
			if ((Double.doubleToLongBits(k) == Double.doubleToLongBits(e)))
				return i.previousIndex();
		}
		return -1;
	}

	@Override
	public long lastIndexOf(final double k) {
		DoubleBigListIterator i = listIterator(size64());
		double e;
		while (i.hasPrevious()) {
			e = i.previousDouble();
			if ((Double.doubleToLongBits(k) == Double.doubleToLongBits(e)))
				return i.nextIndex();
		}
		return -1;
	}

	@Override
	public void size(final long size) {
		long i = size64();
		if (size > i)
			while (i++ < size)
				add((0));
		else
			while (i-- != size)
				remove(i);
	}

	@Override
	public DoubleBigList subList(final long from, final long to) {
		ensureIndex(from);
		ensureIndex(to);
		if (from > to)
			throw new IndexOutOfBoundsException("Start index (" + from + ") is greater than end index (" + to + ")");
		return new DoubleSubList(this, from, to);
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
		DoubleBigListIterator i = listIterator(from);
		long n = to - from;
		if (n < 0)
			throw new IllegalArgumentException("Start index (" + from + ") is greater than end index (" + to + ")");
		while (n-- != 0) {
			i.nextDouble();
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
	public void addElements(long index, final double a[][], long offset, long length) {
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
	public void addElements(final long index, final double a[][]) {
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
	public void getElements(final long from, final double a[][], long offset, long length) {
		DoubleBigListIterator i = listIterator(from);
		ensureOffsetLength(a, offset, length);
		if (from + length > size64())
			throw new IndexOutOfBoundsException(
					"End index (" + (from + length) + ") is greater than list size (" + size64() + ")");
		while (length-- != 0)
			BigArrays.set(a, offset++, i.nextDouble());
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
		DoubleIterator i = iterator();
		int h = 1;
		long s = size64();
		while (s-- != 0) {
			double k = i.nextDouble();
			h = 31 * h + it.unimi.dsi.fastutil.HashCommon.double2int(k);
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
		if (l instanceof DoubleBigList) {
			final DoubleBigListIterator i1 = listIterator(), i2 = ((DoubleBigList) l).listIterator();
			while (s-- != 0)
				if (i1.nextDouble() != i2.nextDouble())
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
	public int compareTo(final BigList<? extends Double> l) {
		if (l == this)
			return 0;
		if (l instanceof DoubleBigList) {
			final DoubleBigListIterator i1 = listIterator(), i2 = ((DoubleBigList) l).listIterator();
			int r;
			double e1, e2;
			while (i1.hasNext() && i2.hasNext()) {
				e1 = i1.nextDouble();
				e2 = i2.nextDouble();
				if ((r = (Double.compare((e1), (e2)))) != 0)
					return r;
			}
			return i2.hasNext() ? -1 : (i1.hasNext() ? 1 : 0);
		}
		BigListIterator<? extends Double> i1 = listIterator(), i2 = l.listIterator();
		int r;
		while (i1.hasNext() && i2.hasNext()) {
			if ((r = ((Comparable<? super Double>) i1.next()).compareTo(i2.next())) != 0)
				return r;
		}
		return i2.hasNext() ? -1 : (i1.hasNext() ? 1 : 0);
	}

	@Override
	public void push(double o) {
		add(o);
	}

	@Override
	public double popDouble() {
		if (isEmpty())
			throw new NoSuchElementException();
		return removeDouble(size64() - 1);
	}

	@Override
	public double topDouble() {
		if (isEmpty())
			throw new NoSuchElementException();
		return getDouble(size64() - 1);
	}

	@Override
	public double peekDouble(int i) {
		return getDouble(size64() - 1 - i);
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
	public boolean rem(double k) {
		long index = indexOf(k);
		if (index == -1)
			return false;
		removeDouble(index);
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
	public boolean addAll(final long index, final DoubleCollection c) {
		return addAll(index, (Collection<? extends Double>) c);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation delegates to the type-specific version of
	 * {@link #addAll(long, Collection)}.
	 */
	@Override
	public boolean addAll(final long index, final DoubleBigList l) {
		return addAll(index, (DoubleCollection) l);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation delegates to the type-specific version of
	 * {@link #addAll(long, Collection)}.
	 */
	@Override
	public boolean addAll(final DoubleCollection c) {
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
	public boolean addAll(final DoubleBigList l) {
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
	public void add(final long index, final Double ok) {
		add(index, ok.doubleValue());
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
	public Double set(final long index, final Double ok) {
		return Double.valueOf(set(index, ok.doubleValue()));
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
	public Double get(final long index) {
		return Double.valueOf(getDouble(index));
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
		return indexOf(((Double) (ok)).doubleValue());
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
		return lastIndexOf(((Double) (ok)).doubleValue());
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
	public Double remove(final long index) {
		return Double.valueOf(removeDouble(index));
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
	public void push(Double o) {
		push(o.doubleValue());
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
	public Double pop() {
		return Double.valueOf(popDouble());
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
	public Double top() {
		return Double.valueOf(topDouble());
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
	public Double peek(int i) {
		return Double.valueOf(peekDouble(i));
	}

	@Override
	public String toString() {
		final StringBuilder s = new StringBuilder();
		final DoubleIterator i = iterator();
		long n = size64();
		double k;
		boolean first = true;
		s.append("[");
		while (n-- != 0) {
			if (first)
				first = false;
			else
				s.append(", ");
			k = i.nextDouble();
			s.append(String.valueOf(k));
		}
		s.append("]");
		return s.toString();
	}

	/** A class implementing a sublist view. */
	public static class DoubleSubList extends AbstractDoubleBigList implements java.io.Serializable {
		private static final long serialVersionUID = -7046029254386353129L;
		/** The list this sublist restricts. */
		protected final DoubleBigList l;
		/** Initial (inclusive) index of this sublist. */
		protected final long from;
		/** Final (exclusive) index of this sublist. */
		protected long to;

		public DoubleSubList(final DoubleBigList l, final long from, final long to) {
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
		public boolean add(final double k) {
			l.add(to, k);
			to++;
			assert assertRange();
			return true;
		}

		@Override
		public void add(final long index, final double k) {
			ensureIndex(index);
			l.add(from + index, k);
			to++;
			assert assertRange();
		}

		@Override
		public boolean addAll(final long index, final Collection<? extends Double> c) {
			ensureIndex(index);
			to += c.size();
			return l.addAll(from + index, c);
		}

		@Override
		public double getDouble(long index) {
			ensureRestrictedIndex(index);
			return l.getDouble(from + index);
		}

		@Override
		public double removeDouble(long index) {
			ensureRestrictedIndex(index);
			to--;
			return l.removeDouble(from + index);
		}

		@Override
		public double set(long index, double k) {
			ensureRestrictedIndex(index);
			return l.set(from + index, k);
		}

		@Override
		public long size64() {
			return to - from;
		}

		@Override
		public void getElements(final long from, final double[][] a, final long offset, final long length) {
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
		public void addElements(final long index, final double a[][], long offset, long length) {
			ensureIndex(index);
			l.addElements(this.from + index, a, offset, length);
			this.to += length;
			assert assertRange();
		}

		@Override
		public DoubleBigListIterator listIterator(final long index) {
			ensureIndex(index);
			return new DoubleBigListIterator() {
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
				public double nextDouble() {
					if (!hasNext())
						throw new NoSuchElementException();
					return l.getDouble(from + (last = pos++));
				}

				@Override
				public double previousDouble() {
					if (!hasPrevious())
						throw new NoSuchElementException();
					return l.getDouble(from + (last = --pos));
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
				public void add(double k) {
					if (last == -1)
						throw new IllegalStateException();
					DoubleSubList.this.add(pos++, k);
					last = -1;
					assert assertRange();
				}

				@Override
				public void set(double k) {
					if (last == -1)
						throw new IllegalStateException();
					DoubleSubList.this.set(last, k);
				}

				@Override
				public void remove() {
					if (last == -1)
						throw new IllegalStateException();
					DoubleSubList.this.removeDouble(last);
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
		public DoubleBigList subList(final long from, final long to) {
			ensureIndex(from);
			ensureIndex(to);
			if (from > to)
				throw new IllegalArgumentException("Start index (" + from + ") is greater than end index (" + to + ")");
			return new DoubleSubList(this, from, to);
		}

		@Override
		public boolean rem(double k) {
			long index = indexOf(k);
			if (index == -1)
				return false;
			to--;
			l.removeDouble(from + index);
			assert assertRange();
			return true;
		}

		@Override
		public boolean addAll(final long index, final DoubleCollection c) {
			ensureIndex(index);
			return super.addAll(index, c);
		}

		@Override
		public boolean addAll(final long index, final DoubleBigList l) {
			ensureIndex(index);
			return super.addAll(index, l);
		}
	}
}
