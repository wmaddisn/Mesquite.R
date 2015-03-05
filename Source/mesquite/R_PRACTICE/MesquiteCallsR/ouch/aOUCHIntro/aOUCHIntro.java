/* Mesquite.R source code.  Copyright 2010 W. Maddison, H. Lapp & D. Maddison. 

Mesquite.R is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.

This source code and its compiled class files are free and modifiable under the terms of 
GNU General Public License v. 2.  (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html)
*/
package mesquite.R_PRACTICE.MesquiteCallsR.ouch.aOUCHIntro;
/*~~  */

import mesquite.lib.duties.*;

/* ======================================================================== */
public class aOUCHIntro extends PackageIntro {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
  	 }
  	 public Class getDutyClass(){
  	 	return aOUCHIntro.class;
  	 }
	/*.................................................................................................................*/
    	 public String getExplanation() {
		return "Serves as an introduction to the OUCH glue package for Mesquite.";
   	 }
   
    		/*.................................................................................................................*/
    		/** Returns the R packages needed*/
    		public String[] packagesRequired(){
    			return new String[]{"ouch"};
    		}
	/*.................................................................................................................*/
    	 public String getName() {
		return "OUCH Package Introduction";
   	 }
	/*.................................................................................................................*/
	/** Returns the name of the package of modules (e.g., "Basic Mesquite Package", "Rhetenor")*/
 	public String getPackageName(){
 		return "OUCH";
 	}
	/*.................................................................................................................*/
	/** Returns citation for a package of modules*/
 	public String getPackageCitation(){
 		return "";
 	}
	/*.................................................................................................................*/
  	 public String getPackageVersion() {
		return "";
   	 }
	/*.................................................................................................................*/
  	 public String getPackageAuthors() {
		return "";
   	 }
	/*.................................................................................................................*/
	/** Returns whether there is a splash banner*/
	public boolean hasSplash(){
 		return true; 
	}
}
