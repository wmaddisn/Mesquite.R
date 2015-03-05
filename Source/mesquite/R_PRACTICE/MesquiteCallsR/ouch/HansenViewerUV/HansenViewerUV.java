/* Mesquite.R source code.  Copyright 2010 W. Maddison, H. Lapp & D. Maddison. 

Mesquite.R is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.

This source code and its compiled class files are free and modifiable under the terms of 
GNU General Public License v. 2.  (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html)
*/
package mesquite.R_PRACTICE.MesquiteCallsR.ouch.HansenViewerUV;
/*~~  */

import java.awt.*;

import mesquite.R_PRACTICE.MesquiteCallsR.ouch.OUCHHansenFitUV.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;


public class HansenViewerUV extends TreeWindowAssistantA    {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(OUCHHansenFitUV.class, getName() + "  needs a method to calculate Hansen Fit.",
		"You can choose the diversification calculation initially or under the Diversification Measure submenu.");
		e.setPriority(2);
		EmployeeNeed ew = registerEmployeeNeed(CharSourceCoordObed.class, getName() + "  needs a source of characters.",
		"The source of characters is arranged initially");
	}
	/*.................................................................................................................*/
	int current = 0;
	Tree tree;
	OUCHHansenFitUV numberTask;
	CharSourceCoordObed characterSourceTask;
	Taxa taxa;
	Class stateClass;
	MesquiteWindow containingWindow;
	HFUVPanel panel;
	MesquiteString numberTaskName;
	MesquiteCommand ntC;
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		numberTask = (OUCHHansenFitUV)hireEmployee(OUCHHansenFitUV.class, "Calculator of Hansen Fit");
		if (numberTask == null)
			return sorry(getName() + " couldn't start because no calculator module obtained.");
		makeMenu("Hansen_Fit");
		ntC =makeCommand("setCalculator",  this);
 		numberTask.setHiringCommand(ntC);
 		numberTaskName = new MesquiteString();
 		numberTaskName.setValue(numberTask.getName());


		characterSourceTask = (CharSourceCoordObed)hireCompatibleEmployee(CharSourceCoordObed.class, numberTask.getCompatibilityTest(), "Source of  characters (for " + getName() + ")");
		if (characterSourceTask == null)
			return sorry(getName() + " couldn't start because no source of characters was obtained.");
		MesquiteWindow f = containerOfModule();
		if (f instanceof MesquiteWindow){
			containingWindow = (MesquiteWindow)f;
			containingWindow.addSidePanel(panel = new HFUVPanel(), 200);
		}

		addMenuItem( "Choose Character...", makeCommand("chooseCharacter",  this));
		addMenuItem( "Close Hansen Fit Analysis", makeCommand("close",  this));
		addMenuItem( "-", null);

		return true;
	}


	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public void windowGoAway(MesquiteWindow whichWindow) {
		whichWindow.hide();
		whichWindow.dispose();
		iQuit();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("getCharSource ", characterSourceTask); 
		temp.addLine("getCalculator ", numberTask); 
		temp.addLine("setCharacter " + CharacterStates.toInternal(current)); 
		temp.addLine("doCounts");

		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {

		if (checker.compare(this.getClass(), "Sets the module that calculates Hansen Fit", "[name of module]", commandName, "getCalculator")) {
			
				return numberTask;
		}

		else   	if (checker.compare(this.getClass(), "Returns employee", null, commandName, "getCharSource")) {
			return characterSourceTask;
		}
		else if (checker.compare(this.getClass(), "Queries the user about what character to use", null, commandName, "chooseCharacter")) {
			int ic=characterSourceTask.queryUserChoose(taxa, " Character to use for Hansen Fit analysis ");
			if (MesquiteInteger.isCombinable(ic) && ic != current) {
				current = ic;
				doCounts();
			}
		}
		else if (checker.compare(this.getClass(), "Sets the character to use", "[character number]", commandName, "setCharacter")) {
			int icNum = MesquiteInteger.fromFirstToken(arguments, stringPos);
			if (!MesquiteInteger.isCombinable(icNum))
				return null;
			int ic = CharacterStates.toInternal(icNum);
			if ((ic>=0) && characterSourceTask.getNumberOfCharacters(taxa)==0) {
				current = ic;

			}
			else if ((ic>=0) && (ic<=characterSourceTask.getNumberOfCharacters(taxa)-1)) {
				current = ic;

			}
		}

		else if (checker.compare(this.getClass(), "Provokes Calculation", null, commandName, "doCounts")) {
			doCounts();
		}
		else if (checker.compare(this.getClass(), "Quits", null, commandName, "close")) {
			if (panel != null && containingWindow != null)
				containingWindow.removeSidePanel(panel);
			iQuit();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	long oldTreeID = -1;
	long oldTreeVersion = 0;
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		if (tree==null)
			return;
		this.tree=tree;
		taxa = tree.getTaxa();
		if ((tree.getID() != oldTreeID || tree.getVersionNumber() != oldTreeVersion) && !MesquiteThread.isScripting()) {
			doCounts();  //only do counts if tree has changed
		}
		oldTreeID = tree.getID();
		oldTreeVersion = tree.getVersionNumber();
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (numberTask!=null && !MesquiteThread.isScripting())
			doCounts();
	}
	String blankIfNull(String s){
		if (s == null)
			return "";
		return s;
	}
	/*.................................................................................................................*/
	public void doCounts() {
		if (taxa == null)
			return;
		CharacterDistribution states = characterSourceTask.getCharacter(taxa, current);
		stateClass = states.getStateClass();
		MesquiteNumber result = new MesquiteNumber();
		MesquiteString rs = new MesquiteString();
//		window.setText("");
		panel.setStatus(true);
		panel.repaint();
		panel.setText("\nAnalysis of Fit of Hansen Model from OUCH\n");
		panel.append("\nCalculation: " + numberTask.getNameAndParameters() + "\n");
		panel.append("\nTree: " + tree.getName() );
		panel.append("\nCharacter: " + characterSourceTask.getCharacterName(taxa, current) +  "\n");
		if (states == null )
			rs.setValue("Sorry, no character was not obtained.  The diversification analysis could not be completed.");
		else 
			numberTask.calculateNumber(tree, states, result, rs);
		panel.append("\n\n" + blankIfNull(result.getName()) + '\t' + result);
		MesquiteNumber[] aux = result.getAuxiliaries();
		if (aux != null){
			panel.append("\n");
			for (int i = 0; i< aux.length; i++){
				panel.append('\n' + blankIfNull(aux[i].getName()) + '\t' + aux[i].toString());
			}
		}
		else
			panel.append("\n\n" + rs);
		
		panel.append("\n\n" + rs);
		panel.append("\n\nExplanation of calculation:\n" + numberTask.getExplanation());
		panel.setStatus(false);
		panel.repaint();
//		window.append("\n\n  " + rs);
	}
	/*.................................................................................................................*/
	public String getName() {
		return "OUCH Hansen Fit Analysis";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Coordinates analyses for the Hansen Fit)." ;
	}
	public void endJob() {
		if (panel != null && containingWindow != null)
			containingWindow.removeSidePanel(panel);
		super.endJob();
	}
}

class HFUVPanel extends MousePanel{
	TextArea text;
	Font df = new Font("Dialog", Font.BOLD, 14);
	boolean calculating = false;
	int labelHeight = 52;
	public HFUVPanel(){
		super();
		text = new TextArea(" ", 50, 50, TextArea.SCROLLBARS_VERTICAL_ONLY);
		setLayout(null);
		add(text);
		text.setLocation(0,labelHeight);
		text.setVisible(true);
		text.setEditable(false);
		setBackground(Color.darkGray);
		text.setBackground(Color.white);
	}
	public void setStatus(boolean calculating){
		this.calculating = calculating;
	}
	public void setText(String t){
		text.setText(t);
	}
	public void append(String t){
		text.append(t);
	}
	public void setSize(int w, int h){
		super.setSize(w, h);
		text.setSize(w, h-labelHeight);
	}
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x, y, w, h);
		text.setSize(w, h-labelHeight);
	}
	public void paint(Graphics g){
		g.setFont(df);

		if (!calculating){
			g.setColor(Color.white);
			g.drawString("OUCH Hansen Fit", 8, 20);
		}
		else{
			g.setColor(Color.black);
			g.fillRect(0,0, getBounds().width, 50);
			g.setColor(Color.red);
			g.drawString("OUCH Hansen Fit", 8, 20);
			g.drawString("Calculating...", 8, 46);
		}
	}
}
