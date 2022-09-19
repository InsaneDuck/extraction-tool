package dev.insaneduck.extraction_tool.modal;

public class JsonField
{
    private String fieldName;
    private String data;

    public JsonField(String fieldName, String data)
    {
        this.fieldName = fieldName;
        this.data = data;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getData()
    {
        return data;
    }

    public void setData(String data)
    {
        this.data = data;
    }

    @Override
    public String toString()
    {
        return fieldName + " = " + data;
    }
}
