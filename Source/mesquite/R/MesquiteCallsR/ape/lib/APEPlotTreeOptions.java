/* Mesquite.R source code.  Copyright 2010 W. Maddison, H. Lapp & D. Maddison. 

Mesquite.R is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.

This source code and its compiled class files are free and modifiable under the terms of 
GNU General Public License v. 2.  (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html)
*/
package mesquite.R.MesquiteCallsR.ape.lib;

import java.awt.*;

import mesquite.lib.*;

/* ======================================================================== */
public class APEPlotTreeOptions  {
	int typeB = 0;
	boolean useEdgeLengthB = false;
	boolean showTipLabelB = true;
	boolean showNodeLabelB = false;
	int fontB = 2;
	double cexB = MesquiteDouble.unassigned;
	double justificationB = MesquiteDouble.unassigned;
	double srtB = 0;
	boolean noMarginB = true;
	boolean rootEdgeB = false;
	boolean underscoreB = true;
	int directionB = 0;
	int lab4utB = 0;

	public String queryPlotTreeOptions(MesquiteWindow window){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(window, "ape Plot Tree Options",buttonPressed);
		dialog.addLargeOrSmallTextLabel("Options for ape's Plot tree");
		//	if (queryDialog.isInWizard())
		//		queryDialog.appendToHelpString("<h3>Initial guesses for alpha and sigma parameters</h3>Reasonable defaults might be 0 for alpha and 1 for sigma.");
		Choice type = dialog.addPopUpMenu ("Type", new String[]{"phylogram", "cladogram", "fan", "unrooted", "radial"}, typeB);
		Checkbox useEdgeLength = dialog.addCheckBox ("Use Edge Length", useEdgeLengthB);


		Checkbox showTipLabel = dialog.addCheckBox ("Show Tip Label", showTipLabelB);
		Checkbox showNodeLabel = dialog.addCheckBox ("Show Node Label", false);
		Choice font = dialog.addPopUpMenu ("Font", new String[]{"Plain Text", "Bold", "Italic", "Bold Italic"}, fontB);
		DoubleField cex = dialog.addDoubleField("Character expansion", cexB, 20);
		DoubleField justification = dialog.addDoubleField ("Justification (0 left, 1 right)", justificationB, 20);
		DoubleField srt = dialog.addDoubleField ("Label Rotation (degrees)", srtB, 20);

		Checkbox noMargin = dialog.addCheckBox ("No Margin", noMarginB);
		Checkbox rootEdge = dialog.addCheckBox ("Draw Root Edge", rootEdgeB);
		Checkbox underscore = dialog.addCheckBox ("Underscores as Spaces", underscoreB);  //inverted
		Choice direction = dialog.addPopUpMenu ("Direction", new String[]{"Right", "Left", "Up", "Down"}, directionB);
		Choice lab4ut = dialog.addPopUpMenu ("Unrooted Label Orientation", new String[]{"Horizontal", "Axial"}, lab4utB);

		dialog.completeAndShowDialog(true);
		String s = null;
		if (buttonPressed.getValue()==0) {
			typeB = type.getSelectedIndex();
			s = "type = \"" + type.getSelectedItem() + "\", ";
			
			useEdgeLengthB = useEdgeLength.getState();
			if (useEdgeLength.getState())
				s += "use.edge.length = TRUE, ";
			else
				s += "use.edge.length = FALSE, ";
			
			showTipLabelB = showTipLabel.getState();
			if (showTipLabel.getState())
				s += "show.tip.label = TRUE, ";
			else
				s += "show.tip.label = FALSE, ";
			
			showNodeLabelB = showTipLabel.getState();
			if (showNodeLabel.getState())
				s += "show.node.label = TRUE, ";
			else
				s += "show.node.label = FALSE, ";
			
			fontB = font.getSelectedIndex();
			s += "font = " + (font.getSelectedIndex() + 1 + ", ");
			
			cexB = cex.getValue();
			if (MesquiteDouble.isCombinable(cex.getValue()))
				s += "cex = " + cex.getValue() + ", ";
			
			justificationB = justification.getValue();
			if (MesquiteDouble.isCombinable(justification.getValue()))
			s += "adj = " + justification.getValue() + ", ";
			
			srtB = srt.getValue();
			if (MesquiteDouble.isCombinable(srt.getValue()))
			s += "srt = " + srt.getValue() + ", ";
			
			noMarginB = noMargin.getState();
			if (noMargin.getState())
				s += "no.margin = TRUE, ";
			else
				s += "no.margin  = FALSE, ";
			
			rootEdgeB = rootEdge.getState();
			if (rootEdge.getState())
				s += "root.edge = TRUE, ";
			else
				s += "root.edge  = FALSE, ";
			
			underscoreB = underscore.getState();
			if (!underscore.getState())  //inverted
				s += "underscore = TRUE, ";
			else
				s += "underscore  = FALSE, ";
			
			directionB = direction.getSelectedIndex();
			if (direction.getSelectedIndex() ==0)
				s += "direction = \"rightwards\", ";
			else if (direction.getSelectedIndex() ==1)
				s += "direction = \"leftwards\", ";
			else if (direction.getSelectedIndex() ==2)
				s += "direction = \"upwards\", ";
			else if (direction.getSelectedIndex() ==3)
				s += "direction = \"downwards\", ";
			
			lab4utB = lab4ut.getSelectedIndex();
			if (lab4ut.getSelectedIndex() ==0)
				s += "lab4ut = \"horizontal\"";
			else if (lab4ut.getSelectedIndex() ==1)
				s += "lab4ut = \"axial\"";
			
			
		}

		dialog.dispose();
		return s;
	}

}


