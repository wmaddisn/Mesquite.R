/* Mesquite.R source code.  Copyright 2010 W. Maddison, H. Lapp & D. Maddison. 

Mesquite.R is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.

This source code and its compiled class files are free and modifiable under the terms of 
GNU General Public License v. 2.  (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html)
*/
package mesquite.R.MesquiteCallsR.diversitree.lib;


import mesquite.R.MesquiteCallsR.lib.RLinker;
import mesquite.categ.lib.RequiresExactlyCategoricalData;
import mesquite.diverse.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterDistribution;

import mesquite.stochchar.lib.MargLikelihoodForModel;

public abstract class BiSSELikelihoodDRBase extends NumForCharAndTreeDivers {

	/*------------------------------------------------------------------------------------------*/
	public String getName() {
		return "BiSSE Likelihood (diversitree)";
	}

	public String getVeryShortName(){
		if (reportMode>0)
			return "BiSSE Likelihood (" + reportModes.getValue(reportMode) + ")";
		return "BiSSE (diversitree)";
	}

	public String getAuthors() {
		return "Rich FitzJohn";
	}

	public String getVersion() {
		return "1.0";
	}

	public String getExplanation(){
		return "(From diversitree package in R): Calculates likelihood with a tree of a species diversification model whose speciation and extinction rates depend on the state of a binary character (BiSSE model, Maddison, Midford & Otto, 2007).";
	}

	/*.................................................................................................................*/
	/** returns keywords related to what the module does, for help-related searches. */ 
	public  String getKeywords()  {
		return "diversification birth death";
	}

	public boolean isPrerelease(){
		return true;
	}

	protected MesquiteParameter mu0;   //user specified extinction rate in state 0
	protected MesquiteParameter lambda0;   //user specified speciation rate in state 0
	protected MesquiteParameter mu1;   //user specified extinction rate in state 1
	protected MesquiteParameter lambda1;   //user specified speciation rate in state 1
	protected MesquiteParameter q01;   //user specified transition rate from state 0 to state 1
	protected MesquiteParameter q10;   //user specifiedtransition rate from state 1 to state 0

	protected MesquiteParameter[] params;
	protected MesquiteParameter[] paramsCopy;
	protected boolean suspended = false;
	protected MesquiteString reportModeName;
	protected StringArray reportModes;
	protected int reportMode = 0;
	protected RLinker rLinker;

	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		rLinker = new RLinker();
		if (!rLinker.linkToR()) {
			return sorry(getName() + " cannot run: Problem with R.");
		}
		double def = MesquiteDouble.unassigned;
		//following is for the parameters explorer
		lambda0 = new MesquiteParameter("lambda0", "Rate of speciation with state 0", def, 0, MesquiteDouble.infinite, 0.000, 1);
		lambda1 = new MesquiteParameter("lambda1", "Rate of speciation with state 1", def, 0, MesquiteDouble.infinite, 0.000, 1);
		mu0 = new MesquiteParameter("mu0", "Rate of extinction with state 0", def, 0, MesquiteDouble.infinite, 0.000, 1);
		mu1 = new MesquiteParameter("mu1", "Rate of extinction with state 1", def, 0, MesquiteDouble.infinite, 0.000, 1);
		q01 = new MesquiteParameter("q01", "Rate of 0->1 changes", def, 0, MesquiteDouble.infinite, 0.000, 1);
		q10 = new MesquiteParameter("q10", "Rate of 1->0 changes", def, 0, MesquiteDouble.infinite, 0.000, 1);
		params = new MesquiteParameter[]{lambda0, lambda1, mu0, mu1, q01, q10};
		reportModes = new StringArray(7);  
		reportModes.setValue(0, "ln Likelihood");  //the strings passed will be the menu item labels
		reportModes.setValue(1, lambda0.getName());  //the strings passed will be the menu item labels
		reportModes.setValue(2, lambda1.getName());  //the strings passed will be the menu item labels
		reportModes.setValue(3, mu0.getName());  //the strings passed will be the menu item labels
		reportModes.setValue(4, mu1.getName());  //the strings passed will be the menu item labels
		reportModes.setValue(5, q01.getName());  //the strings passed will be the menu item labels
		reportModes.setValue(6, q10.getName());  //the strings passed will be the menu item labels
		reportModeName = new MesquiteString(reportModes.getValue(reportMode));  //this helps the menu keep track of checkmenuitems
		MesquiteSubmenuSpec mss = addSubmenu(null, "Report BiSSE Results As", makeCommand("setReportMode", this), reportModes); 
		mss.setSelected(reportModeName);

		if (MesquiteThread.isScripting()  && !MesquiteThread.suppressInteractionAsLibrary)
			suspended = true;
		if (!MesquiteThread.isScripting()){
			if (!showDialog())
				return sorry(getName() + " couldn't start because parameters not specified.");
		}
		addMenuItem("Set Parameters...", makeCommand("setParameters", this));
		addMenuItem("Recalculate", makeCommand("resume", this));

		return true;
	}
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresExactlyCategoricalData();
	}
	/*.................................................................................................................*/
	boolean showDialog(){
		if (params == null)
			return false;
		String s = whatIsMyPurpose();
		if (!StringUtil.blank(s))
			s = "BiSSE Parameters for " + s;
		else
			s = "BiSSE Parameters";
		ParametersDialog dlog = new ParametersDialog(containerOfModule(), "BiSSE Parameters", s, params, null, 2, 2, false);
		dlog.addLargeOrSmallTextLabel("For parameters constrained to be the same as others, DO NOT indicate this reciprocally, e.g. indicate lambda1 is the same as lambda0 but DO NOT indicate at the same time that lambda0 is the same as lambda1. ");
		dlog.appendToHelpString("Parameters for BiSSE model.  Indicate the rates of speciation when in state 0 (lambda0), speciation when in state 1 (lambda1), ");
		dlog.appendToHelpString("rates of extinction when in state 0 (mu0), extinction when in state 1 (mu1), ");
		dlog.appendToHelpString("rates of character change 0 to 1(q01), and rates of character change 1 to 0 (q10). ");
		dlog.completeAndShowDialog(true);

		boolean ok = (dlog.query()==0);
		if (ok) 
			dlog.acceptParameters();
		//	dlog.setInWizard(false);
		dlog.dispose();

		return ok;
	}

	public void initialize(Tree tree, CharacterDistribution charStates1) {
		// TODO Auto-generated method stub

	}
	/*.................................................................................................................*/

	public Snapshot getSnapshot(MesquiteFile file) {
		final Snapshot temp = new Snapshot();
		String pLine = MesquiteParameter.paramsToScriptString(params);
		temp.addLine("suspend ");
		if (!StringUtil.blank(pLine))
			temp.addLine("setParameters " + pLine);
		temp.addLine("setReportMode " + ParseUtil.tokenize(reportModes.getValue(reportMode)));

		temp.addLine("resume ");
		return temp;
	}
	boolean setParam(MesquiteParameter p, MesquiteParameter[] params, Parser parser){
		double newValue = MesquiteDouble.fromString(parser);
		int loc = parser.getPosition();
		String token = parser.getNextToken();
		if (token != null && "=".equals(token)){
			int con = MesquiteInteger.fromString(parser);
			if (MesquiteInteger.isCombinable(con) && con>=0 && con < params.length)
				p.setConstrainedTo(params[con], false);
		}
		else
			parser.setPosition(loc);
		if ((MesquiteDouble.isUnassigned(newValue) ||  newValue >=0) && newValue != p.getValue()){
			p.setValue(newValue); //change mode
			return true;
		}
		return false;
	}

	/*.................................................................................................................*/
	/*  the main command handling method.  */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(getClass(), "Sets rate parameters", "[double double double double double double]", commandName, "setParameters")) {
			if (StringUtil.blank(arguments)){
				if (!MesquiteThread.isScripting() && showDialog())
					parametersChanged();
			}
			else {
				parser.setString(arguments);
				boolean changed =  setParam(lambda0, params, parser);
				boolean more = setParam(lambda1, params, parser);
				changed = changed || more;
				more = setParam(mu0, params, parser);
				changed = changed || more;
				more = setParam(mu1, params, parser);
				changed = changed || more;
				more = setParam(q01, params, parser);
				changed = changed || more;
				more = setParam(q10, params, parser);
				changed = changed || more;
				if (changed && !MesquiteThread.isScripting())
					parametersChanged(); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(getClass(), "Sets the report mode", null, commandName, "setReportMode")) {
			if (getHiredAs() == MargLikelihoodForModel.class)
				return null;
			String name = parser.getFirstToken(arguments); //get argument passed of option chosen
			int newMode = reportModes.indexOf(name); //see if the option is recognized by its name
			if (newMode >=0 && newMode!=reportMode){
				reportMode = newMode; //change mode
				reportModeName.setValue(reportModes.getValue(reportMode)); //so that menu item knows to become checked
				if (!MesquiteThread.isScripting())
					parametersChanged(); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(getClass(), "Suspends calculations", null, commandName, "suspend")) {
			suspended = true;
		}
		else if (checker.compare(getClass(), "Resumes calculations", null, commandName, "resume")) {
			suspended = false;
			parametersChanged();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}



	public boolean returnsMultipleValues(){
		return true;
	}

}

