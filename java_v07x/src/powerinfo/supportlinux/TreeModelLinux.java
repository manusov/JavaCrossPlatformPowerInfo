//========== Java Power Info utility, (C)2018 IC Book Labs =====================
//========== Built node lists for "/sys", "/proc" lists, linux =================

package powerinfo.supportlinux;

import java.util.ArrayList;
import javax.swing.tree.DefaultMutableTreeNode;
import powerinfo.*;

public class TreeModelLinux 
{

private static String sysName = "/sys";
private static String sysPath = "" + sysName; // + "/";

private static String procName = "/proc";
private static String procPath = "" + procName; // + "/";

private static ArrayList<DefaultMutableTreeNode> sysList, procList;
private static ListEntry sysEntry;
private static DefaultMutableTreeNode sysRoot;

public TreeModelLinux()
    {
        
    sysEntry = new ListEntry( sysName, "", sysPath, false, false );
    sysRoot = new DefaultMutableTreeNode( sysEntry, true );
    sysList = new ArrayList();
    sysList.add( sysRoot );
    // NOTE THIS OPERATION REQUIRED LARGE MEMORY BLOCK !
    FileService.builtTree( sysList,  2, null, true, true );    // changes v0.61

    sysEntry = new ListEntry( procName, "", procPath, false, false );
    sysRoot = new DefaultMutableTreeNode( sysEntry, true );
    procList = new ArrayList();
    procList.add( sysRoot );
    // NOTE THIS OPERATION REQUIRED LARGE MEMORY BLOCK !
    FileService.builtTree( procList,  3, null, true, true );  // changes v0.61

    }

public ArrayList <DefaultMutableTreeNode> getSysList()
    {
    return sysList;
    }

public ArrayList <DefaultMutableTreeNode> getProcList()
    {
    return procList;
    }
    
}
