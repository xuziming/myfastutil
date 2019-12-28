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
package it.unimi.dsi.fastutil.booleans;

import static it.unimi.dsi.fastutil.BigArrays.grow;
import static it.unimi.dsi.fastutil.BigArrays.length;
import static it.unimi.dsi.fastutil.BigArrays.set;
import static it.unimi.dsi.fastutil.BigArrays.trim;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A class providing static methods and objects that do useful things with
 * type-specific iterators.
 *
 * @see Iterator
 */
public final class BooleanIterators {
	private BooleanIterators() {
	}

	/**
	 * A class returning no elements and a type-specific iterator interface.
	 *
	 * <p>
	 * This class may be useful to implement your own in case you subclass a
	 * type-specific iterator.
	 */
	public static class EmptyIterator implements BooleanListIterator, java.io.Serializable, Cloneable {
		private static final long serialVersionUID = -7046029254386353129L;

		protected EmptyIterator() {
		}

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public boolean hasPrevious() {
			return false;
		}

		@Override
		public boolean nextBoolean() {
			throw new NoSuchElementException();
		}

		@Override
		public boolean previousBoolean() {
			throw new NoSuchElementException();
		}

		@Override
		public int nextIndex() {
			return 0;
		}

		@Override
		public int previousIndex() {
			return -1;
		}

		@Override
		public int skip(int n) {
			return 0;
		};

		@Override
		public int back(int n) {
			return 0;
		};

		@Override
		public Object clone() {
			return EMPTY_ITERATOR;
		}

		private Object readResolve() {
			return EMPTY_ITERATOR;
		}
	}

	/**
	 * An empty iterator. It is serializable and cloneable.
	 *
	 * <p>
	 * The class of this objects represent an abstract empty iterator that can
	 * iterate as a type-specific (list) iterator.
	 */

	public static final EmptyIterator EMPTY_ITERATOR = new EmptyIterator();

	/** An iterator returning a single element. */
	private static class SingletonIterator implements BooleanListIterator {
		private final boolean element;
		private int curr;

		public SingletonIterator(final boolean element) {
			this.element = element;
		}

		@Override
		public boolean hasNext() {
			return curr == 0;
		}

		@Override
		public boolean hasPrevious() {
			return curr == 1;
		}

		@Override
		public boolean nextBoolean() {
			if (!hasNext())
				throw new NoSuchElementException();
			curr = 1;
			return element;
		}

		@Override
		public boolean previousBoolean() {
			if (!hasPrevious())
				throw new NoSuchElementException();
			curr = 0;
			return element;
		}

		@Override
		public int nextIndex() {
			return curr;
		}

		@Override
		public int previousIndex() {
			return curr - 1;
		}
	}

	/**
	 * Returns an immutable iterator that iterates just over the given element.
	 *
	 * @param element the only element to be returned by a type-specific list
	 *                iterator.
	 * @return an immutable iterator that iterates just over {@code element}.
	 */
	public static BooleanListIterator singleton(final boolean element) {
		return new SingletonIterator(element);
	}

	/** A class to wrap arrays in iterators. */
	private static class ArrayIterator implements BooleanListIterator {
		private final boolean[] array;
		private final int offset, length;
		private int curr;

		public ArrayIterator(final boolean[] array, final int offset, final int length) {
			this.array = array;
			this.offset = offset;
			this.length = length;
		}

		@Override
		public boolean hasNext() {
			return curr < length;
		}

		@Override
		public boolean hasPrevious() {
			return curr > 0;
		}

		@Override
		public boolean nextBoolean() {
			if (!hasNext())
				throw new NoSuchElementException();
			return array[offset + curr++];
		}

		@Override
		public boolean previousBoolean() {
			if (!hasPrevious())
				throw new NoSuchElementException();
			return array[offset + --curr];
		}

		@Override
		public int skip(int n) {
			if (n <= length - curr) {
				curr += n;
				return n;
			}
			n = length - curr;
			curr = length;
			return n;
		}

		@Override
		public int back(int n) {
			if (n <= curr) {
				curr -= n;
				return n;
			}
			n = curr;
			curr = 0;
			return n;
		}

		@Override
		public int nextIndex() {
			return curr;
		}

		@Override
		public int previousIndex() {
			return curr - 1;
		}
	}

	/**
	 * Wraps the given part of an array into a type-specific list iterator.
	 *
	 * <p>
	 * The type-specific list iterator returned by this method will iterate
	 * {@code length} times, returning consecutive elements of the given array
	 * starting from the one with index {@code offset}.
	 *
	 * @param array  an array to wrap into a type-specific list iterator.
	 * @param offset the first element of the array to be returned.
	 * @param length the number of elements to return.
	 * @return an iterator that will return {@code length} elements of {@code array}
	 *         starting at position {@code offset}.
	 */
	public static BooleanListIterator wrap(final boolean[] array, final int offset, final int length) {
		BooleanArrays.ensureOffsetLength(array, offset, length);
		return new ArrayIterator(array, offset, length);
	}

	/**
	 * Wraps the given array into a type-specific list iterator.
	 *
	 * <p>
	 * The type-specific list iterator returned by this method will return all
	 * elements of the given array.
	 *
	 * @param array an array to wrap into a type-specific list iterator.
	 * @return an iterator that will the elements of {@code array}.
	 */
	public static BooleanListIterator wrap(final boolean[] array) {
		return new ArrayIterator(array, 0, array.length);
	}

	/**
	 * Unwraps an iterator into an array starting at a given offset for a given
	 * number of elements.
	 *
	 * <p>
	 * This method iterates over the given type-specific iterator and stores the
	 * elements returned, up to a maximum of {@code length}, in the given array
	 * starting at {@code offset}. The number of actually unwrapped elements is
	 * returned (it may be less than {@code max} if the iterator emits less than
	 * {@code max} elements).
	 *
	 * @param i      a type-specific iterator.
	 * @param array  an array to contain the output of the iterator.
	 * @param offset the first element of the array to be returned.
	 * @param max    the maximum number of elements to unwrap.
	 * @return the number of elements unwrapped.
	 */
	public static int unwrap(final BooleanIterator i, final boolean array[], int offset, final int max) {
		if (max < 0)
			throw new IllegalArgumentException("The maximum number of elements (" + max + ") is negative");
		if (offset < 0 || offset + max > array.length)
			throw new IllegalArgumentException();
		int j = max;
		while (j-- != 0 && i.hasNext())
			array[offset++] = i.nextBoolean();
		return max - j - 1;
	}

	/**
	 * Unwraps an iterator into an array.
	 *
	 * <p>
	 * This method iterates over the given type-specific iterator and stores the
	 * elements returned in the given array. The iteration will stop when the
	 * iterator has no more elements or when the end of the array has been reached.
	 *
	 * @param i     a type-specific iterator.
	 * @param array an array to contain the output of the iterator.
	 * @return the number of elements unwrapped.
	 */
	public static int unwrap(final BooleanIterator i, final boolean array[]) {
		return unwrap(i, array, 0, array.length);
	}

	/**
	 * Unwraps an iterator, returning an array, with a limit on the number of
	 * elements.
	 *
	 * <p>
	 * This method iterates over the given type-specific iterator and returns an
	 * array containing the elements returned by the iterator. At most {@code max}
	 * elements will be returned.
	 *
	 * @param i   a type-specific iterator.
	 * @param max the maximum number of elements to be unwrapped.
	 * @return an array containing the elements returned by the iterator (at most
	 *         {@code max}).
	 */

	public static boolean[] unwrap(final BooleanIterator i, int max) {
		if (max < 0)
			throw new IllegalArgumentException("The maximum number of elements (" + max + ") is negative");
		boolean array[] = new boolean[16];
		int j = 0;
		while (max-- != 0 && i.hasNext()) {
			if (j == array.length)
				array = BooleanArrays.grow(array, j + 1);
			array[j++] = i.nextBoolean();
		}
		return BooleanArrays.trim(array, j);
	}

	/**
	 * Unwraps an iterator, returning an array.
	 *
	 * <p>
	 * This method iterates over the given type-specific iterator and returns an
	 * array containing the elements returned by the iterator.
	 *
	 * @param i a type-specific iterator.
	 * @return an array containing the elements returned by the iterator.
	 */
	public static boolean[] unwrap(final BooleanIterator i) {
		return unwrap(i, Integer.MAX_VALUE);
	}

	/**
	 * Unwraps an iterator into a big array starting at a given offset for a given
	 * number of elements.
	 *
	 * <p>
	 * This method iterates over the given type-specific iterator and stores the
	 * elements returned, up to a maximum of {@code length}, in the given big array
	 * starting at {@code offset}. The number of actually unwrapped elements is
	 * returned (it may be less than {@code max} if the iterator emits less than
	 * {@code max} elements).
	 *
	 * @param i      a type-specific iterator.
	 * @param array  a big array to contain the output of the iterator.
	 * @param offset the first element of the array to be returned.
	 * @param max    the maximum number of elements to unwrap.
	 * @return the number of elements unwrapped.
	 */
	public static long unwrap(final BooleanIterator i, final boolean array[][], long offset, final long max) {
		if (max < 0)
			throw new IllegalArgumentException("The maximum number of elements (" + max + ") is negative");
		if (offset < 0 || offset + max > length(array))
			throw new IllegalArgumentException();
		long j = max;
		while (j-- != 0 && i.hasNext())
			set(array, offset++, i.nextBoolean());
		return max - j - 1;
	}

	/**
	 * Unwraps an iterator into a big array.
	 *
	 * <p>
	 * This method iterates over the given type-specific iterator and stores the
	 * elements returned in the given big array. The iteration will stop when the
	 * iterator has no more elements or when the end of the array has been reached.
	 *
	 * @param i     a type-specific iterator.
	 * @param array a big array to contain the output of the iterator.
	 * @return the number of elements unwrapped.
	 */
	public static long unwrap(final BooleanIterator i, final boolean array[][]) {
		return unwrap(i, array, 0, length(array));
	}

	/**
	 * Unwraps an iterator into a type-specific collection, with a limit on the
	 * number of elements.
	 *
	 * <p>
	 * This method iterates over the given type-specific iterator and stores the
	 * elements returned, up to a maximum of {@code max}, in the given type-specific
	 * collection. The number of actually unwrapped elements is returned (it may be
	 * less than {@code max} if the iterator emits less than {@code max} elements).
	 *
	 * @param i   a type-specific iterator.
	 * @param c   a type-specific collection array to contain the output of the
	 *            iterator.
	 * @param max the maximum number of elements to unwrap.
	 * @return the number of elements unwrapped. Note that this is the number of
	 *         elements returned by the iterator, which is not necessarily the
	 *         number of elements that have been added to the collection (because of
	 *         duplicates).
	 */
	public static int unwrap(final BooleanIterator i, final BooleanCollection c, final int max) {
		if (max < 0)
			throw new IllegalArgumentException("The maximum number of elements (" + max + ") is negative");
		int j = max;
		while (j-- != 0 && i.hasNext())
			c.add(i.nextBoolean());
		return max - j - 1;
	}

	/**
	 * Unwraps an iterator, returning a big array, with a limit on the number of
	 * elements.
	 *
	 * <p>
	 * This method iterates over the given type-specific iterator and returns a big
	 * array containing the elements returned by the iterator. At most {@code max}
	 * elements will be returned.
	 *
	 * @param i   a type-specific iterator.
	 * @param max the maximum number of elements to be unwrapped.
	 * @return a big array containing the elements returned by the iterator (at most
	 *         {@code max}).
	 */

	public static boolean[][] unwrapBig(final BooleanIterator i, long max) {
		if (max < 0)
			throw new IllegalArgumentException("The maximum number of elements (" + max + ") is negative");
		boolean array[][] = BooleanBigArrays.newBigArray(16);
		long j = 0;
		while (max-- != 0 && i.hasNext()) {
			if (j == length(array))
				array = grow(array, j + 1);
			set(array, j++, i.nextBoolean());
		}
		return trim(array, j);
	}

	/**
	 * Unwraps an iterator, returning a big array.
	 *
	 * <p>
	 * This method iterates over the given type-specific iterator and returns a big
	 * array containing the elements returned by the iterator.
	 *
	 * @param i a type-specific iterator.
	 * @return a big array containing the elements returned by the iterator.
	 */
	public static boolean[][] unwrapBig(final BooleanIterator i) {
		return unwrapBig(i, Long.MAX_VALUE);
	}

	/**
	 * Unwraps an iterator into a type-specific collection.
	 *
	 * <p>
	 * This method iterates over the given type-specific iterator and stores the
	 * elements returned in the given type-specific collection. The returned count
	 * on the number unwrapped elements is a long, so that it will work also with
	 * very large collections.
	 *
	 * @param i a type-specific iterator.
	 * @param c a type-specific collection to contain the output of the iterator.
	 * @return the number of elements unwrapped. Note that this is the number of
	 *         elements returned by the iterator, which is not necessarily the
	 *         number of elements that have been added to the collection (because of
	 *         duplicates).
	 */
	public static long unwrap(final BooleanIterator i, final BooleanCollection c) {
		long n = 0;
		while (i.hasNext()) {
			c.add(i.nextBoolean());
			n++;
		}
		return n;
	}

	/**
	 * Pours an iterator into a type-specific collection, with a limit on the number
	 * of elements.
	 *
	 * <p>
	 * This method iterates over the given type-specific iterator and adds the
	 * returned elements to the given collection (up to {@code max}).
	 *
	 * @param i   a type-specific iterator.
	 * @param s   a type-specific collection.
	 * @param max the maximum number of elements to be poured.
	 * @return the number of elements poured. Note that this is the number of
	 *         elements returned by the iterator, which is not necessarily the
	 *         number of elements that have been added to the collection (because of
	 *         duplicates).
	 */
	public static int pour(final BooleanIterator i, final BooleanCollection s, final int max) {
		if (max < 0)
			throw new IllegalArgumentException("The maximum number of elements (" + max + ") is negative");
		int j = max;
		while (j-- != 0 && i.hasNext())
			s.add(i.nextBoolean());
		return max - j - 1;
	}

	/**
	 * Pours an iterator into a type-specific collection.
	 *
	 * <p>
	 * This method iterates over the given type-specific iterator and adds the
	 * returned elements to the given collection.
	 *
	 * @param i a type-specific iterator.
	 * @param s a type-specific collection.
	 * @return the number of elements poured. Note that this is the number of
	 *         elements returned by the iterator, which is not necessarily the
	 *         number of elements that have been added to the collection (because of
	 *         duplicates).
	 */
	public static int pour(final BooleanIterator i, final BooleanCollection s) {
		return pour(i, s, Integer.MAX_VALUE);
	}

	/**
	 * Pours an iterator, returning a type-specific list, with a limit on the number
	 * of elements.
	 *
	 * <p>
	 * This method iterates over the given type-specific iterator and returns a
	 * type-specific list containing the returned elements (up to {@code max}).
	 * Iteration on the returned list is guaranteed to produce the elements in the
	 * same order in which they appeared in the iterator.
	 *
	 *
	 * @param i   a type-specific iterator.
	 * @param max the maximum number of elements to be poured.
	 * @return a type-specific list containing the returned elements, up to
	 *         {@code max}.
	 */
	public static BooleanList pour(final BooleanIterator i, int max) {
		final BooleanArrayList l = new BooleanArrayList();
		pour(i, l, max);
		l.trim();
		return l;
	}

	/**
	 * Pours an iterator, returning a type-specific list.
	 *
	 * <p>
	 * This method iterates over the given type-specific iterator and returns a list
	 * containing the returned elements. Iteration on the returned list is
	 * guaranteed to produce the elements in the same order in which they appeared
	 * in the iterator.
	 *
	 * @param i a type-specific iterator.
	 * @return a type-specific list containing the returned elements.
	 */
	public static BooleanList pour(final BooleanIterator i) {
		return pour(i, Integer.MAX_VALUE);
	}

	private static class IteratorWrapper implements BooleanIterator {
		final Iterator<Boolean> i;

		public IteratorWrapper(final Iterator<Boolean> i) {
			this.i = i;
		}

		@Override
		public boolean hasNext() {
			return i.hasNext();
		}

		@Override
		public void remove() {
			i.remove();
		}

		@Override
		public boolean nextBoolean() {
			return (i.next()).booleanValue();
		}
	}

	/**
	 * Wraps a standard iterator into a type-specific iterator.
	 *
	 * <p>
	 * This method wraps a standard iterator into a type-specific one which will
	 * handle the type conversions for you. Of course, any attempt to wrap an
	 * iterator returning the instances of the wrong class will generate a
	 * {@link ClassCastException}. The returned iterator is backed by {@code i}:
	 * changes to one of the iterators will affect the other, too.
	 *
	 * <p>
	 * If {@code i} is already type-specific, it will returned and no new object
	 * will be generated.
	 *
	 * @param i an iterator.
	 * @return a type-specific iterator backed by {@code i}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static BooleanIterator asBooleanIterator(final Iterator i) {
		if (i instanceof BooleanIterator)
			return (BooleanIterator) i;
		return new IteratorWrapper(i);
	}

	private static class ListIteratorWrapper implements BooleanListIterator {
		final ListIterator<Boolean> i;

		public ListIteratorWrapper(final ListIterator<Boolean> i) {
			this.i = i;
		}

		@Override
		public boolean hasNext() {
			return i.hasNext();
		}

		@Override
		public boolean hasPrevious() {
			return i.hasPrevious();
		}

		@Override
		public int nextIndex() {
			return i.nextIndex();
		}

		@Override
		public int previousIndex() {
			return i.previousIndex();
		}

		@Override
		public void set(boolean k) {
			i.set(Boolean.valueOf(k));
		}

		@Override
		public void add(boolean k) {
			i.add(Boolean.valueOf(k));
		}

		@Override
		public void remove() {
			i.remove();
		}

		@Override
		public boolean nextBoolean() {
			return (i.next()).booleanValue();
		}

		@Override
		public boolean previousBoolean() {
			return (i.previous()).booleanValue();
		}
	}

	/**
	 * Wraps a standard list iterator into a type-specific list iterator.
	 *
	 * <p>
	 * This method wraps a standard list iterator into a type-specific one which
	 * will handle the type conversions for you. Of course, any attempt to wrap an
	 * iterator returning the instances of the wrong class will generate a
	 * {@link ClassCastException}. The returned iterator is backed by {@code i}:
	 * changes to one of the iterators will affect the other, too.
	 *
	 * <p>
	 * If {@code i} is already type-specific, it will returned and no new object
	 * will be generated.
	 *
	 * @param i a list iterator.
	 * @return a type-specific list iterator backed by {@code i}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static BooleanListIterator asBooleanIterator(final ListIterator i) {
		if (i instanceof BooleanListIterator)
			return (BooleanListIterator) i;
		return new ListIteratorWrapper(i);
	}

	public static boolean any(final BooleanIterator iterator,
			final java.util.function.Predicate<? super Boolean> predicate) {
		return indexOf(iterator, predicate) != -1;
	}

	public static boolean all(final BooleanIterator iterator,
			final java.util.function.Predicate<? super Boolean> predicate) {
		Objects.requireNonNull(predicate);
		do {
			if (!iterator.hasNext())
				return true;
		} while (predicate.test(Boolean.valueOf(iterator.nextBoolean())));
		return false;
	}

	public static int indexOf(final BooleanIterator iterator,
			final java.util.function.Predicate<? super Boolean> predicate) {
		Objects.requireNonNull(predicate);
		for (int i = 0; iterator.hasNext(); ++i) {
			if (predicate.test(Boolean.valueOf(iterator.nextBoolean())))
				return i;
		}
		return -1;
	}

	private static class IteratorConcatenator implements BooleanIterator {
		final BooleanIterator a[];
		int offset, length, lastOffset = -1;

		public IteratorConcatenator(final BooleanIterator a[], int offset, int length) {
			this.a = a;
			this.offset = offset;
			this.length = length;
			advance();
		}

		private void advance() {
			while (length != 0) {
				if (a[offset].hasNext())
					break;
				length--;
				offset++;
			}
			return;
		}

		@Override
		public boolean hasNext() {
			return length > 0;
		}

		@Override
		public boolean nextBoolean() {
			if (!hasNext())
				throw new NoSuchElementException();
			boolean next = a[lastOffset = offset].nextBoolean();
			advance();
			return next;
		}

		@Override
		public void remove() {
			if (lastOffset == -1)
				throw new IllegalStateException();
			a[lastOffset].remove();
		}

		@Override
		public int skip(int n) {
			lastOffset = -1;
			int skipped = 0;
			while (skipped < n && length != 0) {
				skipped += a[offset].skip(n - skipped);
				if (a[offset].hasNext())
					break;
				length--;
				offset++;
			}
			return skipped;
		}
	}

	/**
	 * Concatenates all iterators contained in an array.
	 *
	 * <p>
	 * This method returns an iterator that will enumerate in order the elements
	 * returned by all iterators contained in the given array.
	 *
	 * @param a an array of iterators.
	 * @return an iterator obtained by concatenation.
	 */
	public static BooleanIterator concat(final BooleanIterator a[]) {
		return concat(a, 0, a.length);
	}

	/**
	 * Concatenates a sequence of iterators contained in an array.
	 *
	 * <p>
	 * This method returns an iterator that will enumerate in order the elements
	 * returned by {@code a[offset]}, then those returned by {@code a[offset + 1]},
	 * and so on up to {@code a[offset + length - 1]}.
	 *
	 * @param a      an array of iterators.
	 * @param offset the index of the first iterator to concatenate.
	 * @param length the number of iterators to concatenate.
	 * @return an iterator obtained by concatenation of {@code length} elements of
	 *         {@code a} starting at {@code offset}.
	 */
	public static BooleanIterator concat(final BooleanIterator a[], final int offset, final int length) {
		return new IteratorConcatenator(a, offset, length);
	}

	/** An unmodifiable wrapper class for iterators. */
	public static class UnmodifiableIterator implements BooleanIterator {
		protected final BooleanIterator i;

		public UnmodifiableIterator(final BooleanIterator i) {
			this.i = i;
		}

		@Override
		public boolean hasNext() {
			return i.hasNext();
		}

		@Override
		public boolean nextBoolean() {
			return i.nextBoolean();
		}
	}

	/**
	 * Returns an unmodifiable iterator backed by the specified iterator.
	 *
	 * @param i the iterator to be wrapped in an unmodifiable iterator.
	 * @return an unmodifiable view of the specified iterator.
	 */
	public static BooleanIterator unmodifiable(final BooleanIterator i) {
		return new UnmodifiableIterator(i);
	}

	/** An unmodifiable wrapper class for bidirectional iterators. */
	public static class UnmodifiableBidirectionalIterator implements BooleanBidirectionalIterator {
		protected final BooleanBidirectionalIterator i;

		public UnmodifiableBidirectionalIterator(final BooleanBidirectionalIterator i) {
			this.i = i;
		}

		@Override
		public boolean hasNext() {
			return i.hasNext();
		}

		@Override
		public boolean hasPrevious() {
			return i.hasPrevious();
		}

		@Override
		public boolean nextBoolean() {
			return i.nextBoolean();
		}

		@Override
		public boolean previousBoolean() {
			return i.previousBoolean();
		}
	}

	/**
	 * Returns an unmodifiable bidirectional iterator backed by the specified
	 * bidirectional iterator.
	 *
	 * @param i the bidirectional iterator to be wrapped in an unmodifiable
	 *          bidirectional iterator.
	 * @return an unmodifiable view of the specified bidirectional iterator.
	 */
	public static BooleanBidirectionalIterator unmodifiable(final BooleanBidirectionalIterator i) {
		return new UnmodifiableBidirectionalIterator(i);
	}

	/** An unmodifiable wrapper class for list iterators. */
	public static class UnmodifiableListIterator implements BooleanListIterator {
		protected final BooleanListIterator i;

		public UnmodifiableListIterator(final BooleanListIterator i) {
			this.i = i;
		}

		@Override
		public boolean hasNext() {
			return i.hasNext();
		}

		@Override
		public boolean hasPrevious() {
			return i.hasPrevious();
		}

		@Override
		public boolean nextBoolean() {
			return i.nextBoolean();
		}

		@Override
		public boolean previousBoolean() {
			return i.previousBoolean();
		}

		@Override
		public int nextIndex() {
			return i.nextIndex();
		}

		@Override
		public int previousIndex() {
			return i.previousIndex();
		}
	}

	/**
	 * Returns an unmodifiable list iterator backed by the specified list iterator.
	 *
	 * @param i the list iterator to be wrapped in an unmodifiable list iterator.
	 * @return an unmodifiable view of the specified list iterator.
	 */
	public static BooleanListIterator unmodifiable(final BooleanListIterator i) {
		return new UnmodifiableListIterator(i);
	}
}
