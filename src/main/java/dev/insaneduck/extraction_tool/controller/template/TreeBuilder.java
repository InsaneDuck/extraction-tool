package dev.insaneduck.extraction_tool.controller.template;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import dev.insaneduck.extraction_tool.controller.Logic;
import dev.insaneduck.extraction_tool.modal.Parameter;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

//alternative tree builder
//todo future work
public class TreeBuilder
{
    private final DefaultMutableTreeNode tree = new DefaultMutableTreeNode(new Parameter("", "", "Root", "Root", false));
    List<Parameter> parameterList = new ArrayList<>();

    public TreeBuilder(File xml)
    {
        try
        {
            String json = Logic.xmlToJson(Logic.readTextFromFile(xml));
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(json);
            generateTree(jsonNode, tree);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public DefaultMutableTreeNode getTree()
    {
        return tree;
    }

    public List<Parameter> getParameterAltList()
    {
        return parameterList;
    }

    private void generateTree(JsonNode jsonNode, DefaultMutableTreeNode tree)
    {
        Iterator<String> fields = jsonNode.fieldNames();
        while (fields.hasNext())
        {
            String field = fields.next();
            JsonNodeType fieldType = jsonNode.get(field).getNodeType();
            switch (fieldType)
            {
                case OBJECT ->
                {
                    Parameter parameter = new Parameter(Arrays.toString(tree.getPath()), tree.toString(), field, field, false);
                    parameterList.add(parameter);
                    DefaultMutableTreeNode currentNode = new DefaultMutableTreeNode(parameter);
                    tree.add(currentNode);
                    generateTree(jsonNode.get(field), currentNode);
                }
                case ARRAY ->
                {
                    ArrayNode arrayNode = (ArrayNode) jsonNode.get(field);
                    JsonNode node = arrayNode.get(0);
                    Parameter parameter = new Parameter(Arrays.toString(tree.getPath()), tree.toString(), field, field, false);
                    parameterList.add(parameter);
                    DefaultMutableTreeNode currentNode = new DefaultMutableTreeNode(parameter);
                    tree.add(currentNode);
                    generateTree(node, currentNode);
                }
                case NUMBER, BOOLEAN, STRING ->
                {
                    Parameter parameter = new Parameter(Arrays.toString(tree.getPath()), tree.toString(), field, fieldType.toString(), false);
                    parameterList.add(parameter);
                    DefaultMutableTreeNode currentNode = new DefaultMutableTreeNode(parameter);
                    tree.add(currentNode);
                }
            }
        }
    }
}
