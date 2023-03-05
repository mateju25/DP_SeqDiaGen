package SLList;

public class SLList<T> {
	private SLLElement<T> head;
	private SLLElement<T> tail;

	public void tailInsert(T data) {
		SLLElement<T> element = new SLLElement<T>(data);
		
		if (head == null) {
			head = element;
			tail = head;
		}
		else {
			tail.setNext(element);
			tail = element;
		}
		
		tail.setNext(null);
	}
	
	public void print() {
		SLLElement<T> element = head;

		while (element != null) {
			System.out.println(element);
			element = element.getNext();
		}
	}

	public SLLElement<T> getHead() {
		return head;
	}

	public SLLElement<T> getNext(SLLElement<T> element) {
		return element.getNext();
	}
}
