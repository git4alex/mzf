package com.zonrong.core.log;

public class SyncQueue<E> {

	public SyncQueue(int size) {
		_array = new Object[size];
		_size = size;
		_oldest = 0;
		_next = 0;
	}

	public synchronized void put(E e) {
		while (full()) {
			try {
				wait();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
				// throw new ExceptionAdapter(ex);
			}
		}
		_array[_next] = e;
		_next = (_next + 1) % _size;
		notify();
	}

	public synchronized E get() {
		while (empty()) {
			try {
				wait();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
				// throw new ExceptionAdapter(ex);
			}
		}
		E ret = (E) _array[_oldest];
		_oldest = (_oldest + 1) % _size;
		notify();
		return ret;
	}

	protected boolean empty() {
		return _next == _oldest;
	}

	protected boolean full() {
		return (_next + 1) % _size == _oldest;
	}

	protected Object[] _array;

	protected int _next;

	protected int _oldest;

	protected int _size;
}