package org.ticketingsystem;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Evaluate {

	public static int routeNum = 5;
	public static int coachNum = 8;
	public static int seatNum = 100;
	public static int stationNum = 10;

	public static void main(String[] args) throws InterruptedException {

		TicketingSystem tds = new TicketingDS2(routeNum, coachNum, seatNum, stationNum);
		int num = 16;
		int time = 1 * 60 * 1000;
		Passenger[] passengers = new Passenger[num];
		for (int i = 0; i < num; i++) {
			passengers[i] = new Passenger("name"+i,tds,time);
			passengers[i].start();
		}
		int count = 0;
		for (int i = 0; i < num;i ++){
			passengers[i].join();
			count += passengers[i].getCount();
		}
		System.out.println(time*1.0/count);
	}

	public static class Passenger extends Thread {
		private String name;
		private TicketingSystem tds;
		private int count;
		private long time;
		private List<TicketWrapper> tickets;

		public Passenger(String name, TicketingSystem tds, long time) {
			this.name = name;
			this.tds = tds;
			this.count = 0;
			this.time = time;
			this.tickets = new LinkedList<TicketWrapper>();
		}
		
		public int getCount(){
			return count;
		}

		@Override
		public void run() {
			Random r = new Random();
			long sumTime = 0;
			while (sumTime < time) {
				count++;
				int type = r.nextInt(9);
				if (type < 3) {
					long s = System.currentTimeMillis();
					Ticket ticket = tds.buyTicket(name, r.nextInt(routeNum), r.nextInt(stationNum),
							r.nextInt(stationNum));
					sumTime += System.currentTimeMillis() - s;
					if (ticket != null) {
						tickets.add(new TicketWrapper(ticket));
					}
				} else if (type == 3) {
					if (!tickets.isEmpty()) {
						Ticket ticket = tickets.get(0).toTicket();
						if(ticket.coach==9&&ticket.seat==1)System.out.println(ticket);
						if (r.nextBoolean()) {
							long s = System.currentTimeMillis();
							boolean flag = tds.refundTicket(ticket);
							sumTime += System.currentTimeMillis() - s;
							if (flag) {
								tickets.remove(0);
							} else {
								tds.debug(ticket);
								throw new IllegalStateException("退票异常.正确的票退失败");
							}
						} else {
							int tt = r.nextInt(5);
							if (tt == 1) {
								ticket.tid++;
							} else if (tt == 2) {
								ticket.coach++;
							} else if (tt == 3) {
								ticket.departure++;
							} else if (tt == 4) {
								ticket.seat++;
							} else {
								ticket.route++;
							}
							long s = System.currentTimeMillis();
							boolean flag = tds.refundTicket(ticket);
							sumTime += System.currentTimeMillis() - s;
							if (flag) {
								throw new IllegalStateException("退票异常.错误票退票成功");
							}
						}
					}
				} else {
					long s = System.currentTimeMillis();
					tds.inquiry(r.nextInt(routeNum), r.nextInt(stationNum), r.nextInt(stationNum));
					sumTime += System.currentTimeMillis() - s;
				}
			}
		}

	}

}
