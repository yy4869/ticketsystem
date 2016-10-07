package org.ticketingsystem;

public class TicketWrapper {
	private final long tid;
	private final String passenger;
	private final int route;
	private final int coach;
	private final int seat;
	private final int departure;
	private final int arrival;

	public TicketWrapper(Ticket ticket) {
		tid = ticket.tid;
		passenger = ticket.passenger;
		route = ticket.route;
		coach = ticket.coach;
		seat = ticket.seat;
		departure = ticket.departure;
		arrival = ticket.arrival;
	}

	@Override
	public boolean equals(Object object) {
		if (object == this)
			return true;
		TicketWrapper other = null;
		if (object instanceof TicketWrapper) {
			other = (TicketWrapper) object;
		} else {
			return false;
		}

		if (other.tid != tid)
			return false;
		if (other.route == route && other.coach == coach && other.seat == seat && other.arrival == arrival
				&& other.departure == departure)
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = (int) (tid % 1000000007L);
		return hashCode;
	}

	public String toString() {
		return String.format("%s,%s,%s,%s,%s,%s,%s", tid, passenger, route, coach, seat, departure, arrival);
	}
	
	public Ticket toTicket(){
		Ticket ticket = new Ticket();
		ticket.passenger = passenger;
		ticket.tid = tid;
		ticket.route = route;
		ticket.coach = coach;
		ticket.seat = seat;
		ticket.departure = departure;
		ticket.arrival = arrival;
		return ticket;
	}
}
