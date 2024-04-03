package uk.ac.qub.csc3021;

public abstract class ParallelContext {
    private int num_threads;

    ParallelContext( int num_threads_ ) {
	num_threads = num_threads_;
	//for (int i=0; i<num_threads; i++) {
		// create threads and start
		// work out what goes in the run method 
		// partition graphs into sections for threads here?
		
		// this should go in the parallel context simple class
	//}
    }

    public int getNumThreads() { return num_threads; }

    // Terminate all threads
    public abstract void terminate();

    // This is currently calling into the edgemap method in the matrix.
    // You will specialise this class to introduce concurrency in Question 2
    // and will update this class in subsequent questions.
    public abstract void edgemap( SparseMatrix matrix, Relax relax );
}
