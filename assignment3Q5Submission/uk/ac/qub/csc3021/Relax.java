package uk.ac.qub.csc3021;

// Functional interface that describes the operation performed when visiting
// an edge.
interface Relax {
    public void relax( int source, int destination );
}
