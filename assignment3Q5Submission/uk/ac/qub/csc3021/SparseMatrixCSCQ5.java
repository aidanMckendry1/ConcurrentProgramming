package uk.ac.qub.csc3021;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicIntegerArray;

import uk.ac.qub.csc3021.Relax;
import uk.ac.qub.csc3021.SparseMatrix;

// This class represents the adjacency matrix of a graph as a sparse matrix
// in compressed sparse rows format (CSR), where a row index corresponds to
// a source vertex and a column index corresponds to a destination
public class SparseMatrixCSCQ5 extends SparseMatrix {
    // TODO: variable declarations
    //...
    int num_vertices; // Number of vertices in the graph
    int num_edges;    // Number of edges in the graph
    // static DSCCRelax DSCCrelax;
    // RandomAccessFile raf;
    // BufferedReader bf = new BufferedReader(raf);
    String file;
    int ncc = 0;
    static int sizes[];
    
    
    public SparseMatrixCSCQ5( String file ) {
	try {
		this.file = file;
		InputStreamReader is = new InputStreamReader(new FileInputStream(file), "UTF-8");
        BufferedReader rd = new BufferedReader(is);
        readFile(rd); // only reads in first 3 lines
        rd.close();
        
	} catch( FileNotFoundException e ) {
	    System.err.println( "File not found: " + e );
	    return;
	} catch( UnsupportedEncodingException e ) {
	    System.err.println( "Unsupported encoding exception: " + e );
	    return;
	} catch( Exception e ) {
	    System.err.println( "Exception: " + e );
	    return;
	}
    }

    private void readFile(BufferedReader rd) throws Exception {
    	String line = rd.readLine();
    	if( line == null )
    	    throw new Exception( "premature end of file" );
    	if( !line.equalsIgnoreCase( "CSC" ) && !line.equalsIgnoreCase( "CSC-CSR" ) )
    	    throw new Exception( "file format error -- header" );
    	
    	num_vertices = Integer.parseInt(rd.readLine());
    	num_edges = Integer.parseInt(rd.readLine());		
	}

	int getNext( BufferedReader rd ) throws Exception {
	String line = rd.readLine();
	if( line == null )
	    throw new Exception( "premature end of file" );
	return Integer.parseInt( line );
    }

 // private AtomicInteger partition_from = new AtomicInteger(0);
    private class ReadThread extends Thread {
    	MappedByteBuffer buffer;
    	Relax relax;
    	int i;
    	int lineOverlap;
    	
    	public ReadThread(MappedByteBuffer buffer, Relax relax, int i, int lineOverlap) {
			//this.chunkSize = chunkSize;
			this.buffer = buffer;
			this.relax = relax;
			//this.DSCCrelax = DSCCrelax;
			this.i = i;
			this.lineOverlap = lineOverlap;
		}
    	
    	public void run() {
    		if (i == 0) {
                int newLines = 2;
                while (newLines > 0) {
                    if ((char) buffer.get() == '\n') {
                        newLines--;
                    }
                }
            }
            char c = (char) buffer.get();
            StringBuilder stringBuilder = new StringBuilder();

            
            while (c != '\n') {
                c = (char) buffer.get();
            }
            // until buffer exceeds the longest possible line in orkut graph
            while (buffer.remaining() >= lineOverlap) {
                c = (char) buffer.get();
                while (c != ' ' && c != '\n') {
                    stringBuilder.append(c);
                    c = (char) buffer.get();
                }
                int src = Integer.parseInt(stringBuilder.toString());
                stringBuilder = new StringBuilder();

                while (c != '\n') {
                	// acquiring each destination vertex for this src vertex
                    c = (char) buffer.get();
                    while (c != ' ' && c != '\n') {
                        stringBuilder.append(c);
                        c = (char) buffer.get();
                    }
                    int dst = Integer.parseInt(stringBuilder.toString());
                    
                    relax.relax(src, dst);
                    stringBuilder = new StringBuilder();;
                }
            }
        }
    }
    
    // Return number of vertices in the graph
    public int getNumVertices() { return num_vertices; }

    // Return number of edges in the graph
    public int getNumEdges() { return num_edges; }

    // Auxiliary function for PageRank calculation
    public void calculateOutDegree( int outdeg[] ) {
	// TODO:
	//    Calculate the out-degree for every vertex, i.e., the
	//    number of edges where a vertex appears as a source vertex.
	//...
    }

    public void ranged_edgemap( Relax relax, int from, int to ) {
	// Only implement for parallel/concurrent processing
	// if you find it useful
    }
    
    public void edgemap( Relax relax ) {
    	// should create read threads 
    	int num_threads = 8;
    	try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            FileChannel fc = raf.getChannel();
            long fcSize = fc.size();
            // DSCCrelax relax;
            int id;
            ReadThread[] threads = new ReadThread[num_threads];
            
            for (int i = 0; i < num_threads; i++) {      
                int lineOverlap = 270000;
                if (i == num_threads - 1) {
                    lineOverlap = 1;
                }
                int offset = (int) (lineOverlap + (fcSize/num_threads));
                int size = (int) (i * fcSize/num_threads);
                MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, size, offset);
                
                threads[i] = new ReadThread(buffer, relax, i, lineOverlap);
                threads[i].start();
            }
            for (int i = 0; i < num_threads; i++) {
                threads[i].join();
            }
            fc.close();
        } catch (Exception e) {
			e.printStackTrace();
        }
    }
    
    /*private static class DSCCRelax implements Relax {
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
    				// u = parent.get(u);
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
        };*/
}

