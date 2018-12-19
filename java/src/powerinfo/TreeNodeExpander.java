/*
Java Power Info utility, (C)2018 IC Book Labs
Table model for Tree Node Expand (text built), abstract class
*/

package powerinfo;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;

public abstract class TreeNodeExpander implements TreeExpansionListener
{
// Standard
@Override abstract public void treeExpanded(TreeExpansionEvent e1);
@Override abstract public void treeCollapsed(TreeExpansionEvent e1);
// Extended
abstract public void setViewer(ViewPanel x);
abstract public void setMonitor(TreeMonitor x);
}


