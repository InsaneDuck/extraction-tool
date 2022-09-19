package dev.insaneduck.extraction_tool.modal;

import javax.swing.tree.TreeNode;
import java.util.LinkedHashMap;
import java.util.Objects;

//class to store json node name and all it's values
public class NodeValue
{
    private TreeNode nodeName;
    private LinkedHashMap<String, String> nodeKeysAndValues;

    public NodeValue(TreeNode nodeName, LinkedHashMap<String, String> nodeKeysAndValues)
    {
        this.nodeName = nodeName;
        this.nodeKeysAndValues = nodeKeysAndValues;
    }

    public TreeNode getNodeName()
    {
        return nodeName;
    }

    public void setNodeName(TreeNode nodeName)
    {
        this.nodeName = nodeName;
    }

    public LinkedHashMap<String, String> getNodeKeysAndValues()
    {
        return nodeKeysAndValues;
    }

    public void setNodeKeysAndValues(LinkedHashMap<String, String> nodeKeysAndValues)
    {
        this.nodeKeysAndValues = nodeKeysAndValues;
    }

    public boolean notNull()
    {
        return !Objects.equals(nodeName.toString(), "");
    }

    @Override
    public String toString()
    {
        return "\nnodeName='" + nodeName.toString() + "'" +
                ", nodeKeysAndValues=" + nodeKeysAndValues;
    }
}
