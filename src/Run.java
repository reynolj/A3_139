import java.io.*;
import java.util.*;

public class Run
{
	private static Scanner scanner = null;
	private static Algorithm algo = null;
	private static int proc_count;
	private static Queue<Process> q = new LinkedList<Process>();
	private static int rrtq = 0;

	public static void main(String[] args)
	{
		// Open input file and get: proc_count, rrtq, and algo
		open();
		// Read input file, create process objs, and put them in a queue
		populateQ();

		// Send queue to the proper Scheduling algorithm
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

		// Close output file
		GanttChart.close();
	}

	private static void open()
	{
		File in;

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
	}

	private static void populateQ()
	{
		for (int i = 0; i < proc_count; i++){
			int proc_number 	= 	scanner.nextInt();
			int arrival_time 	= 	scanner.nextInt();
			int cpu_burst 		= 	scanner.nextInt();
			int priority 		= 	scanner.nextInt();
			Process p = new Process(proc_number, arrival_time, cpu_burst, priority, i);
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
	// Public fields made it easier to code/read my algorithms.
	// And frankly I don't think it really matters for this assignment
	public final int proc_number;
	public final int arrival_time;
	public final int cpu_burst;
	public final int priority;

	public final int order; 	// For Round Robin
	public int time_remaining;  // For Priority_withPREMP
	public int wait_time; 		// NOTE: The wait_time field of process is unnecessary for calculation,
								// but it is very useful for debugging

	Process(int p, int a, int c, int pr, int o)
	{
		proc_number 	= 	p;
		arrival_time 	= 	a;
		cpu_burst 		= 	c;
		priority 		= 	pr;
		order			=   o;
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
	// Round Robin scheduling algorithm
	public static void RoundRobin(Queue<Process> input_q, int rrtq, int proc_count)
	{
		// Process objects are put into queue and sorted in order of 'arrival_time', ties broken by 'order'
		// Queue holds processes that have not arrived
		PriorityQueue<Process> hold = new PriorityQueue<Process>(proc_count, new RR_Comparator());
		hold.addAll(input_q);

		// Queue for Round Robin behavior
		Queue<Process> q = new LinkedList<Process>();

		Process p;				// Process on cpu
		int runTime = 0;		// CPU time
		int usedTime;			// The amount of time a process used while on the CPU
		double waitTime = 0.0;	// Used to calculate average wait time

		// Print header to output file
		GanttChart.printAlgo(Algorithm.RR, rrtq);

		// Get all processes arriving at runTime:0
		getArrivingProcesses_RR(q, hold, runTime);

		while ( !q.isEmpty() || !hold.isEmpty() )
		{
			// While there are no processes that have arrived, time will continue until one arrives.
			// NOTE: Not relevant to any given test cases. Required if there is any gaps of time between processes.
			while (q.isEmpty()){
				runTime++;
				getArrivingProcesses_RR(q, hold, runTime);
			}

			// Gets the process requesting the CPU
			p = q.remove();

			// Print runTime and the number of the process on the CPU
			GanttChart.printGanttLn(runTime, p.proc_number);

			/*
				Scheduling decision are made when either the process that has the CPU terminates or the time quantum
				expires.
		 	*/
			usedTime = Math.min(p.time_remaining, rrtq);
			runTime += usedTime;
			p.time_remaining -= usedTime;

			/*
				If a new process arrives and a time quantum expires at the same time, insert the new arrival
				at the end of the queue before inserting hte process whose time quantum expired.
			*/
			getArrivingProcesses_RR(q, hold, runTime);

			/*
				If the process on CPU has terminated, then add it's wait time to local wait time,
				Otherwise, put it on the end of the queue
			*/
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

	/*
		SJF / PR_noPREMP scheduling algorithms
		My implimentation of SJF and Pr_noPREMP were the same except for the comparator. So, I merged the
		two functions and added a ternary statement for the comparator.
	*/
	public static void noPremp(Queue<Process> hold, int proc_count, Algorithm algo)
	{
		PriorityQueue<Process> pq = new PriorityQueue<Process>
				(
						proc_count,
						algo == Algorithm.SJF ? new SJF_Comparator() : new Pr_Comparator()
				);

		Process p;				// Process on CPU
		int runTime = 	0;		// CPU time
		double waitTime = 0.0;	// Used to calculate average wait time

		// Print header to output file
		GanttChart.printAlgo(algo);

		while ( !pq.isEmpty() || !hold.isEmpty() ){
			getArrivingProcesses(pq, hold, runTime);

			// While there are no processes that have arrived, time will continue until one arrives.
			// NOTE: Not relevant to any given test cases. Required if there is any gaps of time between processes.
			while (pq.isEmpty()){ //CPU waits for processes
				runTime++;
				getArrivingProcesses(pq, hold, runTime);
			}

			// Gets the process requesting the CPU
			p = pq.remove();

			// Print runTime and the number of the process on the CPU
			GanttChart.printGanttLn(runTime, p.proc_number);

			// Process gets CPU until its burst is completed
			runTime += p.cpu_burst;

			// Calculate wait time
			p.wait_time = runTime - p.cpu_burst - p.arrival_time;
			waitTime += p.wait_time;
		}

		GanttChart.printAvgWaitTime(waitTime / proc_count);
	}

	// PR_noPREMP scheduling algorithm
	public static void Priority_withPremp(Queue<Process> hold, int proc_count)
	{
		PriorityQueue<Process> pq = new PriorityQueue<Process>(proc_count, new Pr_Comparator());

		Process p;				// Process on CPU
		int runTime = 0;		// CPU time
		double waitTime = 0.0;	// Used to calculate average wait time
		boolean preempted;		// Flag used to kick a process off CPU

		// Print header to output file
		GanttChart.printAlgo(Algorithm.PR_withPREMP);

		while ( !pq.isEmpty() || !hold.isEmpty() ){
			preempted = false;
			getArrivingProcesses(pq, hold, runTime);

			// While there are no processes that have arrived, time will continue until one arrives.
			// NOTE: Not relevant to any given test cases. Required if there is any gaps of time between processes.
			while (pq.isEmpty()){
				runTime++;
				getArrivingProcesses(pq, hold, runTime);
			}
			// Gets the process requesting the CPU
			p = pq.remove();

			// Print runTime and the number of the process on the CPU
			GanttChart.printGanttLn(runTime, p.proc_number);

			/*
				P gets the CPU, but if a process with higher priority arrives, it is preempted and added to queue.
				Unless, the process on the cpu has terminated.
			*/
			while (p.time_remaining != 0 && !preempted){
				p.time_remaining--;
				runTime++;

				getArrivingProcesses(pq, hold, runTime);

				if (!pq.isEmpty() && p.time_remaining != 0) {
					if (pq.peek().priority < p.priority) {
						preempted = true;
						pq.add(p);
					}
				}
			}

			/*
				If the process on the CPU terminated, add its wait time to the local wait time.
			*/
			if (p.time_remaining == 0)
			{
				p.wait_time = runTime - p.cpu_burst - p.arrival_time;
				waitTime += p.wait_time;
			}
		}

		GanttChart.printAvgWaitTime(waitTime / proc_count);
	}

	// Ascending: Order by priority -> ties broken by process number
	private static class Pr_Comparator implements Comparator<Process>
	{
		public int compare (Process p1, Process p2){
			int ret = ((Integer) p1.priority).compareTo((Integer) p2.priority);
			if (ret == 0){ // Priority tie
				ret = ((Integer) p1.proc_number).compareTo((Integer) p2.proc_number);
			}
			return ret;
		}
	}

	//	Ascending: Order by arrival time -> ties broken by order
	private static class RR_Comparator implements Comparator<Process>
	{
		public int compare (Process p1, Process p2){
			int ret = ((Integer) p1.arrival_time).compareTo((Integer) p2.arrival_time);
			if (ret == 0){ // Arrival Time tie
				ret = ((Integer) p1.order).compareTo((Integer) p2.order);
			}
			return ret;
		}
	}

	// Ascending: Order by cpu burst -> ties broken by arrival time -> ties broken by process number
	private static class SJF_Comparator implements Comparator<Process>
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

	// Arriving processes in hold, are moved into pq
	private static void getArrivingProcesses(Queue<Process> pq, Queue<Process> hold, int runTime)
	{
		if (hold.isEmpty())
		{
			return;
		}

		Queue<Process> arrived_processes = new LinkedList<Process>();

		for (Process p : hold){
			if (p.arrival_time <= runTime){
				arrived_processes.add(p);
			}
		}

		pq.addAll(arrived_processes);
		hold.removeAll(arrived_processes);
	}

	// Arriving processes in hold, are moved into q
	private static void getArrivingProcesses_RR(Queue<Process> q, Queue<Process> hold, int runTime)
	{
		while (!hold.isEmpty() && hold.peek().arrival_time <= runTime){
			q.add(hold.remove());
		}
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

/*****Old forgotten code****/
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