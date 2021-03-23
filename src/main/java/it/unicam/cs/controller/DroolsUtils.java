package it.unicam.cs.controller;

import org.kie.api.KieServices;
import org.kie.api.event.rule.ObjectDeletedEvent;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.event.rule.ObjectUpdatedEvent;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import lombok.Getter;

public class DroolsUtils {
	private static DroolsUtils instance = null;
	
	@Getter
	private KieServices ks;
	
	@Getter
	private KieContainer kContainer;
	
	@Getter
	private KieSession kSession;
	
	private void createNewSession() {
		this.kSession = kContainer.newKieSession("ksession-rules");
		this.kSession.addEventListener(new RuleRuntimeEventListener() {

			public void objectUpdated(ObjectUpdatedEvent arg0) {
				System.out.println("*****Object Updated*****\n" +arg0.getObject().toString());
			}

			public void objectInserted(ObjectInsertedEvent arg0) {
				System.out.println("*****Object inserted***** \n" + arg0.getObject().toString());
			}

			public void objectDeleted(ObjectDeletedEvent arg0) {
				System.out.println("*****Object Retracted*****\n" + arg0.getOldObject().toString());
			}
		});
	}
	
	private DroolsUtils() {
		try {
			this.ks = KieServices.Factory.get();
			this.kContainer = ks.getKieClasspathContainer();
			this.createNewSession();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static DroolsUtils getInstance() {
		if (instance == null)
			instance = new DroolsUtils();
		
		return instance;
	}
	
	public void clear() {
		this.kSession.dispose();
		this.createNewSession();
	}
}
