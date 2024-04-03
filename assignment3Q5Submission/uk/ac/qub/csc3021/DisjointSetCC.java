package uk.ac.qub.csc3021;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicIntegerArray;

// Calculate the connected components using disjoint set data structure
// This algorithm only works correctly for undirected graphs
public class DisjointSetCC {
	
	
    private static class DSCCRelax implements Relax {
	DSCCRelax( AtomicIntegerArray parent_ ) {
	    this.parent = parent_;
	}

	public void relax( int src, int dst ) {
	    union( src, dst );
	    
	}

	public int find( int x ) {
		int u = x;
		while (true) {
			int v = parent.get(u);
			int w = parent.get(v);
			if (v == w) {
				return v;
			} else {
				parent.compareAndSet(u,v,w);
				u = v;
				u = parent.get(u);
			}
		}
	}

	private boolean union( int x, int y ) {
		int u = x;
		int v = y;
		while (true) {
		    u = find(u);
		    v = find(v);
		    if (u < v) {
		        if (parent.compareAndSet(u,u,v)) {
		            return false;
		    	}
		    }
		    
		    else if (u == v) {
		    	return true;
		    }
		    else if (parent.compareAndSet(v,v,u)) {
		         return false;
		    }
		}
	}
	
	// return x < y; 
	    
	    
	// Variable declarations
	private AtomicIntegerArray parent;
    };

    public static int[] compute( SparseMatrix matrix ) {
	long tm_start = System.nanoTime();

	final int n = matrix.getNumVertices();
	final AtomicIntegerArray parent = new AtomicIntegerArray( n );
	final boolean verbose = true;

	for( int i=0; i < n; ++i ) {
	    // Each vertex is a set on their own
	    parent.set(i, i);
	}

	DSCCRelax DSCCrelax = new DSCCRelax( parent );

	double tm_init = (double)(System.nanoTime() - tm_start) * 1e-9;
	System.err.println( "Initialisation: " + tm_init + " seconds" );
	tm_start = System.nanoTime();

	ParallelContext context = ParallelContextHolder.get();

	// 1. Make pass over graph
	context.edgemap( matrix, DSCCrelax );

	double tm_step = (double)(System.nanoTime() - tm_start) * 1e-9;
	if( verbose )
	    System.err.println( "processing time=" + tm_step + " seconds" );
	tm_start = System.nanoTime();

	// Post-process the labels

	// 1. Count number of components
	//    and map component IDs to narrow domain
	int ncc = 0;
	int remap[] = new int[n];
	for( int i=0; i < n; ++i )
	    if( DSCCrelax.find( i ) == i )
		remap[i] = ncc++;

	if( verbose )
	    System.err.println( "Number of components: " + ncc );

	// 2. Calculate size of each component
	int sizes[] = new int[ncc];
	for( int i=0; i < n; ++i )
	    ++sizes[remap[DSCCrelax.find( i )]];

	if( verbose )
	    System.err.println( "DisjointSetCC: " + ncc + " components" );

	return sizes;
    }
}