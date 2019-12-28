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

import java.util.List;

/**
 * A type-specific {@link List}; provides some additional methods that use
 * polymorphism to avoid (un)boxing.
 *
 * <p>
 * Note that this type-specific interface extends {@link Comparable}: it is
 * expected that implementing classes perform a lexicographical comparison using
 * the standard operator "less then" for primitive types, and the usual
 * {@link Comparable#compareTo(Object) compareTo()} method for objects.
 *
 * <p>
 * Additionally, this interface strengthens {@link #listIterator()},
 * {@link #listIterator(int)} and {@link #subList(int,int)}.
 *
 * <p>
 * Besides polymorphic methods, this interfaces specifies methods to copy into
 * an array or remove contiguous sublists. Although the abstract implementation
 * of this interface provides simple, one-by-one implementations of these
 * methods, it is expected that concrete implementation override them with
 * optimized versions.
 *
 * @see List
 */
public interface ShortList extends List<Short>, Comparable<List<? extends Short>>, ShortCollection {
	/**
	 * Returns a type-specific iterator on the elements of this list.
	 *
	 * <p>
	 * Note that this specification strengthens the one given in
	 * {@link List#iterator()}. It would not be normally necessary, but
	 * {@link java.lang.Iterable#iterator()} is bizarrily re-specified in
	 * {@link List}.
	 *
	 * @return an iterator on the elements of this list.
	 */
	@Override
	ShortListIterator iterator();

	/**
	 * Returns a type-specific list iterator on the list.
	 *
	 * @see List#listIterator()
	 */
	@Override
	ShortListIterator listIterator();

	/**
	 * Returns a type-specific list iterator on the list starting at a given index.
	 *
	 * @see List#listIterator(int)
	 */
	@Override
	ShortListIterator listIterator(int index);

	/**
	 * Returns a type-specific view of the portion of this list from the index
	 * {@code from}, inclusive, to the index {@code to}, exclusive.
	 *
	 * <p>
	 * Note that this specification strengthens the one given in
	 * {@link List#subList(int,int)}.
	 *
	 * @see List#subList(int,int)
	 */
	@Override
	ShortList subList(int from, int to);

	/**
	 * Sets the size of this list.
	 *
	 * <p>
	 * If the specified size is smaller than the current size, the last elements are
	 * discarded. Otherwise, they are filled with 0/{@code null}/{@code false}.
	 *
	 * @param size the new size.
	 */
	void size(int size);

	/**
	 * Copies (hopefully quickly) elements of this type-specific list into the given
	 * array.
	 *
	 * @param from   the start index (inclusive).
	 * @param a      the destination array.
	 * @param offset the offset into the destination array where to store the first
	 *               element copied.
	 * @param length the number of elements to be copied.
	 */
	void getElements(int from, short a[], int offset, int length);

	/**
	 * Removes (hopefully quickly) elements of this type-specific list.
	 *
	 * @param from the start index (inclusive).
	 * @param to   the end index (exclusive).
	 */
	void removeElements(int from, int to);

	/**
	 * Add (hopefully quickly) elements to this type-specific list.
	 *
	 * @param index the index at which to add elements.
	 * @param a     the array containing the elements.
	 */
	void addElements(int index, short a[]);

	/**
	 * Add (hopefully quickly) elements to this type-specific list.
	 *
	 * @param index  the index at which to add elements.
	 * @param a      the array containing the elements.
	 * @param offset the offset of the first element to add.
	 * @param length the number of elements to add.
	 */
	void addElements(int index, short a[], int offset, int length);

	/**
	 * Set (hopefully quickly) elements to match the array given.
	 * 
	 * @param a the array containing the elements.
	 * @since 8.3.0
	 */
	default void setElements(short a[]) {
		setElements(0, a);
	}

	/**
	 * Set (hopefully quickly) elements to match the array given.
	 * 
	 * @param index the index at which to start setting elements.
	 * @param a     the array containing the elements.
	 * @since 8.3.0
	 */
	default void setElements(int index, short a[]) {
		setElements(index, a, 0, a.length);
	}

	/**
	 * Set (hopefully quickly) elements to match the array given.
	 *
	 * Sets each in this list to the corresponding elements in the array, as if by
	 * 
	 * <pre>
	 * <code>
	 * ListIterator iter = listIterator(index);
	 * int i = 0;
	 * while (i < length) {
	 *   iter.next();
	 *   iter.set(a[offset + i++]);
	 * }
	 * </code>
	 * </pre>
	 * 
	 * However, the exact implementation may be more efficient, taking into account
	 * whether random access is faster or not, or at the discretion of subclasses,
	 * abuse internals.
	 *
	 * @param index  the index at which to start setting elements.
	 * @param a      the array containing the elements
	 * @param offset the offset of the first element to add.
	 * @param length the number of elements to add.
	 * @since 8.3.0
	 */
	default void setElements(int index, short a[], int offset, int length) {
		// We can't use AbstractList#ensureIndex, sadly.
		if (index < 0)
			throw new IndexOutOfBoundsException("Index (" + index + ") is negative");
		if (index > size())
			throw new IndexOutOfBoundsException("Index (" + index + ") is greater than list size (" + (size()) + ")");
		ShortArrays.ensureOffsetLength(a, offset, length);
		if (index + length > size())
			throw new IndexOutOfBoundsException(
					"End index (" + (index + length) + ") is greater than list size (" + size() + ")");
		ShortListIterator iter = listIterator(index);
		int i = 0;
		while (i < length) {
			iter.nextShort();
			iter.set(a[offset + i++]);
		}
	}

	/**
	 * Appends the specified element to the end of this list (optional operation).
	 * 
	 * @see List#add(Object)
	 */
	@Override
	boolean add(short key);

	/**
	 * Inserts the specified element at the specified position in this list
	 * (optional operation).
	 * 
	 * @see List#add(int,Object)
	 */
	void add(int index, short key);

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default void add(int index, Short key) {
		add(index, (key).shortValue());
	}

	/**
	 * Inserts all of the elements in the specified type-specific collection into
	 * this type-specific list at the specified position (optional operation).
	 * 
	 * @see List#addAll(int,java.util.Collection)
	 */
	boolean addAll(int index, ShortCollection c);

	/**
	 * Inserts all of the elements in the specified type-specific list into this
	 * type-specific list at the specified position (optional operation).
	 * 
	 * @see List#add(int,Object)
	 */
	boolean addAll(int index, ShortList c);

	/**
	 * Appends all of the elements in the specified type-specific list to the end of
	 * this type-specific list (optional operation).
	 * 
	 * @see List#add(int,Object)
	 */
	boolean addAll(ShortList c);

	/**
	 * Replaces the element at the specified position in this list with the
	 * specified element (optional operation).
	 * 
	 * @see List#set(int,Object)
	 */
	short set(int index, short k);

	/**
	 * Returns the element at the specified position in this list.
	 * 
	 * @see List#get(int)
	 */
	short getShort(int index);

	/**
	 * Returns the index of the first occurrence of the specified element in this
	 * list, or -1 if this list does not contain the element.
	 * 
	 * @see List#indexOf(Object)
	 */
	int indexOf(short k);

	/**
	 * Returns the index of the last occurrence of the specified element in this
	 * list, or -1 if this list does not contain the element.
	 * 
	 * @see List#lastIndexOf(Object)
	 */
	int lastIndexOf(short k);

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default boolean contains(final Object key) {
		return ShortCollection.super.contains(key);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default Short get(int index) {
		return Short.valueOf(getShort(index));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default int indexOf(Object o) {
		return indexOf(((Short) (o)).shortValue());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default int lastIndexOf(Object o) {
		return lastIndexOf(((Short) (o)).shortValue());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method specification is a workaround for
	 * <a href="http://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8177440">bug
	 * 8177440</a>.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default boolean add(Short k) {
		return add((k).shortValue());
	}

	/**
	 * Removes the element at the specified position in this list (optional
	 * operation).
	 * 
	 * @see List#remove(int)
	 */
	short removeShort(int index);

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default boolean remove(final Object key) {
		return ShortCollection.super.remove(key);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default Short remove(int index) {
		return Short.valueOf(removeShort(index));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default Short set(int index, Short k) {
		return Short.valueOf(set(index, (k).shortValue()));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default void sort(final java.util.Comparator<? super Short> comparator) {
		sort(ShortComparators.asShortComparator(comparator));
	}

	/**
	 * Sort a list using a type-specific comparator.
	 *
	 * <p>
	 * Pass {@code null} to sort using natural ordering.
	 * 
	 * @see List#sort(java.util.Comparator)
	 *
	 * @implSpec The default implementation dumps the elements into an array using
	 *           {@link #toArray()}, sorts the array, then replaces all elements
	 *           using the {@link #setElements} function.
	 *
	 * @implNote It is possible for this method to call {@link #unstableSort} if it
	 *           can determine that the results of a stable and unstable sort are
	 *           completely equivalent. This means if you override
	 *           {@link #unstableSort}, it should <em>not</em> call this method
	 *           unless you override this method as well.
	 *
	 * @since 8.3.0
	 */
	default void sort(final ShortComparator comparator) {
		if (comparator == null) {
			// For non-floating point primitive types, when comparing naturally,
			// it is impossible to tell the difference between a stable and not-stable sort.
			// So just use the probably faster unstable sort.
			unstableSort(comparator);
		} else {
			short[] elements = toShortArray();
			ShortArrays.stableSort(elements, comparator);
			setElements(elements);
		}
	}

	/**
	 * Sorts this list using a sort not assured to be stable.
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	default void unstableSort(final java.util.Comparator<? super Short> comparator) {
		unstableSort(ShortComparators.asShortComparator(comparator));
	}

	/**
	 * Sorts this list using a sort not assured to be stable.
	 *
	 * <p>
	 * Pass {@code null} to sort using natural ordering.
	 *
	 * <p>
	 * This differs from {@link List#sort(java.util.Comparator)} in that the results
	 * are not assured to be stable, but may be a bit faster.
	 *
	 * <p>
	 * Unless a subclass specifies otherwise, the results of the method if the list
	 * is concurrently modified during the sort are unspecified.
	 *
	 * @implSpec The default implementation dumps the elements into an array using
	 *           {@link #toArray()}, sorts the array, then replaces all elements
	 *           using the {@link #setElements} function.
	 *
	 * @since 8.3.0
	 */
	default void unstableSort(final ShortComparator comparator) {
		short[] elements = toShortArray();
		if (comparator == null) {
			ShortArrays.unstableSort(elements);
		} else {
			ShortArrays.unstableSort(elements, comparator);
		}
		setElements(elements);
	}
}
