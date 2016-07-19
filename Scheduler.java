// Oscar Garcia-Telles
// CSS 430 Lab 2
// Scheduler.java
// 13 July 2016

// THIS IS THE CODE FOR PART 2!!!!
// Modify the following:
// --- constructors
// --- run()
// --- getMyTcb()
// --- addThread()

import java.util.*;

public class Scheduler extends Thread
{
	// Array of vectors (one vector per queue)
	// replaced a single vector for the original
	// single queue of TCBs
	private Vector<TCB>[] queues;
	private final int numberOfQueues = 3;
    
    private int timeSlice;	// Time slice allocated to each user thread execution
    private static final int DEFAULT_TIME_SLICE = 1000; // 1 second

    // New data added to p161
    private boolean[] tids; // Indicate which ids have been used
    private static final int DEFAULT_MAX_THREADS = 10000; // tids[] has this many elements
    

    // A new feature added to p161
    // Allocate an ID array, each 
    // element indicating if that id has been used
    private int nextId = 0;
    
    // Only used for debugging
    private boolean verbose = false;
    
    private void initTid( int maxThreads ) 
    {
        tids = new boolean[maxThreads];
        for ( int i = 0; i < maxThreads; i++ )
        {
        	tids[i] = false;
        }
            
    }

    // A new feature added to p161
    // Search an available thread 
    // ID and provide a new thread with this ID
    private int getNewTid( ) 
    {
        for ( int i = 0; i < tids.length; i++ ) 
        {
            int tentative = ( nextId + i ) % tids.length;
            if ( tids[tentative] == false ) 
            {
                tids[tentative] = true;
                nextId = ( tentative + 1 ) % tids.length;
                return tentative;
            }
        }
        return -1;
    }

    // A new feature added to p161
    // Return the thread ID and set 
    // the corresponding tids element to be unused
    private boolean returnTid( int tid ) {
        if ( tid >= 0 && tid < tids.length && tids[tid] == true ) 
        {
            tids[tid] = false;
            return true;
        }
        return false;
    }

    // ******************* Modified getMyTcb() For Part 2 ******************
    // This method must look through 
    // all 3 queues: Q0,Q1,Q2
    
    // A new feature added to p161
    // Retrieve the current thread's 
    // TCB from the queue
    public TCB getMyTcb( ) {
        Thread myThread = Thread.currentThread( ); // Get my thread object
        synchronized(queues)
        {
        	// Will traverse each vector at queue[i]
        	for(int i = 0; i < getNumberOfQueues(); i++)
        	{
        		// Now traversing each vector
        		int lenOfQ = getLengthOfQueue(i);
        		for(int k = 0; k < lenOfQ; i++)
        		{
        			TCB tcb = (TCB)queues[i].elementAt(k);
        			Thread thread = tcb.getThread();
        			if(thread == myThread)
        			{
        				return tcb;
        			}
        		}
        	}
        }

        return null;
    }

    // A new feature added to p161
    // Return the maximal number of 
    // threads to be spawned in the system
    public int getMaxThreads( ) 
    {
        return tids.length;
    }

    // ******************* Modified Constructor For Part 2 ****************
    public Scheduler( ) 
    {
    	// Use:
    	// this(param1, param2, ..., paramN);
    	this(DEFAULT_TIME_SLICE, DEFAULT_MAX_THREADS);
    	
    }

    // ******************* Modified Constructor For Part 2 ****************
    public Scheduler( int quantum ) 
    {
    	// Use:
    	// this(param1, param2, ..., paramN);
    	this(quantum, DEFAULT_MAX_THREADS);
    	
    }

    // ******************* Modified Constructor For Part 2 ****************
    // A new feature added to p161
    // A constructor to receive the 
    // max number of threads to be spawned
    public Scheduler( int quantum, int maxThreads ) 
    {
    	queues = new Vector[numberOfQueues];
    	for(int i = 0; i < queues.length; i++)
    	{
    		queues[i] = new Vector<TCB>();
    	}
        timeSlice = quantum;      
        initTid( maxThreads );
    }

    // ********************** Modified schedulerSleep() for part 2 **************
    // Since we always check for TCBs of a higher priority
    // after timeslice/2, the sleep time was chaged to 
    // this value. 
    @SuppressWarnings("unused")
	private void schedulerSleep( ) 
    {
        try 
        {
            Thread.sleep( timeSlice/2 );
        } 
        catch ( InterruptedException e ) 
        {
        }
    }

    // ******************* Modified addThread() For Part 2 ****************
    // Call to method should add threads to Q0 
    // A modified addThread of p161 example
	public TCB addThread( Thread t ) 
    {
        TCB parentTcb = getMyTcb( ); // get my TCB and find my TID
        int pid = ( parentTcb != null ) ? parentTcb.getTid( ) : -1;
        int tid = getNewTid( ); // get a new TID
        if ( tid == -1)
        {
        	return null;
        }
            
        TCB tcb = new TCB( t, tid, pid ); // create a new TCB
        // Adding to queue 0
        // MODIFIED, returning .add(tcb) to debug
        boolean added = false;
        added = queues[0].add( tcb );
        if(verbose)
        {
        	SysLib.cerr("queues[0].add(tcb) returned:  " + added + "\n");
        }
        
        
        return tcb;
    }

    // A new feature added to p161
    // Removing the TCB of a terminating thread
    public boolean deleteThread( ) 
    {
        TCB tcb = getMyTcb( );
        if ( tcb!= null )
            return tcb.setTerminated( );
        else
            return false;
    }

    public void sleepThread( int milliseconds ) 
    {
        try {
            sleep( milliseconds );
        } catch ( InterruptedException e ) { }
    }

    // ******************* Modified run() For Part 2 ****************

    // Should implement the following algorithm:
    // Execute threads in Q0
    // 		if thread in Q0 doesn't complete
    // 		in its quantum, move to Q1
    // If Q0 is empty, execute Q1 threads. 
    //		if Q1 thread doesn't complete
    //		in timeslice/2, check Q0 for 
    // 		threads and execute Q0 threads
    //		after pausing Q1 thread exec. 
    //		If Q1 thread doesn't terminate
    // 		in timeslice, move to Q2
    // If Q0, Q1 are empty, execute Q2 threads
    // 		after timeslice/2, check Q0,Q1 for
    //		threads and execute Q0-1 threads
    //		before resuming Q2 thread exec.
    //		If Q2 thread doesn't execute in 
    // 		timeslice*2, move to tail of Q2
    
    // A modified run of p161
    public void run( ) 
    {
    	// Note: I was originally trying to implement a single method that
    	// processes all queues in run(), but I couldn't correctly implement
    	// it right away. Therefore, I broke it up into 3 methods in a manner
    	// that made sense to me. 
        processQueueZero();
        processQueueOne();
        processQueueTwo();

    }
                
    // *****************************	Processing Queue 0		************************************
    // Processing first queue with 
    // highest priority TCBs
    @SuppressWarnings({ "deprecation", "unchecked" })
	public void processQueueZero()
    {
    	// For debugging
    	if(verbose)
    	{
    		int q0, q1, q2;
    		q0 = queues[0].size();
    		q1 = queues[1].size();
    		q2 = queues[2].size();
    		SysLib.cerr("start of processQueueZero() \n");
    		SysLib.cerr("... q0 size = " + q0 + ", q1 size = " + q1 + ", q2 size = " + q2 + " \n");
    	}
    	
    	int currQ = 0;	// Current queue is 0
    	Thread currThread = null;
    	// Will run as long as queue0 has TCBs
    	while(true) // First had while(getLengthOfQueue(0) > 0)
    	{
    		try
    		{
    			// Will exit if current queue is empty but next queue
    			// of lower priority has TCB's to process.
    			if ( queues[currQ].size( ) == 0 && queues[currQ + 1].size() > 0)
    			{
    				if(verbose)
    				{
    					SysLib.cerr("Breaking from processQueueZero() \n");
    				}
    				
    				break;
    			}
    			else if(queues[currQ].size( ) == 0)
    			{
    				if(verbose)
    				{
    					SysLib.cerr("queue[0].size() == 0 \n");
    					int q2size = queues[1].size();
        				SysLib.cerr("**//**//**//** queue[1] size == " + q2size + "\n");
    					
    				}
    				continue;
    			}
    			else if(verbose)
    			{
    				SysLib.cerr("**************************** queue[0].size() == " + queues[currQ].size() + " \n");
    				
    			}
    			
    			if(verbose)
    			{
    				int q2size = queues[1].size();
    				SysLib.cerr("**//**//**//** queue[1] size == " + q2size + "\n");
    			}
                    
    			// Getting TCB at front of queue, then getting thread
    			TCB currTCB = (TCB)queues[0].firstElement();
    			// Checking if process is terminated, removing if it is
    			if(currTCB.getTerminated() == true)
    			{
    				queues[0].remove(currTCB);
    				returnTid(currTCB.getTid()); // Freeing tid
    			}
    			// Getting current thread to process 
    			currThread = currTCB.getThread();
    			if ( currThread != null ) 
                {
                    if ( currThread.isAlive( ) )
                    {
                    	// Resuming thread execution
                    	currThread.resume();	
                    }
                        
                    else {
                        // Spawn must be controlled by Scheduler
                        // Scheduler must start a new thread
                        currThread.start( );
                    }
                }
    			// Sleeping for timeslice/2
                //sleepThread(quantumA);
    			schedulerSleep();
                
                // Will now check if current TCB needs to be bumped into
                // queue1 or removed from queue0 if it terminates
                synchronized(queues[currQ]) // First had synchronized(queues[currQ])
                {
                	// Checking if TCB has been terminated so
                	// that we can remove it from the queue
                	boolean term = currTCB.getTerminated();
                	if(term)
                	{
                		if(verbose)
                		{
                			SysLib.cerr("Removing currTCB from queue[0] since it's terminated \n");
                		}
                		// Removing
                		queues[currQ].remove(currTCB);
                	}
                	// If still alive, needs to be bumped to
                	// queue1
                	else if ( currThread != null && currThread.isAlive( ) )
                	{
                		// Suspending after quantum time processing
                		currThread.suspend(); // ***** Added for part1
                		queues[0].remove( currTCB );// Remove from queue0
                		queues[1].add(currTCB);		// Adding to queue1
                		
                	}
                	else if(currThread != null && !currThread.isAlive())
                	{

                		// Modification to commented code above: removing if not null and dead (not alive)
                		queues[currQ].remove(currTCB);
                	}
                	
                }	
    		} catch ( NullPointerException e3 ) { };
    	} // End while
    } // end processQueueZero()
    
    
    // *****************************	 Processing Queue 1		********************************
    
    // Processes second queue, may need to call 
    // processQueueZero()
    @SuppressWarnings({ "deprecation", "unchecked" })
	public void processQueueOne()
    {
    	// for debugging
    	if(verbose)
    	{
    		int q0, q1, q2;
    		q0 = queues[0].size();
    		q1 = queues[1].size();
    		q2 = queues[2].size();
    		SysLib.cerr("start of processQueueOne() \n");
    		SysLib.cerr("... q0 size = " + q0 + ", q1 size = " + q1 + ", q2 size = " + q2 + " \n");
    	}
    	
    	// Function will take a TCB from queues[1]
    	// and run for timeslice/2 before checking
    	// queues[0] to see if there are TCBs to 
    	// process. 
    	
    	int segment = 0;	// Quantum is timeslice, and we check 
    						// queue0 every timeslice/2, so we have
    						// segments 1,2
    	int lastSegment = 2;	// Used to see if we need to bump TCB to queue 2
    	
    	int currQ = 1;	// Current queue whose TCBs are being processed
    	int nextQ = 2;	// TCBs might get bumped to next queue
	
    	Thread currThread = null;
    	// Will run as long as queue0 has TCBs
    	while(true) // first had while(getLengthOfQueue(currQ) > 0)
    	{
    		try
    		{
    			// Checking of current queue is empty and next queue 
    			// with lower priority queues has TCBs to process
    			if ( queues[currQ].size( ) == 0 && queues[currQ + 1].size() > 0)
    			{
    				if(verbose)
    				{
    					SysLib.cerr("Breaking from processQueueOne() \n");
    				}
    				
    				break;
    			}
    			// Else current and next queue of lower priority
    			// is also empty, so active waiting.
    			else if(queues[currQ].size( ) == 0)
    			{
    				if(verbose)
    				{
    					SysLib.cerr("queue[1].size() == 0, continuing \n");
    				}

    				continue;
    			}
    			
    			// For debugging
    			if(verbose)
    			{
    				int q0 = queues[0].size();
    				SysLib.cerr("queue[0].size() == " + q0 + "\n");
    			}
    			// Getting TCB at front of queue, then getting thread
    			TCB currTCB = (TCB)queues[currQ].firstElement();
    			// Checking if process is terminated, removing if it is
    			if(currTCB.getTerminated() == true)
    			{
    				queues[currQ].remove(currTCB);
    				returnTid(currTCB.getTid()); // Freeing tid
    			}
    			// Getting current thread to process 
    			currThread = currTCB.getThread();
    			if ( currThread != null ) 
                {
                    if ( currThread.isAlive( ) )
                    {
                    	// Resuming thread execution
                    	currThread.resume();	
                    }
                        
                    else {
                        // Spawn must be controlled by Scheduler
                        // Scheduler must start a new thread
                        currThread.start( );
                    }
                }
    			// Sleeping and checking for TCBs in queue0
    			//sleepThread(checkAfter);
    			schedulerSleep();
    			segment = segment + 1;
    			// Checking of queue of higher priority, 
    			// queue0, has TCBs. If it does, we'll
    			// suspend current thread to process
    			// queue0
    			if(queues[currQ - 1].size() > 0)
    			{
    				if(currThread != null && currThread.isAlive())
    				{
    					currThread.suspend(); // pausing
    				}
    				if(verbose)
    				{
    					SysLib.cerr("suspending thread in processQueueOne() to exec processQueueZero() \n");
    				}
    				// Processing TCBs in queue0
    				processQueueZero();
    			}
    			
                // Will now check if current TCB needs to be bumped into
                // queue1 or removed from queue0 if it terminates
                synchronized(queues[currQ]) // First had synchronized(queues[currQ])
                {
                	// If still alive, needs to be bumped to
                	// queue2
                	if ( currThread != null && currThread.isAlive( ) && segment == lastSegment)
                	{
                		// Suspending after quantum time processing
                		currThread.suspend(); // ***** Added for part1
                		queues[currQ].remove( currTCB );// Remove from queue0
                		queues[nextQ].add(currTCB);		// Adding to queue1
                		segment = 0; // Resetting
                		
                	}
                	else if(currThread != null && currThread.isAlive() )
                	{
                		if(verbose)
                		{
                			SysLib.cerr("processQueueOne(), thread is not null and alive, segment = " + segment + "\n");
                		}
                		
                	}
                	
                	// Checking if TCB has been terminated so we can remove it
                	boolean term = currTCB.getTerminated();
                	if(term)
                	{
                		if(verbose)
                		{
                			SysLib.cerr("Removing from queue1 since TCB is terminated \n");
                		}
                		
                		queues[currQ].remove(currTCB);
                		segment = 0;
                	}                	
                }	
    		} catch ( NullPointerException e3 ) { };
    	}
    	
    }
    
    
    // *****************************	Processing Queue 2		************************************
    // This method will process the TCBs in queue 2 and check for 
    // TCBs in higher priority queues 0, 1, and process those
    // before resuming current thread.

    @SuppressWarnings({ "deprecation", "unchecked" })
	public void processQueueTwo()
    {
    	// For debugging
    	if(verbose)
    	{
    		int q0, q1, q2;
    		q0 = queues[0].size();
    		q1 = queues[1].size();
    		q2 = queues[2].size();
    		SysLib.cerr("start of processQueueTwo() \n");
    		SysLib.cerr("... q0 size = " + q0 + ", q1 size = " + q1 + ", q2 size = " + q2 + " \n");
    	}
    	
    	// Function will take a TCB from queues[2]
    	// and run for timeslice/2 before checking
    	// queues[0] and queues[1] to see if there 
    	// are TCBs to process. 
    	int segment = 0;	// Quantum is timeslice * 2, and we check 
    						// queue0 every timeslice/2, so we have
    						// segments 1, 2, 3, 4.
    	int lastSegment = 4;
    	int currQ = 2;	// Current queue whose TCBs are being processed
    	int nextQ = 2;	// TCBs bumped to end of current vector

    	Thread currThread = null;
    	// Will run as long as queue0 has TCBs
    	while(true) // First had while(getLengthOfQueue(currQ) > 0)
    	{
    		try
    		{
    			if ( queues[currQ].size( ) == 0 )
    			{
    				if(verbose)
    				{
    					SysLib.cerr("queue[2].size() == 0, continuing \n");
    				}
    				continue;
    			}
    			
    			// Getting TCB at front of queue, then getting thread
    			TCB currTCB = (TCB)queues[currQ].firstElement();
    			// Checking if process is terminated, removing if it is
    			if(currTCB.getTerminated() == true)
    			{
    				queues[currQ].remove(currTCB);
    				returnTid(currTCB.getTid()); // Freeing tid
    			}
    			// Getting current thread to process 
    			currThread = currTCB.getThread();
    			if ( currThread != null ) 
    			{
    				if ( currThread.isAlive( ) )
    				{
    					// Resuming thread execution
    					currThread.resume();	
    				}

    				else {
    					// Spawn must be controlled by Scheduler
    					// Scheduler must start a new thread
    					currThread.start( );
    				}
    			}
    			// Sleeping and checking for TCBs in queue0, queue1
    			//sleepThread(checkAfter);
    			schedulerSleep();
    			segment = segment + 1;
    			if(queues[0].size() > 0)
    			{
    				if(currThread != null && currThread.isAlive())
    				{
    					currThread.suspend();
    				}
    				processQueueZero();
    				if(verbose)
    				{
    					SysLib.cerr("*** RESUMING *** processQueueTwo() after calling processQueueZero() \n");
    				}
    				
    			}
    			// Checking queue1
    			if(queues[1].size() > 0)
    			{
    				if(currThread != null && currThread.isAlive())
    				{
    					currThread.suspend();
    				}
    				if(verbose)
    				{
    					SysLib.cerr("calling processQueueOne() from processQueueTwo() \n");
    				}
    				processQueueOne();
    				if(verbose)
    				{
    					SysLib.cerr("*** RESUMING *** processQueueTwo() after calling processQueueOne() \n");
    				}
    				
    			}

    			// Will now check if current TCB needs to be moved
    			// to the tail of the current queue
    			synchronized(queues[currQ]) // First had synchronized(queues[currQ])
    			{
    				// If still alive, needs to be bumped to
    				// queue1
    				if ( currThread != null && currThread.isAlive( ) && segment == lastSegment)
    				{
    					// Suspending after quantum time processing
    					currThread.suspend(); // ***** Added for part1
    					queues[currQ].remove( currTCB );// Remove from queue2
    					queues[currQ].add(currTCB);		// Adding to end
    					segment = 0; // Resetting for next TCB

    				}
    				// Checking if current TCB has been terminated
    				// so that we can remove it from the current queue.
    				boolean term = currTCB.getTerminated();
    				if(term)
    				{
    					if(verbose)
    					{
    						SysLib.cerr("Removing from queue2 since current TCB has been terminated \n");
    					}
    					queues[currQ].remove(currTCB);
    					segment = 0; // Resetting for next TCB
    				}
    			}

    		} catch ( NullPointerException e3 ) { };
    	}

    }
    
    // Returns the number of queues
    public int getNumberOfQueues()
    {
    	return numberOfQueues;
    }
    
    // Returns the size/length of a specified queue
    public int getLengthOfQueue(int q) throws IllegalArgumentException
    {
    	if(q < 0 || q >= getNumberOfQueues())
    	{
    		throw new IllegalArgumentException("Invalid queue number");
    	}
    	
    	return queues[q].size();
    }   
}
