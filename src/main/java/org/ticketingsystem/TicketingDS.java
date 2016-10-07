package org.ticketingsystem;

import java.util.BitSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS implements TicketingSystem {
	private int routeNum;
	private int coachNum;
	private int seatNum;
	private int stationNum;
	private BitSet[][] routes;
	
	private static AtomicLong genId = new AtomicLong(0);

	private ConcurrentHashMap<TicketWrapper, Boolean> sales;

	public TicketingDS() {
		this(5, 8, 100, 10);
	}

	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum) {
		this.routeNum = routenum;
		this.coachNum = coachnum;
		this.seatNum = seatnum;
		this.stationNum = stationnum;
		routes = new BitSet[routenum][stationnum-1];
		for (int i = 0; i < routenum; i++) {
			for (int j = 0; j < stationnum-1; j++) {
				routes[i][j] = new BitSet(coachnum * seatnum);
			}
		}
		sales = new ConcurrentHashMap<TicketWrapper, Boolean>();
	}

	private boolean checkTicket(int route, int departure, int arrival) {
		if (route < 1 || route > routeNum)
			return false;
		if (departure > 0 && departure < arrival && arrival <= stationNum)
			return true;
		return false;
	}

	private boolean checkTicket(Ticket ticket) {
		if (!checkTicket(ticket.route, ticket.departure, ticket.arrival))
			return false;
		if (ticket.coach <= 0 || ticket.coach > coachNum)
			return false;
		if (ticket.seat <= 0 || ticket.seat > seatNum)
			return false;
		return true;
	}

	@Override
	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		if (null == passenger || "".equals(passenger))
			return null;
		if (!checkTicket(route, departure, arrival))
			return null;
		BitSet[] bitSets = routes[route - 1];
		BitSet res = new BitSet(coachNum * seatNum);
		int index = -1;
		synchronized (bitSets) {
			for (int i = departure - 1; i < arrival - 1; i++) {
				res.or(bitSets[i]);
			}

			for (int i = 0; i < coachNum * seatNum; i++) {
				if (!res.get(i)) {
					index = i;
					break;
				}
			}
			if (index == -1)
				return null;
			for (int i = departure - 1; i < arrival - 1; i++) {
				bitSets[i].set(index, true);
			}
		}

		Ticket ticket = new Ticket();
		ticket.tid = genId.incrementAndGet();
		ticket.passenger = passenger;
		ticket.route = route;
		ticket.coach = index / seatNum + 1;
		ticket.seat = index % seatNum + 1;
		ticket.departure = departure;
		ticket.arrival = arrival;
		TicketWrapper tw = new TicketWrapper(ticket);
		sales.put(tw, true);
		return ticket;
	}

	@Override
	public int inquiry(int route, int departure, int arrival) {
		if (!checkTicket(route, departure, arrival))
			return 0;
		BitSet[] bitSets = routes[route - 1];
		BitSet res = new BitSet(coachNum * seatNum);
		synchronized (bitSets) {
			for (int i = departure - 1; i < arrival - 1; i++) {
				res.or(bitSets[i]);
			}
		}
		return res.size() - res.cardinality();
	}

	@Override
	public boolean refundTicket(Ticket ticket) {
		if (!checkTicket(ticket))
			return false;
		TicketWrapper tw = new TicketWrapper(ticket);
		if (sales.containsKey(tw)) {
			sales.remove(tw);
			BitSet[] bitSets = routes[ticket.route - 1];
			int index = (ticket.coach - 1) * seatNum + ticket.seat - 1;
			synchronized (bitSets) {
				for (int i = ticket.departure - 1; i < ticket.arrival - 1; i++) {
					bitSets[i].set(index, false);
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void debug(Ticket ticket) {
		// TODO Auto-generated method stub
		
	}

}
