


// NOTES: 
// Don't remove or put additional 
// "synchronized" keywords in the code
// or else deadlock may occur

// THIS IS THE CODE FOR PART 2!!!!
// Modify the following:
// --- constructors
// --- run()
// --- getMyTcb()
// --- addThread()

// 

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
    // Quanta for queues
    private int quantumA;
    private int quantumB;
    private int quantumC; 

    // New data added to p161
    private boolean[] tids; // Indicate which ids have been used
    private static final int DEFAULT_MAX_THREADS = 10000; // tids[] has this many elements
    

    // A new feature added to p161
    // Allocate an ID array, each 
    // element indicating if that id has been used
    private int nextId = 0;
    
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

    // ********************************************************************
    // ******************* Modify getMyTcb() For Part 2 ******************
    // ********************************************************************
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
//        synchronized( queue ) {
//            for ( int i = 0; i < queue.size( ); i++ ) 
//            {
//                TCB tcb = ( TCB )queue.elementAt( i );
//                Thread thread = tcb.getThread( );
//                if ( thread == myThread ) // if this is my TCB, return it
//                {
//                	return tcb;
//                }
//                    
//            }
//        } // end of synchronized( queue )
        return null;
    }

    // A new feature added to p161
    // Return the maximal number of 
    // threads to be spawned in the system
    public int getMaxThreads( ) 
    {
        return tids.length;
    }

    // ************************************************************************
    // ******************* Modify Constructor For Part 2 ****************
    // ************************************************************************
    public Scheduler( ) 
    {
    	// Use:
    	// this(param1, param2, ..., paramN);
    	this(DEFAULT_TIME_SLICE, DEFAULT_MAX_THREADS);
    	
    }

    // ************************************************************************
    // ******************* Modify Constructor For Part 2 ****************
    // ************************************************************************
    public Scheduler( int quantum ) 
    {
    	// Use:
    	// this(param1, param2, ..., paramN);
    	this(quantum, DEFAULT_MAX_THREADS);
    	
    }

    // ************************************************************************
    // ******************* Modify Constructor For Part 2 ****************
    // ************************************************************************
    
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
        quantumA = timeSlice/2;
        quantumB = timeSlice;
        quantumC = timeSlice * 2;

        
        initTid( maxThreads );
    }

    @SuppressWarnings("unused")
	private void schedulerSleep( ) 
    {
        try 
        {
            Thread.sleep( timeSlice/2 ); // *************************** Originally Thread.sleep( timeSlice );
        } 
        catch ( InterruptedException e ) 
        {
        }
    }

    // ************************************************************************
    // ******************* Modify addThread() For Part 2 ****************
    // ************************************************************************
    // Should add thread to Q0
    
    // A modified addThread of p161 example
	public TCB addThread( Thread t ) 
    {
        //t.setPriority( 2 );	// ********************** Removed for part 1
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

    // ************************************************************************
    // ******************* Modify run() For Part 2 ****************
    // ************************************************************************
    // Should implement the following algorithm:
    // Execute threads in Q0
    // 		if thread in Q0 doesn't complete
    // 		in its quantum, move to Q1
    // If Q0 is empty, execute Q1 threads. 
    //		if Q1 thread doesn't complete
    //		in timeslice/2, check Q0 for 
    // 		threads and execute Q0 threads
    //		after pausing Q1 thread exec. 
    //		If Q1 thread doesn't execute
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
    
        Thread current = null;
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
    	int count = 0;
    	if(verbose)
    	{
    		int q0, q1, q2;
    		q0 = queues[0].size();
    		q1 = queues[1].size();
    		q2 = queues[2].size();
    		SysLib.cerr("start of processQueueZero() \n");
    		SysLib.cerr("... q0 size = " + q0 + ", q1 size = " + q1 + ", q2 size = " + q2 + " \n");
    	}
    	
    	int currQ = 0;
    	Thread currThread = null;
    	// Will run as long as queue0 has TCBs
    	while(true) // First had while(getLengthOfQueue(0) > 0)
    	{
    		try
    		{
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
    					SysLib.cerr("queue[0].size() == 0, count == " + count + ", continuing \n");
    					count = count + 1;
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
                    	currThread.resume();	// ******* Added for part1
                    }
                        
                    else {
                        // Spawn must be controlled by Scheduler
                        // Scheduler must start a new thread
                        currThread.start( );
                    }
                }
    			// Sleeping for queue0 quantum
                //sleepThread(quantumA);
    			schedulerSleep();
                
                // Will now check if current TCB needs to be bumped into
                // queue1 or removed from queue0 if it terminates
                synchronized(queues[currQ]) // First had synchronized(queues[currQ])
                {
                	// If still alive, needs to be bumped to
                	// queue1
                	boolean term = currTCB.getTerminated();
                	if(term)
                	{
                		if(verbose)
                		{
                			SysLib.cerr("Removing currTCB from queue[0] since it's terminated \n");
                		}
                		
                		queues[currQ].remove(currTCB);
                	}
                	else if ( currThread != null && currThread.isAlive( ) )
                	{
                		// Suspending after quantum time processing
                		currThread.suspend(); // ***** Added for part1
                		queues[0].remove( currTCB );// Remove from queue0
                		queues[1].add(currTCB);		// Adding to queue1
                		
                	}
                	else if(currThread != null && !currThread.isAlive())
                	{
//                		// Else bumping to tail of queue0
//                		SysLib.cerr("processQueueZero() bumping to end of queue0, thread not null, not alive \n");
//                		SysLib.cerr("..... TCB tid is " + currTCB.getTid() + "\n");
//                		queues[0].remove( currTCB ); // rotate this TCB to the end
//                		queues[0].add( currTCB );
                		// Modification to commented code above: removing if not null and dead (not alive)
                		queues[currQ].remove(currTCB);
                	}
                	
                }

    			
    			
    		} catch ( NullPointerException e3 ) { };
    	} // End while
    } // end run()
    
    // *****************************	 Processing Queue 1		********************************
    
    // Processes second queue, may need to call 
    // processQueueZero()
    @SuppressWarnings({ "deprecation", "unchecked" })
	public void processQueueOne()
    {
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
    						// segments 0, 1
    	int quantum = 0;
    	int checkAfter = 0;
    	int currQ = 1;	// Current queue whose TCBs are being processed
    	int nextQ = 2;	// TCBs might get bumped to next queue
    	if(currQ == 0)
    	{
    		quantum = timeSlice / 2;
    		checkAfter = 0;
    		nextQ = 1;
    	}
    	else if(currQ == 1)
    	{
    		quantum = timeSlice;
    		checkAfter = timeSlice / 2;
    		nextQ = 2;
    	}
    	else if(currQ == 2)
    	{
    		quantum = timeSlice * 2;
    		checkAfter = timeSlice / 2;
    		
    	}
    	
    	
    	Thread currThread = null;
    	// Will run as long as queue0 has TCBs
    	while(true) // first had while(getLengthOfQueue(currQ) > 0)
    	{
    		try
    		{
    			if ( queues[currQ].size( ) == 0 && queues[currQ + 1].size() > 0)
    			{
    				if(verbose)
    				{
    					SysLib.cerr("Breaking from processQueueOne() \n");
    				}
    				
    				break;
    			}
    			else if(queues[currQ].size( ) == 0)
    			{
    				if(verbose)
    				{
    					SysLib.cerr("queue[1].size() == 0, continuing \n");
    				}

    				continue;
    			}
    			
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
//    			// ****************************************  Checking if terminated **********************************
//    			boolean term = currTCB.getTerminated();
//    			SysLib.cerr("processQueueOne(), currTCB.getTerminated() == " + term + "\n");
    			if ( currThread != null ) 
                {
                    if ( currThread.isAlive( ) )
                    {
                    	// Resuming thread execution
                    	currThread.resume();	// ******* Added for part1
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
    			if(queues[currQ - 1].size() > 0)
    			{
    				if(currThread != null && currThread.isAlive())
    				{
    					currThread.suspend();
    				}
    				if(verbose)
    				{
    					SysLib.cerr("suspending thread in processQueueOne() to exec processQueueZero() \n");
    				}
    				
    				processQueueZero();
    			}
    			
    			
                
                
                // Will now check if current TCB needs to be bumped into
                // queue1 or removed from queue0 if it terminates
                synchronized(queues[currQ]) // First had synchronized(queues[currQ])
                {
                	// If still alive, needs to be bumped to
                	// queue1
                	if ( currThread != null && currThread.isAlive( ) && segment == 2)
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
                	// Do nothing, leave in current queue 1
                	
                }

    			
    			
    		} catch ( NullPointerException e3 ) { };
    	}
    	
    }
    
    
    // *****************************	Processing Queue 2		************************************
    
    // Processes third queue
    @SuppressWarnings({ "deprecation", "unchecked" })
	public void processQueueTwo()
    {
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
    	// segments 0, 1, 2, 3
    	int lastSegment = 4;
    	int quantum = 0;
    	int checkAfter = 0;
    	int currQ = 2;	// Current queue whose TCBs are being processed
    	int nextQ = 2;	// TCBs might get bumped to next queue
    	if(currQ == 0)
    	{
    		quantum = timeSlice / 2;
    		checkAfter = 0;
    		nextQ = 1;
    	}
    	else if(currQ == 1)
    	{
    		quantum = timeSlice;
    		checkAfter = timeSlice / 2;
    		nextQ = 2;
    	}
    	else if(currQ == 2)
    	{
    		quantum = timeSlice * 2;
    		checkAfter = timeSlice / 2;

    	}


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
    			
//    			if(verbose)
//    			{
//    				int q0 = queues[0].size();
//    				int q1 = queues[1].size();
//    				SysLib.cerr("queue[0].size() == " + q0 + ", queue[1].size() == " + q1 + "\n");
//    			}
    			
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
    					currThread.resume();	// ******* Added for part1
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




    			// Will now check if current TCB needs to be bumped into
    			// queue1 or removed from queue0 if it terminates
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
    					segment = 0; // Resetting

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
    					segment = 0;
    				}
    				// Do nothing, leave in current queue 2

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
    
    // Returns true of there are TCBs in queues in lower priority
    
}
