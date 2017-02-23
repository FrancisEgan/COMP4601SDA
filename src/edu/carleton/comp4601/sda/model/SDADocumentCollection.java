package edu.carleton.comp4601.sda.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SDADocumentCollection {
	@XmlElement(name="documents")
	private List<SDADocument> documents;

	public List<SDADocument> getDocuments() {
		return documents;
	}

	public void setDocuments(List<SDADocument> documents) {
		this.documents = documents;
	}
}