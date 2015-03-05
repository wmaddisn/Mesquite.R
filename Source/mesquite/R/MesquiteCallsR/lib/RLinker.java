/* Mesquite.R source code.  Copyright 2010 W. Maddison, H. Lapp & D. Maddison. 
January 2010.

Mesquite.R is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.

This source code and its compiled class files are free and modifiable under the terms of 
GNU General Public License v. 2.  (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html)
*/
package mesquite.R.MesquiteCallsR.lib;
/*~~  */

import java.awt.*;
import java.io.*;

import mesquite.R.common.APETree;
import mesquite.lib.*;

import org.rosuda.JRI.*;

/* ======================================================================== */
public class RLinker implements RMainLoopCallbacks {
	static boolean init = false;
	mesquite.R.MesquiteCallsR.basic.aMesquiteRIntro.aMesquiteRIntro rModule;
	public static boolean echoMesquiteRCalls = false;
	public static boolean echoRConsole = false;
	public static boolean recordRConsole = false;
	public RLinker(){
		rModule = (mesquite.R.MesquiteCallsR.basic.aMesquiteRIntro.aMesquiteRIntro)MesquiteTrunk.mesquiteTrunk.findEmployeeWithDuty(mesquite.R.MesquiteCallsR.basic.aMesquiteRIntro.aMesquiteRIntro.class);
		/*	if (!init)
			Runtime.getRuntime().exec("java.library.path", );*/
		//	pathSet = true;
	}
	boolean enforceVersionCheck(){
		if (rModule == null)
			return true;
		return rModule.enforceVersionCheck();
	}
	StringBuffer consoleRecord = new StringBuffer(100);
	Rengine engine;
	RStop rStop = null;
	public boolean linkToR(){
		boolean checkLibrary = !(System.getProperty("java.library.path") == null || System.getProperty("java.library.path").indexOf(";")>=0);
		String libraryPath = MesquiteFile.composePath(MesquiteTrunk.getRootPath(), System.getProperty("java.library.path"));
		if (checkLibrary && !MesquiteFile.fileOrDirectoryExists(libraryPath)){
			MesquiteTrunk.mesquiteTrunk.discreetAlert("R library cannot be found.(expected location: " + libraryPath + ")");
			return false;
		}
		if (MesquiteTrunk.isMacOSX()){
			if (System.getenv("R_HOME") == null){
				MesquiteTrunk.mesquiteTrunk.discreetAlert("R cannot be found.  R_HOME is null");
				return false;
			}
			if (checkLibrary && !MesquiteFile.fileExists(libraryPath + MesquiteFile.fileSeparator +  "libjri.jnilib")){
				if (MesquiteTrunk.startedFromExecutable)
					MesquiteTrunk.mesquiteTrunk.discreetAlert("Library file for linking to R is missing.  \"libjri.jnilib\" should be within Mesquite_Folder/lib.  You may have better luck starting Mesquite from the MesquiteForR.command file");
				else
					MesquiteTrunk.mesquiteTrunk.discreetAlert("Library file for linking to R is missing.  R needs to be installed, and within R, RJava needs to be installed.");
				return false;
			}
		}
		else if (MesquiteTrunk.isWindows()){
			if (checkLibrary && !MesquiteFile.fileExists(libraryPath  + MesquiteFile.fileSeparator+ "jri.dll")){
				if (MesquiteTrunk.startedFromExecutable)
					MesquiteTrunk.mesquiteTrunk.discreetAlert("Library file for linking to R is missing.  \"jri.dll\" should be within Mesquite_Folder/lib");
				else
					MesquiteTrunk.mesquiteTrunk.discreetAlert("Library file for linking to R is missing.  R needs to be installed, and within R, RJava needs to be installed.");
				return false;
			}
		}
		else {
			if (false && System.getenv("R_HOME") == null){
				MesquiteTrunk.mesquiteTrunk.discreetAlert("R cannot be found.  R_HOME is null");
				return false;
			}
		}
		try {
			boolean vc = Rengine.versionCheck();  
			if (!vc && enforceVersionCheck()){
				MesquiteTrunk.mesquiteTrunk.discreetAlert("Link to R cannot be estabilished.  It is possible that the version of R is not compatible with the current installation of Mesquite.R in Mesquite.  To relax the requirement for identical versions, choose \"Mesquite.R Require Same Version\" in the Setup submenu of the File menu");
				return false;
			}
			else {
				getEngine();
				if (engine == null){
					MesquiteTrunk.mesquiteTrunk.discreetAlert("Link to R cannot be estabilished.  Please make sure R is installed and of a version compatible with this version of Mesquite.R");
					return false;
			}
			}
				MesquiteThread.addCleanUpJob(rStop = new RStop(engine));
			return true;
		}
		catch(Throwable t){
			if (t instanceof java.lang.UnsatisfiedLinkError){
				MesquiteTrunk.mesquiteTrunk.discreetAlert("R can't be started.  It appears that the JRI library file cannot be found. See log for more details.");
				MesquiteMessage.println("Library path: " + libraryPath);
			}
			else
				MesquiteMessage.println("R can't be started.");
			MesquiteFile.throwableToLog(this, t);
			return false;
		}
	}
    public boolean require(String packageName) {
    	if (engine == null)
    		return false;
		REXP ra = engine.eval("require(" + packageName + ")");
		if (ra == null) {
			MesquiteTrunk.mesquiteTrunk.discreetAlert("The calculation cannot function because the R engine is not returning a response to require(" + packageName + ").");
	    	MesquiteMessage.printStackTrace("The calculation cannot function because the R engine is not returning a response to require(" + packageName + ").");
			return false;
		}
		RBool success = ra.asBool();
		if (success == null) {
			MesquiteTrunk.mesquiteTrunk.discreetAlert("The calculation cannot function because the boolean returned by the R engine to require(" + packageName + ") is null.");
	    	MesquiteMessage.printStackTrace("The calculation cannot function because the boolean returned by the R engine to require(" + packageName + ") is null.");
			return false;
		}
		if ("TRUE".equalsIgnoreCase(success.toString()))
			return true;
		MesquiteTrunk.mesquiteTrunk.discreetAlert("The calculation cannot function because the required R package \"" + packageName + "\" appears not bo be installed correctly.");
    	MesquiteMessage.printStackTrace("The calculation cannot function because the required R package \"" + packageName + "\" appears not bo be installed correctly.");
		return false;
	}
   public synchronized REXP eval(String s) {
    	if (engine == null)
    		return null;
		if (echoMesquiteRCalls)
			MesquiteMessage.println("<@>" + s);
		return engine.eval(s);
	}
    public synchronized REXP eval(String s, boolean convert) {
    	if (engine == null)
    		return null;
		if (echoMesquiteRCalls)
			MesquiteMessage.println("<c@>" + s);
		return engine.eval(s, convert);
	}
    public void assign(String sym, String ct) {
    	if (engine == null)
    		return;
		if (echoMesquiteRCalls)
			MesquiteMessage.println("<ASSIGN String@>" + sym + " FROM " + ct);
     engine.assign(sym, ct);
    }
    public void assign(String sym, REXP r) {
    	if (engine == null)
    		return;
		if (echoMesquiteRCalls)
			MesquiteMessage.println("<ASSIGN REXP@>" + sym + " FROM " + r);
     engine.assign(sym, r);
    }
  public void assign(String sym, double[] val)  {
    	if (engine == null)
    		return;
		if (echoMesquiteRCalls)
			MesquiteMessage.println("<ASSIGN double[]@>" + sym + " FROM " + DoubleArray.toString(val));
     engine.assign(sym, val);
    }

	public void assign(String sym, int[] val) {
    	if (engine == null)
    		return;
		if (echoMesquiteRCalls)
			MesquiteMessage.println("<ASSIGN int[]@>" + sym + " FROM " + IntegerArray.toString(val));
     engine.assign(sym, val);
    }

	public void assign(String sym, boolean[] val) {
    	if (engine == null)
    		return;
		if (echoMesquiteRCalls)
			MesquiteMessage.println("<ASSIGN boolean[]@>" + sym + " FROM " + Bits.toString(val));
     engine.assign(sym, val);
    }
	public void assign(String sym, String[] val) {
    	if (engine == null)
    		return;
		if (echoMesquiteRCalls)
			MesquiteMessage.println("<ASSIGN String[]@>" + sym + " FROM " + StringArray.toString(val));
     engine.assign(sym, val);
    }
	public void end(){
		if (engine != null)
			engine.end();
		MesquiteThread.removeCleanUpJob(rStop);
	}
	


	public void sendTreeToR(APETree tree, String name){
		sendTreeToR(tree, name, false);
	}
	/*--------------------------------*/
	public void sendTreeToR(APETree tree, String name, boolean adjustZeroLengthBranches){
		if (engine == null)
			return;
		String rCommand = tree.toRCommand(adjustZeroLengthBranches);
		if (echoMesquiteRCalls)
			MesquiteMessage.println("<@>" + name + " <- " + rCommand);
		engine.eval(name + " <- " + rCommand);
		if (echoMesquiteRCalls)
			MesquiteMessage.println("<@>class(" + name + ") <- \"phylo\"");
		engine.eval("class(" + name + ") <- \"phylo\"");
	}
	/*--------------------------------*/
	public Rengine getEngine(){
		if (engine == null) {
			engine = Rengine.getMainEngine();
			if (engine != null) {
				engine.addMainLoopCallbacks(this);
			}
		}
		if (engine == null) {
			engine=new Rengine(new String[] {"--vanilla"}, true, this);  //was false
			if (!engine.waitForR()) {
				MesquiteTrunk.mesquiteTrunk.discreetAlert("Cannot load R");
				engine = null;
				return null;
			}
		}
		return engine;
	}
	/*--------------------------------*/
	int counter = -1;
	public int inPackOUCH(Tree tree, int node, int[][] packed, double[] times){
		counter++;
		int thisNum = counter;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			int dNum = inPackOUCH(tree, d, packed, times);
			packed[0][dNum] = thisNum;  //ancestor function
			packed[1][dNum] = d;  //original node number
			times[dNum] = tree.distanceToRoot(d, true, 1.0);
		}
		return thisNum;
	}
	public int[][] packOUCHTree(Tree tree, double[] times){
		counter = -1;
		int[][]packed = new int[2][tree.numberOfNodesInClade(tree.getRoot())];
		for (int k=0; k<2; k++)
			for(int it = 0; it<packed[0].length; it++)
				packed[k][it] = -1;
		packed[0][0] = -1;  //ancestor function
		packed[1][0] = tree.getRoot();  //original node number
		inPackOUCH(tree, tree.getRoot(), packed, times);
		for(int it = 0; it<packed[0].length; it++)
			if (packed[0][it]>=0)
				packed[0][it]++;
		return packed;
	}
	
	public String[] getTaxonLabelsVector(Taxa taxa){
		if (taxa == null)
			return null;
		String[] n = new String[taxa.getNumTaxa()];
		for (int it = 0; it< taxa.getNumTaxa(); it++)
			n[it] = taxa.getTaxonName(it);
		return n;
	}
	/*--------------------------------*/
	public void setEchoRConsole(boolean verb){
		echoRConsole = verb;
	}
	public void setEchoMesquiteRCalls(boolean verb){
		echoMesquiteRCalls = verb;
	}
	public void setRecordConsole(boolean verb){
		recordRConsole = verb;
	}
	public void resetConsoleRecord(){
		consoleRecord.setLength(0);
	}
	public String getConsoleRecord(){
		return consoleRecord.toString();
	}
	public void rWriteConsole(Rengine re, String text, int oType) {
		if (echoRConsole)
			MesquiteMessage.print(text);
		if (recordRConsole)
			consoleRecord.append(text);
	}

	public void rBusy(Rengine re, int which) {
		if (echoMesquiteRCalls)
			MesquiteMessage.println("rBusy("+which+")");
	}

	public String rReadConsole(Rengine re, String prompt, int addToHistory) {
		if (echoMesquiteRCalls)
			MesquiteMessage.print(prompt);
		try {
			BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
			String s=br.readLine();
			return (s==null||s.length()==0)?s:s+"\n";
		} catch (Exception e) {
			if (echoMesquiteRCalls)
				MesquiteMessage.println("jriReadConsole exception: "+e.getMessage());
		}
		return null;
	}

	public void rShowMessage(Rengine re, String message) {
		if (echoMesquiteRCalls)
			MesquiteMessage.println("rShowMessage \""+message+"\"");
	}

	public String rChooseFile(Rengine re, int newFile) {
		FileDialog fd = new FileDialog(new Frame(), (newFile==0)?"Select a file":"Select a new file", (newFile==0)?FileDialog.LOAD:FileDialog.SAVE);
		fd.show();
		String res=null;
		if (fd.getDirectory()!=null) res=fd.getDirectory();
		if (fd.getFile()!=null) res=(res==null)?fd.getFile():(res+fd.getFile());
		return res;
	}

	public void   rFlushConsole (Rengine re) {
	}

	public void   rLoadHistory  (Rengine re, String filename) {
	}			

	public void   rSaveHistory  (Rengine re, String filename) {
	}			

}

class RStop implements CleanUpJob {
	Rengine engine;
	public RStop(Rengine engine){
		this.engine = engine;
	}
	public void cleanUp(){
		MesquiteMessage.println("R cleanup called");
		MesquiteTrunk.mesquiteTrunk.alert("It appears you cancelled or had a crash connected with a calculation using R. " + 
				"If R is having problems, try quitting Mesquite and restarting in order to obtain a new instance of R.");
		engine.rniStop(0);
		engine.end();
	}
}