package uk.ac.qub.csc3021;

import java.util.concurrent.atomic.AtomicInteger;

public class ParallelContextSimpleQ5 extends ParallelContext {
	private AtomicInteger partition_from = new AtomicInteger(0);
    
	private class ThreadSimple extends Thread {
    	private int partition_size;
    	private SparseMatrix matrix;
    	private Relax relax;
    	
    	public ThreadSimple(int partition_size, SparseMatrix matrix, Relax relax) {
			this.partition_size = partition_size;
			this.matrix = matrix;
			this.relax = relax;
		}

		public void run() {
			// each thread should execute this line 
	    	
			matrix.ranged_edgemap( relax, partition_from.getAndAdd(partition_size), partition_from.get() );
			
			
		}
    };
    
    public ParallelContextSimpleQ5( int num_threads_ ) {
    	super( num_threads_ );	
    }

    public void terminate() { }

    // The edgemap method for Q3 should create threads, which each process
    // one graph partition, then wait for them to complete.
    public void edgemap( SparseMatrix matrix, Relax relax )  {
	// use matrix.ranged_edgemap( relax, from, to ); in each thread
    	matrix.edgemap(relax);
    }
}
