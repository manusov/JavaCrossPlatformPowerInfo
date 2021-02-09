/*
Java Power Info utility, (C)2021 IC Book Labs
Built node lists for "/sys", "/proc" lists, linux
*/

package powerinfo.supportlinux;

import java.util.ArrayList;
import javax.swing.tree.DefaultMutableTreeNode;
import powerinfo.*;

public class TreeModelLinux 
{

private static final String SYS_NAME = "/sys";
private static final String SYS_PATH = "" + SYS_NAME;     // + "/";

private static final String PROC_NAME = "/proc";
private static final String PROC_PATH = "" + PROC_NAME;   // + "/";

private static ArrayList<DefaultMutableTreeNode> sysList, procList;
private static ListEntry sysEntry;
private static DefaultMutableTreeNode sysRoot;

public TreeModelLinux()
    {
        
    sysEntry = new ListEntry( SYS_NAME, "", SYS_PATH, false, false );
    sysRoot = new DefaultMutableTreeNode( sysEntry, true );
    sysList = new ArrayList();
    sysList.add( sysRoot );
    // NOTE THIS OPERATION REQUIRED LARGE MEMORY BLOCK !
    FileService.builtTree( sysList,  2, null, true, true );

    sysEntry = new ListEntry( PROC_NAME, "", PROC_PATH, false, false );
    sysRoot = new DefaultMutableTreeNode( sysEntry, true );
    procList = new ArrayList();
    procList.add( sysRoot );
    // NOTE THIS OPERATION REQUIRED LARGE MEMORY BLOCK !
    FileService.builtTree( procList,  3, null, true, true );

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
