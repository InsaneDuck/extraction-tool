package dev.insaneduck.extraction_tool.experimental;

import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme;
import dev.insaneduck.extraction_tool.controller.Logic;
import dev.insaneduck.extraction_tool.controller.template.TreeBuilder;
import dev.insaneduck.extraction_tool.modal.Parameter;

import java.io.File;
import java.util.List;

public class Experimental
{
    public static void main(String[] args)
    {
        FlatOneDarkIJTheme.setup();
        TreeBuilder treeBuilder = new TreeBuilder(new File("/home/satya/Downloads/newxml/Belfast1.xml"));
        Result result = new Result(treeBuilder.getTree());
        List<Parameter> parameterList = treeBuilder.getParameterAltList();
        System.out.println(Logic.beautify(Parameter.getJson(parameterList)));
    }
}
