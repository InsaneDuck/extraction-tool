package dev.insaneduck.extraction_tool.controller.extraction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import dev.insaneduck.extraction_tool.controller.Logic;
import dev.insaneduck.extraction_tool.modal.JsonField;
import dev.insaneduck.extraction_tool.modal.NodeValue;
import dev.insaneduck.extraction_tool.modal.Parameter;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

//deals with data extraction and exporting
public class Extractor
{
    //list to save template data
    private final List<Parameter> parameters;
    //list to save unique object node data
    private final List<NodeValue> objectCSV = new ArrayList<>();
    //list to save all array node data
    private final List<NodeValue> arrayCSV = new ArrayList<>();
    //initialising root tree node for preserving parent node data for child node
    private final DefaultMutableTreeNode nodeTree = new DefaultMutableTreeNode("Root");
    private TreeNode whichNode = new DefaultMutableTreeNode();
    private int depth;

    //for preview
    public Extractor(File xmlFile, List<Parameter> parameters)
    {
        this.parameters = parameters;
        init(xmlFile);
    }

    public Extractor(File xmlFile, List<Parameter> parameters, int depth)
    {
        this.parameters = parameters;
        this.depth = depth;
        init(xmlFile);
    }

    public DefaultMutableTreeNode getNodeTree()
    {
        return nodeTree;
    }

    public List<NodeValue> getObjectCSV()
    {
        return objectCSV;
    }

    public List<NodeValue> getArrayCSV()
    {
        return arrayCSV;
    }

    void init(File xmlFile)
    {
        //reading json string from file
        String json = Logic.xmlToJson(Logic.readTextFromFile(xmlFile));
        //getting data from json node using template if it is provided or extract everything if no template is provided
        getDataFromJson(json);
        //execute this method only when depth is applied
        if (depth > 0)
        {
            usedDepth();
        }
        //adding source column to every node
        arrayCSV.forEach(nodeValue -> {
            nodeValue.getNodeKeysAndValues().put("source", xmlFile.getName());
        });
        objectCSV.forEach(nodeValue -> {
            nodeValue.getNodeKeysAndValues().put("source", xmlFile.getName());
        });
    }

    public void getDataFromJson(String json)
    {
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(json);
            traverseNode(jsonNode, nodeTree);
        }
        catch (JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void traverseNode(JsonNode jsonNode, DefaultMutableTreeNode treeNode)
    {
        switch (jsonNode.getNodeType())
        {
            case OBJECT ->
            {
                NodeValue nodeValue = new NodeValue(new DefaultMutableTreeNode(), new LinkedHashMap<>());
                traverseNodeFields(jsonNode, nodeValue, treeNode);
                if (nodeValue.notNull())
                {
                    objectCSV.add(nodeValue);
                }
            }
            case ARRAY ->
            {
                ArrayNode arrayNode = (ArrayNode) jsonNode;
                //for each item/object in that array
                for (JsonNode node : arrayNode)
                {
                    DefaultMutableTreeNode defaultMutableTreeNode = new DefaultMutableTreeNode(treeNode.toString());
                    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) treeNode.getParent();
                    parentNode.add(defaultMutableTreeNode);
                    NodeValue nodeValue = new NodeValue(new DefaultMutableTreeNode(), new LinkedHashMap<>());
                    traverseNodeFields(node, nodeValue, defaultMutableTreeNode);
                    if (nodeValue.notNull())
                    {
                        arrayCSV.add(nodeValue);
                    }
                }
            }
        }
/*
        old method
        if (jsonNode.isObject())
        {
            NodeValue nodeValue = new NodeValue(new DefaultMutableTreeNode(), new LinkedHashMap<>());
            traverseNodeFields(jsonNode, nodeValue, treeNode);
            if (nodeValue.notNull())
            {
                objectCSV.add(nodeValue);
            }
        }
        else if (jsonNode.isArray())
        {
            ArrayNode arrayNode = (ArrayNode) jsonNode;
            //for each item/object in that array
            for (JsonNode node : arrayNode)
            {
                DefaultMutableTreeNode defaultMutableTreeNode = new DefaultMutableTreeNode(treeNode.toString());
                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) treeNode.getParent();
                parentNode.add(defaultMutableTreeNode);
                NodeValue nodeValue = new NodeValue(new DefaultMutableTreeNode(), new LinkedHashMap<>());
                traverseNodeFields(node, nodeValue, defaultMutableTreeNode);
                if (nodeValue.notNull())
                {
                    arrayCSV.add(nodeValue);
                }
            }
        }
*/
    }

    public void traverseNodeFields(JsonNode node, NodeValue nodeValue, DefaultMutableTreeNode treeNode)
    {
        //getting all fields in a node
        Iterator<String> fields = node.fieldNames();
        //checking every field
        while (fields.hasNext())
        {
            String currentField = fields.next();
            //jackson xml doesn't store parent node names so using default mutable tree to keep track of parent
            DefaultMutableTreeNode currentNode = new DefaultMutableTreeNode(currentField);
            if (!(node.get(currentField).isObject() || node.get(currentField).isArray()))
            {
                //todo filter this according to template
                if (parameters.isEmpty())
                {
                    currentNode = new DefaultMutableTreeNode(new JsonField(currentField, String.valueOf(node.path(currentField))));
                }
                else
                {
                    for (Parameter parameter : parameters)
                    {
                        String className = parameter.getParameterClass().substring(parameter.getParameterClass().lastIndexOf(".") + 1);
                        if (parameter.isSelected() && parameter.getParameterName().equalsIgnoreCase(currentField) && className.equalsIgnoreCase(treeNode.toString()))
                        {
                            currentNode = new DefaultMutableTreeNode(new JsonField(currentField, String.valueOf(node.path(currentField))));
                        }
                    }
                }
                //here
            }
            treeNode.add(currentNode);
            //check if current field is object
            switch (node.get(currentField).getNodeType())
            {
                //if the field is array or object traverse through it again
                case OBJECT, ARRAY ->
                {
                    traverseNode(node.get(currentField), currentNode);
                }
                case NUMBER, BOOLEAN, STRING ->
                {
                    //if no template is defined
                    if (parameters.isEmpty())
                    {
                        nodeValue.setNodeName(treeNode);
                        nodeValue.getNodeKeysAndValues().put(currentField, String.valueOf(node.path(currentField)));
                    }
                    else
                    {
                        for (Parameter parameter : parameters)
                        {
                            String className = parameter.getParameterClass().substring(parameter.getParameterClass().lastIndexOf(".") + 1);
                            if (parameter.isSelected() && parameter.getParameterName().equalsIgnoreCase(currentField) && className.equalsIgnoreCase(treeNode.toString()))
                            {
                                nodeValue.setNodeName(treeNode);
                                nodeValue.getNodeKeysAndValues().put(currentField, String.valueOf(node.path(currentField)));
                            }
                        }
                    }
                }
            }
/*
            old method
            if (node.get(currentField).isObject())
            {
                traverseNode(node.get(currentField), currentNode);
            }
            //check current field is an array
            else if (node.get(currentField).isArray())
            {
                traverseNode(node.get(currentField), currentNode);
            }
            else
            {
                //if no template is defined
                if (parameters.isEmpty())
                {
                    nodeValue.setNodeName(treeNode);
                    nodeValue.getNodeKeysAndValues().put(currentField, String.valueOf(node.path(currentField)));
                }
                else
                {
                    for (Parameter parameter : parameters)
                    {
                        String className = parameter.getParameterClass().substring(parameter.getParameterClass().lastIndexOf(".") + 1);
                        if (parameter.isSelected() && parameter.getParameterName().equalsIgnoreCase(currentField) && className.equalsIgnoreCase(treeNode.toString()))
                        {
                            nodeValue.setNodeName(treeNode);
                            nodeValue.getNodeKeysAndValues().put(currentField, String.valueOf(node.path(currentField)));
                        }
                    }
                }
            }
*/
        }
    }

    //this will check for parent nodes other field values and add them to current fields
    void usedDepth()
    {
        objectCSV.forEach(this::traverseParentNodes);
        arrayCSV.forEach(this::traverseParentNodes);
    }

    void traverseParentNodes(NodeValue nodeValue)
    {
        int tempDepth = depth;
        TreeNode treeNode = nodeValue.getNodeName();
        while (tempDepth > 0)
        {
            List<JsonField> jsonFieldList = appendParentParameters(treeNode);
            if (!jsonFieldList.isEmpty())
            {
                for (JsonField jsonField : jsonFieldList)
                {
                    nodeValue.getNodeKeysAndValues().put(whichNode + "." + jsonField.getFieldName(), jsonField.getData());
                }
                treeNode = whichNode.getParent();
            }
            --tempDepth;
        }

    }

    List<JsonField> appendParentParameters(TreeNode node)
    {
        TreeNode n = new DefaultMutableTreeNode();
        try
        {
            n = node.getParent();
            if (nodeHasFields(n))
            {
                whichNode = n;
            }
            else
            {
                while (!nodeHasFields(n))
                {
                    //keep getting parent nodes until it finds a node with fields
                    n = n.getParent();
                    whichNode = n;
                }
            }
        }
        catch (NullPointerException e)
        {
            //no parent nodes for current node
        }
        return getFieldsFromChildNodes(n);
    }


    List<JsonField> getFieldsFromChildNodes(TreeNode node)
    {
        List<JsonField> jsonFieldList = new ArrayList<>();
        try
        {
            int count = node.getChildCount();
            for (int i = 0; i < count; i++)
            {
                try
                {
                    jsonFieldList.add((JsonField) ((DefaultMutableTreeNode) node.getChildAt(i)).getUserObject());
                }
                catch (ClassCastException e)
                {
                    //do nothing and continue for loop
                }
            }
        }
        catch (NullPointerException e)
        {
            //field list null
        }
        return jsonFieldList;
    }

    boolean nodeHasFields(TreeNode node)
    {
        List<JsonField> jsonFieldList = new ArrayList<>();
        try
        {
            int count = node.getChildCount();
            for (int i = 0; i < count; i++)
            {
                try
                {
                    jsonFieldList.add((JsonField) ((DefaultMutableTreeNode) node.getChildAt(i)).getUserObject());
                }
                catch (ClassCastException e)
                {
                    //if there are child nodes, but they cannot be cast to Field class then return no fields
                }
            }
        }
        catch (NullPointerException e)
        {
            //null pointer if node has no child nodes
            //no child nodes = no fields
        }
        return (!jsonFieldList.isEmpty());
    }
}
