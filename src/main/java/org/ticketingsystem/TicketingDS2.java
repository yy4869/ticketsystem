package org.ticketingsystem;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS2 implements TicketingSystem {
	private int routeNum;
	private int coachNum;
	private int seatNum;
	private int stationNum;
	private Long[][][] bits;
	private final long ALL_TRUE = 1L << 63 | ((1L << 63) - 1);
	private Random r = new Random();

	private static AtomicLong genId = new AtomicLong(0);

	private ConcurrentHashMap<TicketWrapper, Boolean> sales;

	public TicketingDS2() {
		this(5, 8, 100, 10);
	}

	public TicketingDS2(int routenum, int coachnum, int seatnum, int stationnum) {
		this.routeNum = routenum;
		this.coachNum = coachnum;
		this.seatNum = seatnum;
		this.stationNum = stationnum;
		bits = new Long[routenum][(coachnum * seatnum + 63) / 64][stationnum - 1];
		for (int i = 0; i < routenum; i++) {
			for (int j = 0; j < bits[i].length; j++) {
				for (int k = 0; k < bits[i][j].length; k++) {
					bits[i][j][k] = Long.valueOf(0);
					if (j == bits[i].length - 1 && bits[i].length * 64 != coachnum * seatnum) {
						bits[i][j][k] = (1L << (64 - bits[i].length * 64 + coachnum * seatnum) - 1) ^ ALL_TRUE;
					}
				}
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
		Long[][] lieche = bits[route - 1];
		long res = 0L;
		int index = 0;
		for (int i = 0, start = r.nextInt(lieche.length - 1); i < lieche.length; i++) {
			index = (i + start) % lieche.length;
			Long[] duan = lieche[index];
			res = 0L;
			synchronized (duan) {
				for (int j = departure - 1; j < arrival - 1; j++) {
					res = res | duan[j];
				}
				res = res ^ ALL_TRUE;
				if (res != 0) {
					res = Long.lowestOneBit(res);
					for (int j = departure - 1; j < arrival - 1; j++) {
						duan[j] = duan[j] | res;
					}
				}
			}
			if (res != 0)
				break;
		}

		if (res == 0)
			return null;

		index = (index << 6) - 1;
		while (res != 0) {
			res = res >>> 1;
			index++;
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
	public void debug(Ticket ticket) {
		TicketWrapper tw = new TicketWrapper(ticket);
		System.out.println(checkTicket(ticket));
		System.out.println(sales.containsKey(tw));
	}

	@Override
	public int inquiry(int route, int departure, int arrival) {
		if (!checkTicket(route, departure, arrival))
			return 0;
		Long[][] lieche = bits[route - 1];
		int ret = 0;
		for (int i = 0; i < lieche.length; i++) {
			long tmp = 0;
			for (int j = departure - 1; j < arrival - 1; j++) {
				tmp = tmp | lieche[i][j];
			}
			ret += 64 - Long.bitCount(tmp);
		}
		return ret;
	}

	@Override
	public boolean refundTicket(Ticket ticket) {
		if (!checkTicket(ticket))
			return false;
		TicketWrapper tw = new TicketWrapper(ticket);
		if (sales.containsKey(tw)) {
			sales.remove(tw);
			int index = (ticket.coach - 1) * seatNum + ticket.seat - 1;
			Long[] duan = bits[ticket.route - 1][index / 64];
			synchronized (duan) {
				for (int i = ticket.departure - 1; i < ticket.arrival - 1; i++) {
					duan[i] = duan[i] ^ (1L << (index % 64));
				}
			}
			return true;
		}
		return false;
	}

}
