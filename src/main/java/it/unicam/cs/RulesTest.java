package it.unicam.cs;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import it.unicam.cs.controller.GridController;
import it.unicam.cs.model.Configuration;
import it.unicam.cs.model.Grid;

public class RulesTest {
	public static void main(String[] args) {
		try {
			KieServices ks = KieServices.Factory.get();
			KieContainer kContainer = ks.getKieClasspathContainer();
			KieSession kSession = kContainer.newKieSession("ksession-rules");
			
			Configuration config = new Configuration(9, 9, 10);
			Grid grid = new Grid(config);
			
			kSession.insert(grid);
			kSession.fireAllRules();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
