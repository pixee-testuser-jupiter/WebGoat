package org.dummy.insecure.framework;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.LocalDateTime;

@Slf4j
//TODO move back to lesson
public class VulnerableTaskHolder implements Serializable {

	private static final long serialVersionUID = 2;

	private String taskName;
	private String taskAction;
	private LocalDateTime requestedExecutionTime;
	
	public VulnerableTaskHolder(String taskName, String taskAction) {
		super();
		this.taskName = taskName;
		this.taskAction = taskAction;
		this.requestedExecutionTime = LocalDateTime.now();
	}
	
	@Override
	public String toString() {
		return "VulnerableTaskHolder [taskName=" + taskName + ", taskAction=" + taskAction + ", requestedExecutionTime="
				+ requestedExecutionTime + "]";
	}

	/**
	 * Execute a task when de-serializing a saved or received object.
	 * @author stupid develop
	 */
	private void readObject( ObjectInputStream stream ) throws Exception {
        //unserialize data so taskName and taskAction are available
		stream.defaultReadObject();
		
		//do something with the data
		log.info("restoring task: {}", taskName);
		log.info("restoring time: {}", requestedExecutionTime);
		
		if (requestedExecutionTime!=null && 
				(requestedExecutionTime.isBefore(LocalDateTime.now().minusMinutes(10))
				|| requestedExecutionTime.isAfter(LocalDateTime.now()))) {
			//do nothing is the time is not within 10 minutes after the object has been created
			log.debug(this.toString());
			throw new IllegalArgumentException("outdated");
		}
		
		//condition is here to prevent you from destroying the goat altogether
		if ((taskAction.startsWith("sleep")||taskAction.startsWith("ping"))
				&& taskAction.length() < 22) {
		log.info("about to execute: {}", taskAction);
		try {
            Process p = io.pixee.security.SystemCommand.runCommand(Runtime.getRuntime(), taskAction);
            BufferedReader in = new BufferedReader(
                                new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = io.pixee.security.SafeIO.boundedReadLine(in, 1000000)) != null) {
                log.info(line);
            }
        } catch (IOException e) {
            log.error("IO Exception", e);
        }
		}
       
    }
	
}