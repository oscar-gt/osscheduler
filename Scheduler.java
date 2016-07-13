// Oscar Garcia-Telles
// Css 430 Lab 2
// 6 July 2016
// Will use suspend() and resume() to
// implement a round-robin scheduler

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
	private Vector[] queues;
    private Vector queue;	// List of active threads (TCBs)
    // Additional queues for part 2
    private Vector queueB;
    private Vector queueC;
    
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
        synchronized( queue ) {
            for ( int i = 0; i < queue.size( ); i++ ) 
            {
                TCB tcb = ( TCB )queue.elementAt( i );
                Thread thread = tcb.getThread( );
                if ( thread == myThread ) // if this is my TCB, return it
                {
                	return tcb;
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
    	queues = new Vector[3];
    	for(int i = 0; i < queues.length; i++)
    	{
    		queues[i] = new Vector();
    	}
        timeSlice = quantum;
        quantumA = timeSlice/2;
        quantumB = timeSlice;
        quantumC = timeSlice * 2;

        
        initTid( maxThreads );
    }

    private void schedulerSleep( ) 
    {
        try 
        {
            Thread.sleep( timeSlice );
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
        queue.add( tcb );
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

        //this.setPriority( 6 );	//*********** Removed for part 1

        while ( true ) 
        {
            try {
                // get the next TCB and its thrad
                if ( queue.size( ) == 0 )
                    continue;
                TCB currentTCB = (TCB)queue.firstElement( );
                if ( currentTCB.getTerminated( ) == true ) 
                {
                    queue.remove( currentTCB );
                    returnTid( currentTCB.getTid( ) );
                    continue;
                }
                current = currentTCB.getThread( );
                if ( current != null ) 
                {
                    if ( current.isAlive( ) )
                    {
                    	//current.setPriority( 4 ) // Removed for part1
                    	current.resume();	// ******* Added for part1
                    }
                        
                    else {
                        // Spawn must be controlled by Scheduler
                        // Scheduler must start a new thread
                        current.start( );
                        //current.setPriority( 4 ); // **** Removed for part1
                    }
                }

                schedulerSleep( );
                // System.out.println("* * * Context Switch * * * ");

                // ***********************************************************
                // ***************** Modify For Part 2 *********************
                // ***********************************************************
                // Should probably use a queues[3] that stores
                // queueA, queueB, and queueC and then use
                // syncrhonized( queues )
                // Then move threads to queueB, C.... Would then 
                // need to modivy addThread() so that it adds to 
                // queues[0]
                synchronized ( queue ) 
                {
                    if ( current != null && current.isAlive( ) )
                    {
                    	//current.setPriority( 2 ); // **Removed for part1
                    	current.suspend(); // ***** Added for part1
                    }
                        
                    queue.remove( currentTCB ); // rotate this TCB to the end
                    queue.add( currentTCB );
                }
            } catch ( NullPointerException e3 ) { };
        }
    }
}
