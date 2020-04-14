import java.io.*;
import java.util.*;

public class Run
{
	private static Scanner scanner = null;
	private static Algorithm algo = null;
	private static int proc_count;
	private static Queue<Process> q = new LinkedList<Process>();

	public static void main(String[] args)
	{
		int rrtq = open();
		populateQ();

		switch(algo)
		{
			case RR:
				Scheduler.RoundRobin(q, rrtq, proc_count);
				break;
			case SJF:
			case PR_noPREMP:
				Scheduler.noPremp(q, proc_count, algo);
				break;
			case PR_withPREMP:
				Scheduler.Priority_withPremp(q, proc_count);
				break;
		}

		GanttChart.close();
	}

	private static int open()
	{
		File in;
		int rrtq = 0;

		try
		{
			in = new File("input.txt");
			scanner = new Scanner(in);
		}
		catch(Exception e)
		{
			System.err.println("Error: Could not open input.txt for reading\n");
			System.err.println(e);
			e.printStackTrace();
			System.exit(1);
		}

		String token = scanner.next();

		if (token.equals("RR"))
		{
			algo = Algorithm.RR;
		}
		else if (token.equals("SJF"))
		{
			algo = Algorithm.SJF;
		}
		else if (token.equals("PR_noPREMP"))
		{
			algo = Algorithm.PR_noPREMP;
		}
		else if (token.equals("PR_withPREMP"))
		{
			algo = Algorithm.PR_withPREMP;
		}
		else {
			System.err.println("Error: Input file contained unidentified CPU Scheduling algorithm\n");
			System.exit(1);
		}

		if ( algo == Algorithm.RR )
		{
			rrtq = scanner.nextInt();
		}
		proc_count = scanner.nextInt();

		return rrtq;
	}

	private static void populateQ()
	{
		for (int i = 0; i < proc_count; i++){
			int proc_number 	= 	scanner.nextInt();
			int arrival_time 	= 	scanner.nextInt();
			int cpu_burst 		= 	scanner.nextInt();
			int priority 		= 	scanner.nextInt();
			Process p = new Process(proc_number, arrival_time, cpu_burst, priority);
			q.add(p);
		}
	}
}

enum Algorithm
{
	RR,
	SJF,
	PR_noPREMP,
	PR_withPREMP
}

class Process
{
	// I didn't use private fields.
	// Public fields made it was easier to code and read my algorithms.
	// And frankly I don't believe it really matters for this assignment
	public final int proc_number;
	public final int arrival_time;
	public final int cpu_burst;
	public final int priority;

	public int time_remaining;
	public int wait_time;

	Process(int p, int a, int c, int pr)
	{
		proc_number 	= 	p;
		arrival_time 	= 	a;
		cpu_burst 		= 	c;
		priority 		= 	pr;
		time_remaining 	= 	cpu_burst;
		wait_time		= 	0;
	}

	public String toString()
	{
		return "Process: " + proc_number + "\nArrival Time: " + arrival_time + "\nCPU Burst: " + cpu_burst + "\nPriority: " + priority + "\nWait Time: " + wait_time + "\n"
				+ "\nTime Remaining: " + time_remaining;
	}

}

class Scheduler
{
	public static void Priority_withPremp(Queue<Process> hold, int proc_count)
	{
		GanttChart.printAlgo(Algorithm.PR_withPREMP);

		PriorityQueue<Process> pq = new PriorityQueue<Process>(proc_count, new PriorityComparator());

		Process p;
		int p_num;
		boolean preempted;

		int procs_left  = 	proc_count;
		int runTime 	= 	0;
		double waitTime = 	0.0;


		getArrivingProcs(pq, hold, procs_left, runTime);

		while ( procs_left > 0 ){
			preempted = false;
			p = pq.remove();
			p_num = p.proc_number;

			GanttChart.printGanttLn(runTime, p_num);

			while (p.time_remaining != 0 && !preempted){
				p.time_remaining--;
				runTime++;

				getArrivingProcs(pq, hold, procs_left, runTime);

				if (!pq.isEmpty()) {
					if (pq.peek().priority < p.priority && p.time_remaining != 0) {
						preempted = true;
						pq.add(p);
					}
				}
			}

			if (p.time_remaining == 0)
			{
				p.wait_time = runTime - p.cpu_burst - p.arrival_time;
				waitTime += p.wait_time;
				procs_left--;
			}
		}

		GanttChart.printAvgWaitTime(waitTime / proc_count);
	}

	public static void noPremp(Queue<Process> hold, int proc_count, Algorithm algo)
	{
		GanttChart.printAlgo(algo);

		PriorityQueue<Process> pq = new PriorityQueue<Process>(
									proc_count,
									algo == Algorithm.SJF ? new SJFComparator() : new PriorityComparator());

		int procs_left = proc_count;
		Process p;
		int p_num;
		int runTime = 0;
		double waitTime = 0;

		getArrivingProcs(pq, hold, procs_left, runTime);

		while ( procs_left > 0 ){
			// If pq is empty, processes haven't arrived at runTime yet.
			// procs_left > 0 so processes so there are processes in hold queue
			// Loop until a process is put into the queue.
			// NOTE: This is not relevant to a test_case provided by Professor.
			while (pq.isEmpty()){
				runTime++;
				getArrivingProcs(pq, hold, procs_left, runTime);
			}

			// Get the process requesting cpu
			p = pq.remove();
			p_num = p.proc_number;

			// Print the process on the cpu
			GanttChart.printGanttLn(runTime, p_num);

			// Process gets CPU until its burst is completed
			while ( p.time_remaining != 0){
				p.time_remaining--;
				runTime++;
			}

			getArrivingProcs(pq, hold, procs_left, runTime);

			// Calculate wait time.
			// The wait_time field of process is unnecessary for calculation, but useful for debugging
			p.wait_time = runTime - p.cpu_burst - p.arrival_time;
			waitTime += p.wait_time;

			// A process has completed it's CPU burst, so we denote there is one less process that needs the CPU
			procs_left--;
		}

		GanttChart.printAvgWaitTime(waitTime / proc_count);
	}

	private static class PriorityComparator implements Comparator<Process>
	{
		public int compare (Process p1, Process p2){
			int ret = ((Integer) p1.priority).compareTo((Integer) p2.priority);
			if (ret == 0){ // Priority tie
				ret = ((Integer) p1.proc_number).compareTo((Integer) p2.proc_number);
			}
			return ret;
		}
	}

	private static void getArrivingProcs(PriorityQueue<Process> pq, Queue<Process> hold, int procs_left, int runTime)
	{
		for (int i = 0; i < procs_left && !hold.isEmpty(); i++){
			Process temp = hold.remove();
			if (temp.arrival_time <= runTime){
				pq.add(temp);
			} else {
				hold.add(temp);
			}
		}
	}

	private static class SJFComparator implements Comparator<Process>
	{
		public int compare (Process p1, Process p2){
			int ret = ((Integer) p1.cpu_burst).compareTo((Integer) p2.cpu_burst);
			if (ret == 0){ // CPU burst length tie
				ret = ((Integer) p1.arrival_time).compareTo((Integer) p2.arrival_time);
			}
			if (ret == 0){ // Arrival time tie
				ret = ((Integer) p1.proc_number).compareTo((Integer) p2.proc_number);
			}
			return ret;
		}
	}

	public static void RoundRobin(Queue<Process> next_q, int rrtq, int proc_count)
	{
		Queue<Process> q = new LinkedList<Process>();

		// Print header to output file
		GanttChart.printAlgo(Algorithm.RR, rrtq);

		Process p;
		int p_num;
		Process next = null;
		double waitTime = 0;
		int runTime = 0;
		int usedTime;

		// Get the first process from the holding queue
		q.add(next_q.remove());

		while ( !q.isEmpty() )
		{
			p = q.remove();
			p_num = p.proc_number;

			GanttChart.printGanttLn(runTime, p_num);

			usedTime = Math.min(p.time_remaining, rrtq);
			runTime += usedTime;
			p.time_remaining -= usedTime;

			if (next == null){
				next = next_q.poll();
			}
			while (next != null && next.arrival_time <= runTime){
				q.add(next);
				next = next_q.poll();
			}

			if (p.time_remaining == 0) {
				int turnAround_time = runTime - p.arrival_time;
				p.wait_time = turnAround_time - p.cpu_burst;
				waitTime += p.wait_time;
			}
			else {
				q.add(p);
			}
		}

		GanttChart.printAvgWaitTime( waitTime / proc_count );
	}
}

class GanttChart
{
	private static FileOutputStream fs = null;
	private static PrintStream out = null;

	private static void initOut()
	{
		if(out!=null)
			return;
		try
		{
			fs = new FileOutputStream("output.txt");
			out = new PrintStream(fs);
		}
		catch(Exception e)
		{
			System.err.println("Error: Could not open output.txt for writing\n");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void close()
	{
		try
		{
			fs.close();
			out.close();
		}
		catch(Exception e)
		{
			System.err.println("Error: Could not close output.txt\n");
			System.err.println(e);
			e.printStackTrace();
		}
	}

	public static void printAlgo(Algorithm alg)
	{
		printAlgo(alg, 0);
	}

	public static void printAlgo(Algorithm alg, int rrtq)
	{
		initOut();
		switch(alg)
		{
		case RR:
			out.println("RR " + rrtq);
			break;
		case SJF:
			out.println("SJF");
			break;
		case PR_noPREMP:
			out.println("PR_noPREMP");
			break;
		case PR_withPREMP:
			out.println("PR_withPREMP");
			break;
		}
	}

	public static void printGanttLn(int tq, int proc)
	{
		initOut();
		out.println(tq + "\t" + proc);
	}

	public static void printAvgWaitTime(double avg)
	{
		initOut();
		out.format("AVG Waiting Time: %.2f\n", avg);
	}
}

/*****Old forgotten cold****/
//	private static class SJFComparator implements Comparator<Process>{
//		public int compare (Process p1, Process p2){
//			int ret = Integer.compare(p1.cpu_burst, p2.cpu_burst);
//			if (ret == 0){ // CPU burst length tie
//				ret = Integer.compare(p1.arrival_time, p2.arrival_time);
//			}
//			if (ret == 0){ // Arrival time tie
//				ret = Integer.compare(p1.proc_number, p2.proc_number);
//			}
//			return ret;
//		}
//	}

//	private static class PriorityComparator implements Comparator<Process> {
//		public int compare (Process p1, Process p2){
//			int ret = Integer.compare(p1.priority, p2.priority);
//			if (ret == 0){ // Priority tie
//				ret = Integer.compare(p1.proc_number, p2.proc_number);
//			}
//			return ret;
//		}
//	}