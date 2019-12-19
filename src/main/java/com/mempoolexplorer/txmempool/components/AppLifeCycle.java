package com.mempoolexplorer.txmempool.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.mempoolexplorer.txmempool.AppProfiles;

@Component
@Profile(value = { AppProfiles.DEV, AppProfiles.PROD })
public class AppLifeCycle {

	
	
	private Boolean applicationReadyEventFirstTime = Boolean.TRUE;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	// @PostConstruct does not hook well. Called when webServices are not
	// initialized. better hook on an aplicationReadyEvent.
	@EventListener(ApplicationReadyEvent.class)
	public void initialization(ApplicationReadyEvent event) {
		// SpringApplicationBuilder fires multiple notification to that listener (for
		// every child)
		// if (event.getApplicationContext().getParent() == null) {
		// Code above does not work, making tricks...
		if (applicationReadyEventFirstTime) {
			applicationReadyEventFirstTime = Boolean.FALSE;
		}
		
		
		
	}
}
