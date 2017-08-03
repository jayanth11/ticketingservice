package org.jay.ticketingservice.service.util;

public class PriorityQueueEntry<E extends Comparable<? super E>> implements
    Comparable<PriorityQueueEntry<E>> {

  final int priority;
  final E entry;

  public PriorityQueueEntry(E entry, int priority) {
    this.priority = priority;
    this.entry = entry;
  }

  public E getEntry() {
    return entry;
  }

  @Override
  public String toString() {
    return "PriorityQueueEntry [priority=" + priority + ", entry=" + entry + "]";
  }  

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((entry == null) ? 0 : entry.hashCode());
    result = prime * result + priority;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PriorityQueueEntry<E> other = (PriorityQueueEntry<E>) obj;
    if (entry == null) {
      if (other.entry != null)
        return false;
    } else if (!entry.equals(other.entry))
      return false;
    if (priority != other.priority)
      return false;
    return true;
  }

  public int compareTo(PriorityQueueEntry<E> other) {
    int result = 0;
    if(priority != other.priority) {
      result = priority < other.priority ? -1 : 1;
    } else {
      result = entry.compareTo(other.entry);
    }
    return result;
  }

}
