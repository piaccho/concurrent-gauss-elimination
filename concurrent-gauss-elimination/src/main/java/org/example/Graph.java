package org.example;
import java.util.*;

public class Graph {
    private final int size;
    private final List<List<Integer>> adjList;

    public Graph(int nodes) {
        this.size = nodes;
        adjList = new ArrayList<>();
        for (int i = 0; i < nodes; i++) {
            adjList.add(new ArrayList<>());
        }
    }

    public void addEdge(int src, int dest) {
        adjList.get(src).add(dest);
//        adjList.get(dest).add(src); // For undirected graph
    }

    public void removeEdge(int src, int dest) {
        if (adjList.get(src).contains(dest))
            adjList.get(src).remove(adjList.get(src).indexOf(dest));
        if (adjList.get(dest).contains(src))
            adjList.get(dest).remove(adjList.get(dest).indexOf(src));
    }

    public List<Integer> getNeighbors(int node) {
        return adjList.get(node);
    }

    public int getSize() {
        return size;
    }

    public List<List<Integer>> getAdjList() {
        return adjList;
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < adjList.size(); i++) {
            sb.append(i).append(" -> ").append(adjList.get(i)).append("\n");
        }
        return sb.toString();
    }
}