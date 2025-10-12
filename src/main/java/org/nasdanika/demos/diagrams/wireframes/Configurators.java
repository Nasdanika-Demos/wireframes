package org.nasdanika.demos.diagrams.wireframes;

import java.util.Date;

import org.nasdanika.drawio.Node;
import org.nasdanika.exec.content.Text;
import org.nasdanika.html.bootstrap.Color;
import org.nasdanika.mapping.ReflectiveContributor.Configurator;
import org.nasdanika.models.bootstrap.Appearance;
import org.nasdanika.models.bootstrap.BootstrapFactory;
import org.nasdanika.models.bootstrap.Container;

/**
 * Contains configurator methods for semantic mapping of diagrams
 */
public class Configurators {
	
	@Configurator("id == 'body-root-container'")
	public void configureBodyRootContainer(Node source, Container target) {		
		Appearance appearance = BootstrapFactory.eINSTANCE.createAppearance();		
		target.setAppearance(appearance);
		appearance.setBackground(Color.LIGHT);
	}
	
	@Configurator("id == 'uDMjheR7BUX2y2fQ2ZUI-7'")
	public void configureText(Node source, Text target) {
		target.setContent("Hello at " + new Date());
	}
			
}
