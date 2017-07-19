package com.duncanson.DocRouter;

import java.util.ArrayList;
/**
 * Subject - Observer pattern implementation between CADEngine's doRun (i.e. Observer)
 *           and Operand's getNextImageFile (i.e. Observable) where getNextImageFile
 *           triggers update of running state when there are no more files left
 *           in the target directory.
 *
 */
public class Subject {
    private ArrayList<Observer> observers;
    private boolean isRunning;

    public Subject() {
    	observers = new ArrayList<Observer>();
    }
    
    public void add(Observer observer) {
        observers.add(observer);
        System.out.println("observers count: "+observers.size());
    }
    
    public void remove(Observer inputObserver) {
    	System.out.println("Did we get here?");
    }

    public boolean getRunningState() {
        return isRunning;
    }

    public void setRunningState(boolean value) {
        this.isRunning = value;
        execute();
    }

    private void execute() {
    	
        for (Observer observer : observers) {
            observer.update();
        }
    }
}
