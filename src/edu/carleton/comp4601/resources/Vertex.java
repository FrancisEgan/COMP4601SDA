package edu.carleton.comp4601.resources;

public class Vertex implements java.io.Serializable {

	String url;
	
	public Vertex() {
		
	}
	
	public Vertex(String _url) {
		url = _url;
	}
	
	public String getURL() {
		return url;
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2151541933542139214L;

}
