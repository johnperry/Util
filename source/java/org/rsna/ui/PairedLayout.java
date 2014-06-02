/*---------------------------------------------------------------
*  Copyright 2014 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.ui;

import java.awt.*;
import javax.swing.*;

/**
 * A layout manager for laying out components in pairs on
 * lines, with the first component of each line being allocated
 * the same width as all other first components, thus effectively
 * columnizing the pairs.
 */
public class PairedLayout implements LayoutManager {

	private int horizontalGap;
	private int verticalGap;

	public PairedLayout(int horizontalGap, int verticalGap) {
		this.horizontalGap = horizontalGap;
		this.verticalGap = verticalGap;
	}

	public void addLayoutComponent(String name,Component component) {
		//not necessary for this layout manager
	}

	public void removeLayoutComponent(Component component) {
	}

	public Dimension preferredLayoutSize(Container parent) {
		return getLayoutSize(parent,horizontalGap,verticalGap,false);
	}

	public Dimension minimumLayoutSize(Container parent) {
		return getLayoutSize(parent,horizontalGap,verticalGap,false);
	}

	public void layoutContainer(Container parent) {
		getLayoutSize(parent,horizontalGap,verticalGap,true);
	}

	private Dimension getLayoutSize(Container parent, int hGap, int vGap, boolean layout) {
		int width = 0;
		int height = 0;
		Component p = parent.getParent();
		if (p instanceof JScrollPane)
			width = ((JScrollPane)p).getViewport().getExtentSize().width;
		else
			width = parent.getSize().width;
		Component[] components = parent.getComponents();
		Insets insets = parent.getInsets();
		int currentY = insets.top;
		Dimension d0, d1;
		//First, find the maximum width of the first component of each pair.
		int maxWidth = 0;
		for (int i=0; i<components.length; i+=2) {
			d0 = components[i].getPreferredSize();
			maxWidth = Math.max(maxWidth, d0.width);
		}
		//Now put the components on the lines.
		for (int i=0; i<components.length-1; i+=2) {
			int currentX = insets.left;
			d0 = components[i].getPreferredSize();
			d1 = components[i+1].getPreferredSize();
			//put the components on the line
			if (layout) {
				components[i].setBounds(currentX, currentY, maxWidth, d0.height);
				int x1 = currentX + maxWidth + hGap;
				int w1 = width - x1 - insets.right;
				components[i+1].setBounds(x1, currentY, w1, d1.height);
			}
			currentY += Math.max(d0.height, d1.height) + vGap;
		}
		return new Dimension(width, currentY+insets.bottom);
	}

}
