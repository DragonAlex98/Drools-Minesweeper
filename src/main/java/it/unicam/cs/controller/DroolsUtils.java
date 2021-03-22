package it.unicam.cs.controller;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import it.unicam.cs.model.Configuration;
import it.unicam.cs.model.Grid;
import lombok.Getter;
import lombok.Setter;

public class DroolsUtils {
	private static DroolsUtils instance = null;
	
	@Getter
	private Grid grid;
	
	@Getter
	private KieServices ks;
	
	@Getter
	private KieContainer kContainer;
	
	@Getter
	private KieSession kSession;
	
	@Setter
	@Getter
	private Configuration config;
	
	private DroolsUtils() {
		try {
			this.ks = KieServices.Factory.get();
			this.kContainer = ks.getKieClasspathContainer();
			this.kSession = kContainer.newKieSession("ksession-rules");
	        //this.kSession.getEnvironment().set("org.jbpm.rule.task.waitstate", "true");
			
			if (this.config == null)
				this.config = new Configuration(9, 9, 10);
			
			this.grid = new Grid(this.config);
			
			kSession.insert(grid);
			kSession.fireAllRules();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static DroolsUtils getInstance() {
		if (instance == null)
			instance = new DroolsUtils();
		
		return instance;
	}
	
}
