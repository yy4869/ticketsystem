package org.ticketingsystem;

class Ticket{
	long tid;
	String passenger;
	int route;
	int coach;
	int seat;
	int departure;
	int arrival;
	public String toString() {
		return String.format("%s,%s,%s,%s,%s,%s,%s", tid, passenger, route, coach, seat, departure, arrival);
	}
}
public interface TicketingSystem {
	Ticket buyTicket(String passenger,int route,int departure,int arrival);
	int inquiry(int route,int departure,int arrival);
	boolean refundTicket(Ticket ticket);
	void debug(Ticket ticket);
}
