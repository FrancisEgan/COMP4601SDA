package edu.carleton.comp4601.crawler;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Multigraph;

public class PageStorage {
	
	private static PageStorage instance;
	private Multigraph<Vertex, DefaultEdge> graph;
	
	private PageStorage(){
		graph = new Multigraph<Vertex, DefaultEdge>(DefaultEdge.class);
	}
	
	public static PageStorage getInstance(){
		if (instance == null)
			instance = new PageStorage();
		return instance;
	}
	
	public Multigraph<Vertex, DefaultEdge> getGraph(){ return this.graph; }
}
