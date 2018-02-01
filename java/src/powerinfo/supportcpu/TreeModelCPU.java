//========== Java Power Info utility, (C)2018 IC Book Labs =====================
//========== Built node lists for Processor ====================================

package powerinfo.supportcpu;

import java.util.ArrayList;
import javax.swing.tree.DefaultMutableTreeNode;
import powerinfo.*;

public class TreeModelCPU 
{

private static ArrayList<DefaultMutableTreeNode> cpuList;
private static ListEntry cpuEntry;
private static DefaultMutableTreeNode cpuRoot, cpuClock, cpuTsc;
private static final String NAME_CPU = "CPU";
private static final String NAME_CLK = "CPU clock";
private static final String NAME_TSC = "TSC";
private static CPUclock cpuClk;
private static long tscClkHz;


public TreeModelCPU()
    {
//--- Initializing CPU clock measurement ---
    cpuClk = new CPUclock();
    tscClkHz = cpuClk.measureTSC();
//--- Create root node --- 
    cpuEntry = new ListEntry( NAME_CPU , "" , "" , true , false );
    cpuRoot = new DefaultMutableTreeNode( cpuEntry , true );
    cpuList = new ArrayList();
    cpuList.add( cpuRoot );
//--- Create and add clock node ---
    cpuEntry = new ListEntry( NAME_CLK , "" , "" , true , false );
    cpuClock = new DefaultMutableTreeNode( cpuEntry , true );
    cpuRoot.add(cpuClock);
//--- Create and add clock TSC node ---
    double x = tscClkHz;
    x/=1000000.0;
    String stsc = String.format("%.2f MHz",x );
    cpuEntry = new ListEntry( NAME_TSC , stsc , "" , true , true );
    cpuTsc = new DefaultMutableTreeNode( cpuEntry , true );
    cpuClock.add(cpuTsc);
    cpuTsc.setAllowsChildren(false);

    }

public ArrayList <DefaultMutableTreeNode> getCpuList()
    {
    return cpuList;
    }

public long getTscClkHz()
    {
    return tscClkHz;
    }

}
