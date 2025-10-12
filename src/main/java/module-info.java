module org.nasdanika.demos.diagrams.wireframes {
		
	requires transitive org.nasdanika.emf;
	requires org.nasdanika.drawio;
	requires org.nasdanika.html.producer;
	requires transitive org.nasdanika.models.bootstrap;
	
	exports org.nasdanika.demos.diagrams.wireframes;
	
	opens org.nasdanika.demos.diagrams.wireframes to org.nasdanika.common; // For reflective invocations
	
}
