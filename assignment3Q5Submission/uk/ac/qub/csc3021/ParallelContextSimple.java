package uk.ac.qub.csc3021;

import java.util.concurrent.atomic.AtomicInteger;

public class ParallelContextSimple extends ParallelContext {
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
    
    public ParallelContextSimple( int num_threads_ ) {
    	super( num_threads_ );	
    }

    public void terminate() { }

    // The edgemap method for Q3 should create threads, which each process
    // one graph partition, then wait for them to complete.
    public void edgemap( SparseMatrix matrix, Relax relax )  {
	// use matrix.ranged_edgemap( relax, from, to ); in each thread
    	
    	int num_threads = this.getNumThreads();
    	int num_vertices = matrix.getNumVertices();
    	int partition_size;
    	
    	int partition_size_no_remainder = num_vertices/num_threads;
    	int remainder = num_vertices%num_threads;
    	
    	ThreadSimple [] threads = new ThreadSimple[num_threads];
    	for (int i=0; i<num_threads; i++) {
    		if (i < remainder) {
    			partition_size = partition_size_no_remainder + 1;
    		} else {
    			partition_size = partition_size_no_remainder;
    		}
    		
    		// may want to change what values are passed to help with concurrency....
    		threads[i] = new ThreadSimple(partition_size, matrix, relax);
    		threads[i].start();
    		
    	}
    	
    	// wait for completion of all threads before continuing
    	for(ThreadSimple thread : threads) {
    		try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    	partition_from.set(0);
    }
}
