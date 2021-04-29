package it.unicam.cs.utils;

import org.kie.api.KieServices;
import org.kie.api.event.rule.ObjectDeletedEvent;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.event.rule.ObjectUpdatedEvent;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import lombok.Getter;

/**
 * Singleton Class used to manage the Drools session and interact with the Working Memory and Rules.
 *
 */
public class DroolsUtils {
	
	private static DroolsUtils instance = null;

	private KieServices ks;
	private KieContainer kContainer;
	@Getter
	private KieSession kSession;

	/**
	 * Method to create a new Drools session
	 */
	private void createNewSession() {
		this.kSession = kContainer.newKieSession("ksession-rules");
		this.kSession.addEventListener(new RuleRuntimeEventListener() {

			public void objectUpdated(ObjectUpdatedEvent arg0) {
				// System.out.println("*****Object Updated*****\n" + arg0.getObject().toString());
			}

			public void objectInserted(ObjectInsertedEvent arg0) {
				// System.out.println("*****Object inserted***** \n" + arg0.getObject().toString());
			}

			public void objectDeleted(ObjectDeletedEvent arg0) {
				// System.out.println("*****Object Retracted*****\n" + arg0.getOldObject().toString());
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

	/**
	 * Method to clear the working memory and create new Session.
	 */
	public void clear() {
		this.kSession.dispose();
		this.createNewSession();
	}

	/**
	 * Thread-safe method to insert an Object into the working memory and fire the rules
	 * corresponding to its agendaGroup.
	 * 
	 * @param agendaGroup The agendGroup to use to fire the rules.
	 * @param object      The object to insert into the working memory.
	 */
	public synchronized void insertAndFire(String agendaGroup, Object object) {
		this.kSession.getAgenda().getAgendaGroup(agendaGroup).setFocus();
		this.kSession.insert(object);
		this.kSession.fireAllRules();
	}

	/**
	 * Thread-safe method to fire those rules corresponding to a certain agendaGroup.
	 * 
	 * @param agendaGroup The agendGroup to use to fire the rules.
	 */
	public synchronized void fireGroup(String agendaGroup) {
		this.kSession.getAgenda().getAgendaGroup(agendaGroup).setFocus();
		this.kSession.fireAllRules();
	}
}
