/* Mesquite.R source code.  Copyright 2010 W. Maddison, H. Lapp & D. Maddison. 

Mesquite.R is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.

This source code and its compiled class files are free and modifiable under the terms of 
GNU General Public License v. 2.  (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html)
 */
package mesquite.R.MesquiteCallsR.basic.aMesquiteRIntro;
/*~~  */


import mesquite.R.MesquiteCallsR.lib.RLinker;
import mesquite.R.common.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import java.io.File;

/* ======================================================================== */
public class aMesquiteRIntro extends MesquiteInit implements PackageIntroInterface {
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Provides a link to functions of the R package.";
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Mesquite.R Package Introduction";
	}
	/*.................................................................................................................*/
	/** Returns the name of the package of modules (e.g., "Basic Mesquite Package", "Rhetenor")*/
	public String getPackageName(){
		return "Mesquite.R";
	}
	/*.................................................................................................................*/
	/** returns the URL of the notices file for this module so that it can phone home and check for messages */
	public String  getHomePhoneNumber(){ 
		return "http://mesquiteproject.org/packages/Mesquite.R/notices.xml";  
	}
	/*.................................................................................................................*/
	/** Returns citation for a package of modules*/
	public String getPackageCitation(){
		return "";
	}
	/*.................................................................................................................*/
	public String getPackageVersion() {
		return "0.7";
	}
	/*.................................................................................................................*/
	/** Returns whether package is built-in (comes with default install of Mesquite)*/
	public boolean isBuiltInPackage(){ 
		return false;
	}
	/*.................................................................................................................*/
	/** Returns build number for a package of modules as an integer*/
	public int getPackageBuildNumber(){
		return 003;
	}
	/*.................................................................................................................*/
	public String getPackageAuthors() {
		return "Wayne Maddison & Hilmar Lapp";
	}
	/*.................................................................................................................*/
	/** Returns the URL for the web page about the package*/
	public String getPackageURL(){
		return "http://mesquiteproject.org/packages/Mesquite.R/";  
	}
	public String getPackageDateReleased(){
		return "September 2011";
	}
	/*.................................................................................................................*/
	/** Returns version for a package of modules as an integer*/
	public int getPackageVersionInt(){
		return 51;
	}
	MesquiteBoolean echoRConsole, enforceVersion, echoMesquiteRCalls;
	public static boolean run = false;
	public static boolean askedEnforceVersion = false;
	static final String BUILTFOR = " 2.13.1";
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		echoRConsole = new MesquiteBoolean(RLinker.echoRConsole);
		echoMesquiteRCalls = new MesquiteBoolean(RLinker.echoMesquiteRCalls);
		enforceVersion = new MesquiteBoolean(true);
		loadPreferences();
		
		//If this is a first run, then notify
		if (!run && System.getProperty("startedForR") == null){
			String batchFileSuffix = "sh";
			if (MesquiteTrunk.isMacOSX())
				batchFileSuffix = "command";
			else if (MesquiteTrunk.isWindows())
				batchFileSuffix = "bat";
			alert("This copy of Mesquite has Mesquite.R installed to make use of functions in R.  In order to be able to use these functions, you should start Mesquite by double clicking the script file mesquiteForR." + batchFileSuffix);
		}
		
		//If this is windows, make sure batch file points correctly to R directory
		if (MesquiteTrunk.isWindows()){ //check to see that mesquiteForR.bat has good reference to R installation
			String baseOfR = "C:\\Program Files\\R\\";
			String batchFile = MesquiteFile.getFileContentsAsString(getRootPath() + "mesquiteForR.bat");
			File rDir = new File(baseOfR);
			if (rDir.exists() && rDir.isDirectory()){
				boolean found = false;
				String[] rInstalls = rDir.list();
				StringArray.sort(rInstalls);
				if (batchFile != null ){
					for (int i = 0; i< rInstalls.length; i++){
						if (batchFile.indexOf(rInstalls[i])>= 0)
							found = true;

					}
				}
				if (!found){
					if (!MesquiteFile.canWrite(getRootPath() + "mesquiteForR.bat")){
						if (!run)
							alert("This copy of Mesquite has Mesquite.R installed to make use of functions in R.  The batch file mesquiteForR.bat is either missing or has in incorrect reference to R, but Mesquite cannot correct this problem.");
					}
					else
						chooseWindowsRVersion(rInstalls);
				}
			}
			else {
				if (!run)
					alert("This copy of Mesquite has Mesquite.R installed to make use of functions in R, but R appears not to be installed on this computer.");
			}
		}
		storePreferences();
		if (MesquiteTrunk.isWindows())
			MesquiteTrunk.mesquiteTrunk.addItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.setupSubmenu,"Set R Version to Use...", makeCommand("chooseRVersion",  this));
		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.setupSubmenu,"Mesquite.R Require Same Version", makeCommand("toggleVersion",  this), enforceVersion);
		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.setupSubmenu,"Mesquite.R Echo R Console", makeCommand("toggleEchoRConsole",  this), echoRConsole);
		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.setupSubmenu,"Mesquite.R Echo Mesquite.R Calls", makeCommand("toggleEchoMesquiteRCalls",  this), echoMesquiteRCalls);
		logln("Mesquite.R version " + getPackageVersion() + " is active.  This version was built for version" + BUILTFOR  + " of R.");
		return true;
	}
	
	void chooseWindowsRVersion(String[] rInstalls){
		int choice = ListDialog.queryList(containerOfModule(), "Version of R", "This copy of Mesquite has Mesquite.R installed, but it appears that the batch file to start it is missing or doesn't have a correct reference to a version of R.  Choose a version of R for Mesquite to use.  Mesquite will modify the batch file; you would then need to restart Mesquite using that batch file to use R", null, rInstalls, 0);
		if (choice >=0 && MesquiteInteger.isCombinable(choice)){
			String chosen = rInstalls[choice];
			String[] lines = new String[2];
			lines[0] = "set PATH=%PATH%;C:\\Program Files\\R\\" + chosen + "\\bin";
			lines[1] = "java -Xmx400M -DstartedForR=\"r\" -Djava.library.path=\"C:\\Program Files\\R\\" + chosen + "\\library\\rJava\\jri\" -Djri.ignore.ule=\"yes\" -cp . mesquite.Mesquite";
			MesquiteFile.putFileContents(getRootPath() + "mesquiteForR.bat", lines, true);
		}
	}
	public boolean enforceVersionCheck(){
		if (!askedEnforceVersion) {
			if (AlertDialog.query(containerOfModule(), "Require Same Version?", "This version of Mesquite.R was built with a different version of the libraries " +
					"that link to R than those currently installed in R itself.  Do you want to proceed regardless?  (This choice will be remembered.  To change whether Mesquite.R requires the same version, " +
					" select Mesquite.R Require Same Version in the Setup submenu of the File menu.)")){
				enforceVersion.setValue(false);
			}
			askedEnforceVersion = true;
			storePreferences();
		}
		return enforceVersion.getValue();
	}
	public void processSingleXMLPreference (String tag, String content) {
		if ("echoRConsole".equalsIgnoreCase(tag)){  
			echoRConsole.setValue(content);
			RLinker.echoRConsole = echoRConsole.getValue();
		}
		else if ("echoMesquiteRCalls".equalsIgnoreCase(tag)){  
			echoMesquiteRCalls.setValue(content);
			RLinker.echoMesquiteRCalls = echoMesquiteRCalls.getValue();
		}		
		else if ("enforceVersion".equalsIgnoreCase(tag)){  
			enforceVersion.setValue(content);
		}
		else if ("run".equalsIgnoreCase(tag)){  
			run = true;
		}
		else if ("askedEnforceVersion".equalsIgnoreCase(tag)){  
			askedEnforceVersion = true;
		}
	}
	public void endJob(){
		storePreferences();
		super.endJob();
	}
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "echoRConsole", echoRConsole);   
		StringUtil.appendXMLTag(buffer, 2, "echoMesquiteRCalls", echoMesquiteRCalls);   
		StringUtil.appendXMLTag(buffer, 2, "enforceVersion", enforceVersion);   
		StringUtil.appendXMLTag(buffer, 2, "run", "true");   
		if (askedEnforceVersion)
			StringUtil.appendXMLTag(buffer, 2, "askedEnforceVersion", "true");   
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Allows the user to choose the version of R to use.", null, commandName, "chooseRVersion")) {
			if (!MesquiteTrunk.isWindows())
				return null;
			String baseOfR = "C:\\Program Files\\R\\";
			File rDir = new File(baseOfR);
			if (rDir.exists() && rDir.isDirectory()){
				String[] rInstalls = rDir.list();
				StringArray.sort(rInstalls);
				chooseWindowsRVersion(rInstalls);
			}
			else {
				alert("Sorry, Mesquite cannot find any version of R installed on this computer.  R can be obtained from http://cran.r-project.org");
			}
		}
		else if (checker.compare(this.getClass(), "toggles whether the R console is echoed to the Mesquite Log.", null, commandName, "toggleEchoRConsole")) {
			echoRConsole.toggleValue(parser.getFirstToken(arguments));
			RLinker.echoRConsole = echoRConsole.getValue();
			
		}
		else if (checker.compare(this.getClass(), "toggles whether commands sent to R are echoed to the Mesquite Log.", null, commandName, "toggleEchoMesquiteRCalls")) {
			echoMesquiteRCalls.toggleValue(parser.getFirstToken(arguments));
			RLinker.echoMesquiteRCalls = echoMesquiteRCalls.getValue();
			
		}
		else if (checker.compare(this.getClass(), "toggles whether R is to enforce same version in library and system.", null, commandName, "toggleVersion")) {
			enforceVersion.toggleValue(parser.getFirstToken(arguments));
			
		}
		else
			return super.doCommand(commandName, arguments, checker);
		return null;
	}

	public Class getDutyClass(){
		return aMesquiteRIntro.class;
	}
	/*.................................................................................................................*/
	/** Returns whether there is a splash banner*/
	public boolean hasSplash(){
		return true; 
	}

	/*.................................................................................................................*/
	/** Returns the URL of document shown when splash screen icon touched. By default, returns path to module's manual*/
	public String getSplashURL(){
		String splashP =getPath()+"index.html";
		if (MesquiteFile.fileExists(splashP))
			return splashP;
		splashP =getPath()+"splash.html";
		if (MesquiteFile.fileExists(splashP))
			return splashP;
		else
			return getManualPath();
	}
	public boolean getSearchableAsModule(){
		return false;
	}
	/*.................................................................................................................*/
	/** Returns whether hideable*/
	public boolean getHideable(){
		return true;
	}

}
