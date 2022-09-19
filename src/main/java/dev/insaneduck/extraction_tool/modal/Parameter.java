package dev.insaneduck.extraction_tool.modal;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
public class Parameter
{
    boolean selected;
    private String nodePath;
    private String parameterClass;
    private String parameterName;
    private String parameterType;

    public Parameter(String nodePath, String parameterClass, String parameterName, String parameterType, boolean selected)
    {
        this.nodePath = nodePath;
        this.parameterClass = parameterClass;
        this.parameterName = parameterName;
        this.parameterType = parameterType;
        this.selected = selected;
    }

    public Parameter()
    {

    }

    public static String getJson(List<Parameter> parameterList)
    {
        StringBuilder json = new StringBuilder("{ \"parameters\" : [");
        for (Parameter parameter : parameterList)
        {
            json.append(json(parameter)).append(",");
        }
        //Fun Fact: only reason StringUtils.chop exists is to remove last character
        json = new StringBuilder(StringUtils.chop(json.toString()));
        json.append("]}");
        //return "{ \"parameters\" : " + parameters + "}";
        return json.toString();
    }

    public static String json(Parameter parameter)
    {
        return "{\"nodePath\" : \"" + parameter.getNodePath()
                + "\", \"parameterClass\" : \"" + parameter.getParameterClass()
                + "\", \"parameterName\" : \"" + parameter.getParameterName()
                + "\", \"parameterType\" : \"" + parameter.getParameterType()
                + "\", \"selected\" : \"" + parameter.isSelected() + "\"}";
    }

    @Override
    public String toString()
    {
        return parameterName + " : " + parameterType;
    }
}
